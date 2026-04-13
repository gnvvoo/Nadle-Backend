package com.nadle.backend.service;

import com.nadle.backend.config.CacheConfig;
import com.nadle.backend.dto.external.ExternalStationInfoItem;
import com.nadle.backend.dto.external.ExternalStationItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 외부 자전거 API 조회 결과를 캐싱하는 서비스.
 * Spring AOP 프록시가 동작하려면 @Cacheable 메서드가 별도 빈에 있어야 한다.
 */
@Service
public class BikeStationCacheService {

    private static final Logger log = LoggerFactory.getLogger(BikeStationCacheService.class);
    private static final int PAGE_SIZE = 1000;

    private final RestTemplate restTemplate;
    private final String serviceKey;
    private final String endpoint;

    public BikeStationCacheService(RestTemplate restTemplate,
                                   @Value("${bike-api.service-key}") String serviceKey,
                                   @Value("${bike-api.endpoint}") String endpoint) {
        this.restTemplate = restTemplate;
        this.serviceKey = serviceKey;
        this.endpoint = endpoint;
    }

    /**
     * 전체 대여소 현황(가용 자전거 수)을 조회한다. 결과는 2분간 캐싱된다.
     */
    @Cacheable(CacheConfig.CACHE_STATIONS)
    public List<ExternalStationItem> fetchAllStations() {
        log.info("[캐시 미스] 대여소 현황 API 전체 조회 시작");
        List<ExternalStationItem> result = new ArrayList<>();
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

            log.info("현황 페이지 {} 조회 완료: {}개 누적", pageNo, result.size());
            pageNo++;
        }

        return result;
    }

    /**
     * 전체 대여소 기본정보(주소, 운영시간 등)를 조회한다. 결과는 10분간 캐싱된다.
     */
    @Cacheable(CacheConfig.CACHE_STATION_INFOS)
    public List<ExternalStationInfoItem> fetchAllStationInfos() {
        log.info("[캐시 미스] 대여소 기본정보 API 전체 조회 시작");
        List<ExternalStationInfoItem> result = new ArrayList<>();
        int pageNo = 1;
        int totalCount = Integer.MAX_VALUE;

        while (result.size() < totalCount) {
            String url = endpoint + "/inf_101_00010001_v2"
                    + "?serviceKey=" + serviceKey
                    + "&numOfRows=" + PAGE_SIZE
                    + "&pageNo=" + pageNo
                    + "&type=json";

            log.info("기본정보 API 호출 (페이지 {})", pageNo);

            @SuppressWarnings("unchecked")
            Map<String, Object> raw = restTemplate.getForObject(url, Map.class);
            if (raw == null) break;

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> body = (Map<String, Object>) raw.get("body");
                if (body == null) break;

                if (pageNo == 1) {
                    totalCount = ((Number) body.getOrDefault("totalCount", 0)).intValue();
                    log.info("전체 대여소 기본정보 수: {}", totalCount);
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("item");
                if (items == null || items.isEmpty()) break;

                items.forEach(item -> {
                    ExternalStationInfoItem info = new ExternalStationInfoItem();
                    info.setStationId((String) item.get("rntstnId"));
                    info.setStationName((String) item.get("rntstnNm"));
                    info.setAddress((String) item.get("roadNmAddr"));
                    info.setLat(toDouble(item.get("lat")));
                    info.setLng(toDouble(item.get("lot")));
                    info.setOperStartHour((String) item.get("operBgngHrCn"));
                    info.setOperEndHour((String) item.get("operEndHrCn"));
                    result.add(info);
                });

                log.info("기본정보 페이지 {} 조회 완료: {}개 누적", pageNo, result.size());
                pageNo++;
            } catch (Exception e) {
                log.error("기본정보 파싱 실패 (페이지 {}): {}", pageNo, e.getMessage());
                break;
            }
        }

        return result;
    }

    private Double toDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).doubleValue();
        try { return Double.parseDouble(value.toString()); }
        catch (NumberFormatException e) { return null; }
    }
}
