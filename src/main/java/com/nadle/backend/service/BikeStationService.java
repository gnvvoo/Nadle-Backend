package com.nadle.backend.service;

import com.nadle.backend.dto.StationDetailResponse;
import com.nadle.backend.dto.StationResponse;
import com.nadle.backend.dto.external.ExternalStationInfoItem;
import com.nadle.backend.dto.external.ExternalStationItem;
import com.nadle.backend.exception.StationNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class BikeStationService {

    private static final Logger log = LoggerFactory.getLogger(BikeStationService.class);

    // 지구 평균 반지름 (단위: m)
    private static final double EARTH_RADIUS_M = 6_371_000.0;

    // 최대 허용 반경 (단위: m)
    private static final int MAX_RADIUS = 5000;

    // 기본 반경 (단위: m)
    private static final int DEFAULT_RADIUS = 1000;

    private final BikeStationCacheService cacheService;

    public BikeStationService(BikeStationCacheService cacheService) {
        this.cacheService = cacheService;
    }

    /**
     * 현재 위치 기준 반경 내 자전거 대여소 목록을 조회한다.
     */
    public List<StationResponse> findNearbyStations(Double lat, Double lng, Integer radius, Integer number) {
        int searchRadius = resolveRadius(radius);

        List<ExternalStationItem> allStations = cacheService.fetchAllStations();
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

        if (number != null && number > 0) {
            return result.stream().limit(number).toList();
        }
        return result;
    }

    /**
     * stationId로 특정 대여소 상세 정보를 조회한다.
     * 기본정보와 현황을 병렬로 조회하여 응답 속도를 개선한다.
     */
    public StationDetailResponse findStationById(String stationId) {
        // 기본정보(주소, 운영시간)와 현황(가용 자전거)을 병렬로 조회
        CompletableFuture<List<ExternalStationInfoItem>> infoFuture =
                CompletableFuture.supplyAsync(() -> cacheService.fetchAllStationInfos());

        CompletableFuture<List<ExternalStationItem>> availFuture =
                CompletableFuture.supplyAsync(() -> cacheService.fetchAllStations());

        CompletableFuture.allOf(infoFuture, availFuture).join();

        ExternalStationInfoItem infoItem = infoFuture.join().stream()
                .filter(item -> stationId.equals(item.getStationId()))
                .findFirst()
                .orElseThrow(() -> new StationNotFoundException(stationId));

        ExternalStationItem availItem = availFuture.join().stream()
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
