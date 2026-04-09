package com.nadle.backend.controller;

import com.nadle.backend.dto.ApiResponse;
import com.nadle.backend.dto.SpotCategory;
import com.nadle.backend.dto.SpotListResponse;
import com.nadle.backend.service.TourSpotService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/spots")
@Validated
public class TourSpotController {

    private final TourSpotService tourSpotService;

    public TourSpotController(TourSpotService tourSpotService) {
        this.tourSpotService = tourSpotService;
    }

    /**
     * 기준 좌표(lat, lng) 중심으로 반경 내 관광지 목록을 조회한다.
     *
     * @param lat      기준 위도 (필수)
     * @param lng      기준 경도 (필수)
     * @param radius   검색 반경(m), 기본값 3000m (선택)
     * @param category 관광지 유형 TOUR / FOOD / CULTURE / NATURE, 미입력 시 전체 (선택)
     * @param page     페이지 번호, 기본값 1 (선택)
     * @param size     페이지 당 개수, 기본값 20 (선택)
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<SpotListResponse>> getNearbySpots(
            @RequestParam @NotNull Double lat,
            @RequestParam @NotNull Double lng,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) SpotCategory category,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        SpotListResponse result = tourSpotService.findNearbySpots(lat, lng, radius, category, page, size);
        return ResponseEntity.ok(ApiResponse.success("주변 관광지 조회 성공", result));
    }
}
