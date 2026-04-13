package com.nadle.backend.config;

import com.nadle.backend.service.BikeStationCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * 앱 시작 시 외부 API 데이터를 미리 캐시에 적재하여 첫 요청 지연을 방지한다.
 */
@Component
public class CacheWarmup {

    private static final Logger log = LoggerFactory.getLogger(CacheWarmup.class);

    private final BikeStationCacheService cacheService;

    public CacheWarmup(BikeStationCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void warmup() {
        log.info("캐시 워밍업 시작 (백그라운드)");

        CompletableFuture<Void> stationsFuture = CompletableFuture
                .runAsync(cacheService::fetchAllStations)
                .whenComplete((v, ex) -> {
                    if (ex != null) log.error("대여소 현황 캐시 워밍업 실패: {}", ex.getMessage());
                    else log.info("대여소 현황 캐시 워밍업 완료");
                });

        CompletableFuture<Void> infosFuture = CompletableFuture
                .runAsync(cacheService::fetchAllStationInfos)
                .whenComplete((v, ex) -> {
                    if (ex != null) log.error("대여소 기본정보 캐시 워밍업 실패: {}", ex.getMessage());
                    else log.info("대여소 기본정보 캐시 워밍업 완료");
                });
    }
}
