package com.nadle.backend.service;

import com.nadle.backend.dto.RouteRecommendResponse;
import com.nadle.backend.dto.external.TourSpotItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteRecommendService {

    private static final Logger log = LoggerFactory.getLogger(RouteRecommendService.class);

    // 기본 여행 시간 (분)
    private static final int DEFAULT_DURATION = 120;

    private final TourApiService tourApiService;
    private final GroqService groqService;

    public RouteRecommendService(TourApiService tourApiService, GroqService groqService) {
        this.tourApiService = tourApiService;
        this.groqService = groqService;
    }

    /**
     * 기준 대여소 위치 기반으로 AI 여행 코스를 추천한다.
     *
     * @param stationLat 기준 대여소 위도
     * @param stationLng 기준 대여소 경도
     * @param duration   여행 예상 시간(분), null이면 기본값 120 사용
     * @return AI 추천 여행 코스
     */
    public RouteRecommendResponse recommend(Double stationLat, Double stationLng, Integer duration, String requirements) {
        int resolvedDuration = duration != null ? duration : DEFAULT_DURATION;
        int radius = resolveRadius(resolvedDuration);
        int spotCount = resolveSpotCount(resolvedDuration);

        log.info("코스 추천 요청 - 위도: {}, 경도: {}, 시간: {}분, 반경: {}m, 추천 관광지 수: {}개, 요구사항: {}",
                stationLat, stationLng, resolvedDuration, radius, spotCount, requirements);

        // TourAPI의 mapX = 경도, mapY = 위도
        List<TourSpotItem> nearbySpots = tourApiService.fetchNearbySpots(stationLng, stationLat, radius);

        if (nearbySpots.isEmpty()) {
            throw new RuntimeException("주변 관광지를 찾을 수 없습니다. 다른 위치를 시도해보세요.");
        }

        log.info("주변 관광지 {}개 조회 완료, Groq 코스 추천 요청", nearbySpots.size());
        return groqService.recommendCourse(nearbySpots, resolvedDuration, spotCount, requirements);
    }

    /**
     * duration에 따라 검색 반경을 결정한다.
     */
    private int resolveRadius(int duration) {
        if (duration <= 60) return 1500;
        if (duration <= 120) return 3000;
        return 5000;
    }

    /**
     * duration에 따라 추천 관광지 수를 결정한다.
     */
    private int resolveSpotCount(int duration) {
        if (duration <= 60) return 2;
        if (duration <= 120) return 3;
        if (duration <= 180) return 4;
        return 5;
    }
}
