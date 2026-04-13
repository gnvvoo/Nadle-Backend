package com.nadle.backend.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    // 대여소 현황(가용 자전거 수): 2분 TTL
    public static final String CACHE_STATIONS = "stations";

    // 대여소 기본정보(위치, 주소 등): 10분 TTL
    public static final String CACHE_STATION_INFOS = "stationInfos";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();

        // 기본 캐시: 각 캐시별 설정은 Caffeine 빌더로 직접 등록
        manager.registerCustomCache(CACHE_STATIONS,
                Caffeine.newBuilder()
                        .expireAfterWrite(2, TimeUnit.MINUTES)
                        .maximumSize(1)
                        .build());

        manager.registerCustomCache(CACHE_STATION_INFOS,
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(1)
                        .build());

        return manager;
    }
}
