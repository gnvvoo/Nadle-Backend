package com.nadle.backend.controller;

import com.nadle.backend.dto.ApiResponse;
import com.nadle.backend.dto.RouteRecommendResponse;
import com.nadle.backend.service.RouteRecommendService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/routes")
@Validated
public class RouteRecommendController {

    private final RouteRecommendService routeRecommendService;

    public RouteRecommendController(RouteRecommendService routeRecommendService) {
        this.routeRecommendService = routeRecommendService;
    }

    /**
     * 기준 대여소 위치를 기반으로 AI 여행 코스를 추천한다.
     *
     * @param stationLat 기준 대여소 위도 (필수)
     * @param stationLng 기준 대여소 경도 (필수)
     * @param duration   여행 예상 시간(분), 기본값 120 (선택)
     */
    @GetMapping("/recommend")
    public ResponseEntity<ApiResponse<RouteRecommendResponse>> recommendRoute(
            @RequestParam @NotNull Double stationLat,
            @RequestParam @NotNull Double stationLng,
            @RequestParam(required = false) Integer duration
    ) {
        RouteRecommendResponse response = routeRecommendService.recommend(stationLat, stationLng, duration);
        return ResponseEntity.ok(ApiResponse.success("AI 코스 추천 성공", response));
    }
}
