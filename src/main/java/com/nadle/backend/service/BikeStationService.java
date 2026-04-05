package com.nadle.backend.service;

import com.nadle.backend.dto.StationDetailResponse;
import com.nadle.backend.dto.StationResponse;
import com.nadle.backend.dto.external.ExternalApiResponse;
import com.nadle.backend.dto.external.ExternalStationInfoItem;
import com.nadle.backend.dto.external.ExternalStationItem;
import com.nadle.backend.exception.StationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class BikeStationService {

    private static final Logger log = LoggerFactory.getLogger(BikeStationService.class);

    // 지구 평균 반지름 (단위: m)
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    // 최대 허용 반경 (단위: m)
    private static final int MAX_RADIUS = 5000;

    // 기본 반경 (단위: m)
    private static final int DEFAULT_RADIUS = 1000;

    // API 허용 최대 페이지 크기
    private static final int PAGE_SIZE = 1000;

    private final RestTemplate restTemplate;
    private final String serviceKey;
    private final String endpoint;

    public BikeStationService(RestTemplate restTemplate,
                              @Value("${bike-api.service-key}") String serviceKey,
                              @Value("${bike-api.endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
        this.endpoint = endpoint;
    }

    /**
     * 현재 위치 기준 반경 내 자전거 대여소 목록을 조회한다.
     */
    public List<StationResponse> findNearbyStations(Double lat, Double lng, Integer radius) {
        int searchRadius = resolveRadius(radius);

        List<ExternalStationItem> allStations = fetchAllStations();
        log.info("외부 API 조회 결과: {}개 대여소", allStations.size());

        List<StationResponse> result = allStations.stream()
                .filter(item -> item.getLat() != null && item.getLng() != null)
                .map(item -> {
                    double distance = calculateDistance(lat, lng, item.getLat(), item.getLng());
                    return new StationResponse(
                            item.getStationId(),
                            item.getStationName(),
                            item.getLat(),
                            item.getLng(),
                            null,                  // rackTotCnt: API 미제공
                            item.getBikeTotCnt(),  // parkingBikeTotCnt: bcyclTpkctNocs (현재 대여 가능 자전거 수)
                            Math.round(distance * 10.0) / 10.0
                    );
                })
                .filter(station -> station.getDistance() <= searchRadius)
                .sorted(Comparator.comparingDouble(StationResponse::getDistance))
                .toList();

        log.info("반경 {}m 내 대여소: {}개", searchRadius, result.size());
        return result;
    }

    /**
     * stationId로 특정 대여소 상세 정보를 조회한다.
     */
    public StationDetailResponse findStationById(String stationId) {
        ExternalStationInfoItem infoItem = fetchAllStationInfos().stream()
                .filter(item -> stationId.equals(item.getStationId()))
                .findFirst()
                .orElseThrow(() -> new StationNotFoundException(stationId));

        ExternalStationItem availItem = fetchAllStations().stream()
                .filter(item -> stationId.equals(item.getStationId()))
                .findFirst()
                .orElse(null);

        Integer bikeTotCnt = availItem != null ? availItem.getBikeTotCnt() : null;
        String status = (bikeTotCnt != null && bikeTotCnt > 0) ? "ACTIVE" : "INACTIVE";

        return new StationDetailResponse(
                infoItem.getStationId(),
                infoItem.getStationName(),
                infoItem.getAddress(),
                infoItem.getLat(),
                infoItem.getLng(),
                null,         // totalSlots(rackTotCnt): API 미제공
                bikeTotCnt,   // availableBikes: bcyclTpkctNocs (현재 대여 가능 자전거 수)
                infoItem.getOperatingHours(),
                status
        );
    }

    /**
     * 공공데이터 API에서 전체 대여소 현황을 페이지네이션으로 모두 조회한다.
     * API 최대 허용 numOfRows = 1000
     */
    private List<ExternalStationItem> fetchAllStations() {
        List<ExternalStationItem> result = new java.util.ArrayList<>();
        int pageNo = 1;
        int totalCount = Integer.MAX_VALUE;

        while (result.size() < totalCount) {
            String url = endpoint + "/inf_101_00010002_v2"
                    + "?serviceKey=" + serviceKey
                    + "&numOfRows=" + PAGE_SIZE
                    + "&pageNo=" + pageNo
                    + "&type=json";

            @SuppressWarnings("unchecked")
            Map<String, Object> raw = restTemplate.getForObject(url, Map.class);
            if (raw == null) break;

            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) raw.get("body");
            if (body == null) break;

            // 첫 페이지에서 totalCount 설정
            if (pageNo == 1) {
                totalCount = ((Number) body.getOrDefault("totalCount", 0)).intValue();
                log.info("전체 대여소 수: {}", totalCount);
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("item");
            if (items == null || items.isEmpty()) break;

            items.forEach(item -> {
                ExternalStationItem station = new ExternalStationItem();
                station.setStationId((String) item.get("rntstnId"));
                station.setStationName((String) item.get("rntstnNm"));
                station.setLatRaw((String) item.get("lat"));
                station.setLngRaw((String) item.get("lot"));
                station.setBikeTotCntRaw((String) item.get("bcyclTpkctNocs"));
                result.add(station);
            });

            log.info("페이지 {} 조회 완료: {}개 누적", pageNo, result.size());
            pageNo++;
        }

        return result;
    }

    /**
     * 공공데이터 API에서 전체 대여소 기본정보를 조회한다 (주소, 운영시간 포함).
     */
    private List<ExternalStationInfoItem> fetchAllStationInfos() {
        String url = endpoint + "/inf_101_00010001_v2"
                + "?serviceKey=" + serviceKey
                + "&numOfRows=" + PAGE_SIZE
                + "&pageNo=1&type=json";

        log.info("대여소 기본정보 API 호출: {}", url);

        @SuppressWarnings("unchecked")
        Map<String, Object> raw = restTemplate.getForObject(url, Map.class);

        if (raw == null) return List.of();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) raw.get("body");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("item");

            if (items == null) return List.of();

            return items.stream().map(item -> {
                ExternalStationInfoItem info = new ExternalStationInfoItem();
                info.setStationId((String) item.get("rntstnId"));
                info.setStationName((String) item.get("rntstnNm"));
                info.setAddress((String) item.get("roadNmAddr"));
                info.setLat(toDouble(item.get("lat")));
                info.setLng(toDouble(item.get("lot")));
                info.setOperStartHour((String) item.get("operBgngHrCn"));
                info.setOperEndHour((String) item.get("operEndHrCn"));
                return info;
            }).toList();
        } catch (Exception e) {
            log.error("기본정보 파싱 실패: {}", e.getMessage());
            return List.of();
        }
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }

    private int resolveRadius(Integer radius) {
        if (radius == null) return DEFAULT_RADIUS;
        return Math.min(radius, MAX_RADIUS);
    }

    /**
     * Haversine 공식으로 두 좌표 간 거리를 계산한다 (단위: m).
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_M * c;
    }
}
