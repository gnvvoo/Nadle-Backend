package com.nadle.backend.service;

import com.nadle.backend.dto.SpotCategory;
import com.nadle.backend.dto.SpotListResponse;
import com.nadle.backend.dto.SpotResponse;
import com.nadle.backend.dto.external.ExternalTourItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class TourSpotService {

    private static final Logger log = LoggerFactory.getLogger(TourSpotService.class);

    private static final int DEFAULT_RADIUS = 3000;
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;

    private final RestTemplate restTemplate;
    private final String serviceKey;
    private final String endpoint;

    public TourSpotService(RestTemplate restTemplate,
                           @Value("${tour-api.service-key}") String serviceKey,
                           @Value("${tour-api.endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
        this.endpoint = endpoint;
    }

    /**
     * 기준 좌표(lat, lng) 중심으로 반경 내 관광지 목록을 조회한다.
     * 한국관광공사 API의 locationBasedList2 엔드포인트를 사용한다.
     *
     * @param lat      기준 위도
     * @param lng      기준 경도
     * @param radius   검색 반경(m), 기본값 3000m
     * @param category 관광지 유형 필터 (null이면 전체 조회)
     * @param page     페이지 번호, 기본값 1
     * @param size     페이지 당 개수, 기본값 20
     */
    public SpotListResponse findNearbySpots(Double lat, Double lng, Integer radius,
                                            SpotCategory category, Integer page, Integer size) {
        int searchRadius = radius != null ? radius : DEFAULT_RADIUS;
        int pageNo = page != null ? page : DEFAULT_PAGE;
        int numOfRows = size != null ? size : DEFAULT_SIZE;

        String url = buildUrl(lat, lng, searchRadius, category, pageNo, numOfRows);
        log.info("관광 API 호출: {}", url);

        @SuppressWarnings("unchecked")
        Map<String, Object> raw = restTemplate.getForObject(url, Map.class);

        if (raw == null) {
            return new SpotListResponse(pageNo, numOfRows, 0, List.of());
        }

        // 한국관광공사 API는 응답이 {"response": {"header": ..., "body": ...}} 구조
        @SuppressWarnings("unchecked")
        Map<String, Object> response = raw.containsKey("response")
                ? (Map<String, Object>) raw.get("response")
                : raw;

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.get("body");
        if (body == null) {
            log.warn("관광 API 응답에 body 없음. 실제 응답 키: {}", raw.keySet());
            return new SpotListResponse(pageNo, numOfRows, 0, List.of());
        }

        int totalCount = ((Number) body.getOrDefault("totalCount", 0)).intValue();
        log.info("관광 API 응답: 총 {}개", totalCount);

        List<ExternalTourItem> items = parseItems(body);
        List<SpotResponse> spots = items.stream()
                .map(this::toSpotResponse)
                .toList();

        return new SpotListResponse(pageNo, numOfRows, totalCount, spots);
    }

    /**
     * 외부 API 호출 URL을 구성한다.
     * - NATURE 카테고리는 contenttypeid=12 + cat1=A01 조합으로 필터링한다.
     * - 그 외 카테고리는 contenttypeid 파라미터로 서버 사이드 필터링한다.
     */
    private String buildUrl(Double lat, Double lng, int radius, SpotCategory category,
                            int pageNo, int numOfRows) {
        StringBuilder url = new StringBuilder(endpoint)
                .append("/locationBasedList2")
                .append("?serviceKey=").append(serviceKey)
                .append("&MobileOS=ETC")
                .append("&MobileApp=Nadle")
                .append("&mapX=").append(lng)    // mapX = 경도(longitude)
                .append("&mapY=").append(lat)    // mapY = 위도(latitude)
                .append("&radius=").append(radius)
                .append("&numOfRows=").append(numOfRows)
                .append("&pageNo=").append(pageNo)
                .append("&_type=json");

        if (category != null) {
            switch (category) {
                case TOUR    -> url.append("&contentTypeId=12");
                case FOOD    -> url.append("&contentTypeId=39");
                case CULTURE -> url.append("&contentTypeId=14");
                case NATURE  -> url.append("&contentTypeId=12&cat1=A01");
            }
        }

        return url.toString();
    }

    /**
     * API 응답의 body.items.item 을 파싱한다.
     * 결과가 1건일 때 item이 배열이 아닌 단일 객체로 올 수 있으므로 두 경우를 처리한다.
     */
    @SuppressWarnings("unchecked")
    private List<ExternalTourItem> parseItems(Map<String, Object> body) {
        try {
            Map<String, Object> items = (Map<String, Object>) body.get("items");
            if (items == null) return List.of();

            Object itemRaw = items.get("item");
            if (itemRaw == null) return List.of();

            List<Map<String, Object>> itemList;
            if (itemRaw instanceof List) {
                itemList = (List<Map<String, Object>>) itemRaw;
            } else if (itemRaw instanceof Map) {
                itemList = List.of((Map<String, Object>) itemRaw);
            } else {
                return List.of();
            }

            return itemList.stream()
                    .map(this::mapToExternalItem)
                    .toList();

        } catch (Exception e) {
            log.error("관광 API 응답 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private ExternalTourItem mapToExternalItem(Map<String, Object> item) {
        ExternalTourItem t = new ExternalTourItem();
        t.setContentid(str(item.get("contentid")));
        t.setTitle(str(item.get("title")));
        t.setContenttypeid(str(item.get("contenttypeid")));
        t.setAddr1(str(item.get("addr1")));
        t.setFirstimage(str(item.get("firstimage")));
        t.setMapx(str(item.get("mapx")));
        t.setMapy(str(item.get("mapy")));
        t.setDist(str(item.get("dist")));
        t.setCat1(str(item.get("cat1")));
        t.setCat2(str(item.get("cat2")));
        t.setCat3(str(item.get("cat3")));
        return t;
    }

    private SpotResponse toSpotResponse(ExternalTourItem item) {
        SpotCategory category = SpotCategory.fromItem(item.getContenttypeid(), item.getCat1());
        Double distance = item.getDistance();
        double roundedDist = distance != null ? Math.round(distance * 10.0) / 10.0 : 0.0;

        return new SpotResponse(
                item.getContentid(),
                item.getTitle(),
                category,
                item.getAddr1(),
                item.getLat(),
                item.getLng(),
                item.getFirstimage(),
                roundedDist
        );
    }

    private String str(Object value) {
        return value != null ? value.toString() : null;
    }
}
