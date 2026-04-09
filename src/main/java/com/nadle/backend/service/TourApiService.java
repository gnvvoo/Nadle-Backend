package com.nadle.backend.service;

import com.nadle.backend.dto.external.TourSpotItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TourApiService {

    private static final Logger log = LoggerFactory.getLogger(TourApiService.class);

    // 관광지 콘텐츠 타입 ID (12 = 관광지)
    private static final String CONTENT_TYPE_TOURIST_SPOT = "12";

    private final RestTemplate restTemplate;
    private final String serviceKey;
    private final String endpoint;

    public TourApiService(RestTemplate restTemplate,
                          @Value("${tour-api.service-key}") String serviceKey,
                          @Value("${tour-api.endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
        this.endpoint = endpoint;
    }

    /**
     * 위경도 기준 반경 내 관광지 목록을 조회한다.
     *
     * @param mapX   경도
     * @param mapY   위도
     * @param radius 검색 반경 (단위: m)
     * @return 관광지 목록
     */
    public List<TourSpotItem> fetchNearbySpots(Double mapX, Double mapY, int radius) {
        URI uri = UriComponentsBuilder.fromHttpUrl(endpoint + "/locationBasedList2")
                .queryParam("serviceKey", serviceKey)
                .queryParam("numOfRows", 20)
                .queryParam("pageNo", 1)
                .queryParam("MobileOS", "ETC")
                .queryParam("MobileApp", "Nadle")
                .queryParam("mapX", mapX)
                .queryParam("mapY", mapY)
                .queryParam("radius", radius)
                .queryParam("contentTypeId", CONTENT_TYPE_TOURIST_SPOT)
                .queryParam("_type", "json")
                .build()
                .toUri();

        log.info("관광지 API 호출 - 위도: {}, 경도: {}, 반경: {}m", mapY, mapX, radius);

        @SuppressWarnings("unchecked")
        Map<String, Object> raw = restTemplate.getForObject(uri, Map.class);
        if (raw == null) return Collections.emptyList();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = (Map<String, Object>) raw.get("response");
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) response.get("body");
            @SuppressWarnings("unchecked")
            Map<String, Object> items = (Map<String, Object>) body.get("items");

            if (items == null) {
                log.info("주변 관광지 없음 - 위도: {}, 경도: {}", mapY, mapX);
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
            if (itemList == null) return Collections.emptyList();

            List<TourSpotItem> result = itemList.stream().map(item -> {
                TourSpotItem spot = new TourSpotItem();
                spot.setContentId(toString(item.get("contentid")));
                spot.setTitle(toString(item.get("title")));
                spot.setMapx(toDouble(item.get("mapx")));
                spot.setMapy(toDouble(item.get("mapy")));
                spot.setAddr1(toString(item.get("addr1")));
                return spot;
            }).toList();

            log.info("관광지 조회 결과: {}개", result.size());
            return result;

        } catch (Exception e) {
            log.error("관광지 API 응답 파싱 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String toString(Object value) {
        return value != null ? value.toString() : null;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
