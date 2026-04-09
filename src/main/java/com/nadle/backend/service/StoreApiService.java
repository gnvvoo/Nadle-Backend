package com.nadle.backend.service;

import com.nadle.backend.dto.StoreResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class StoreApiService {

    private static final Logger log = LoggerFactory.getLogger(StoreApiService.class);

    private final RestTemplate restTemplate;
    private final String serviceKey;
    private final String endpoint;

    public StoreApiService(RestTemplate restTemplate,
                           @Value("${store-api.service-key}") String serviceKey,
                           @Value("${store-api.endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
        this.endpoint = endpoint;
    }

    /**
     * 위경도 기준 반경 내 상가 목록을 조회한다.
     *
     * @param lat      위도
     * @param lng      경도
     * @param radius   검색 반경 (단위: m, 최대 1000)
     * @param category 업종 대분류코드 (예: Q=음식, D=소매). null이면 전체 조회
     * @return 상가 목록
     */
    public List<StoreResponse> fetchNearbyStores(Double lat, Double lng, int radius, String category) {
        StringBuilder url = new StringBuilder(endpoint)
                .append("/storeListInRadius")
                .append("?serviceKey=").append(serviceKey)
                .append("&pageNo=1")
                .append("&numOfRows=100")
                .append("&cx=").append(lng)
                .append("&cy=").append(lat)
                .append("&radius=").append(radius)
                .append("&type=json");

        if (category != null && !category.isBlank()) {
            url.append("&indsLclsCd=").append(category);
        }

        log.info("상권 API 호출 - 위도: {}, 경도: {}, 반경: {}m, 업종: {}", lat, lng, radius, category);

        @SuppressWarnings("unchecked")
        Map<String, Object> raw = restTemplate.getForObject(url.toString(), Map.class);
        if (raw == null) return Collections.emptyList();
        log.info("상권 API raw 응답: {}", raw);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = (Map<String, Object>) raw.get("body");
            if (body == null) return Collections.emptyList();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
            if (items == null || items.isEmpty()) {
                log.info("주변 상권 없음 - 위도: {}, 경도: {}", lat, lng);
                return Collections.emptyList();
            }

            List<StoreResponse> result = items.stream().map(item -> new StoreResponse(
                    toString(item.get("bizesId")),
                    toString(item.get("bizesNm")),
                    toString(item.get("indsSclsNm")),
                    toString(item.get("indsMclsNm")),
                    toString(item.get("rdnmAdr")),
                    toDouble(item.get("lon")),
                    toDouble(item.get("lat"))
            )).toList();

            log.info("상권 조회 결과: {}개", result.size());
            return result;

        } catch (Exception e) {
            log.error("상권 API 응답 파싱 실패: {}", e.getMessage());
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
