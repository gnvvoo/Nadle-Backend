package com.nadle.backend.controller;

import com.nadle.backend.dto.ApiResponse;
import com.nadle.backend.dto.StoreResponse;
import com.nadle.backend.service.StoreApiService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stores")
@Validated
public class StoreController {

    private static final int DEFAULT_RADIUS = 500;

    private final StoreApiService storeApiService;

    public StoreController(StoreApiService storeApiService) {
        this.storeApiService = storeApiService;
    }

    /**
     * 도착지 주변 상권 목록을 조회한다.
     *
     * @param lat      도착 관광지 위도 (필수)
     * @param lng      도착 관광지 경도 (필수)
     * @param radius   검색 반경(m), 기본값 500, 최대 1000 (선택)
     * @param category 업종 대분류코드 (예: Q=음식, D=소매). 미입력 시 전체 (선택)
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<StoreResponse>>> getNearbyStores(
            @RequestParam @NotNull Double lat,
            @RequestParam @NotNull Double lng,
            @RequestParam(required = false) @Max(value = 1000, message = "반경은 최대 1000m까지 가능합니다.") Integer radius,
            @RequestParam(required = false, defaultValue = "I2") String category
    ) {
        int searchRadius = (radius != null) ? radius : DEFAULT_RADIUS;
        List<StoreResponse> stores = storeApiService.fetchNearbyStores(lat, lng, searchRadius, category);
        return ResponseEntity.ok(ApiResponse.success("주변 상권 목록 조회 성공", stores));
    }
}
