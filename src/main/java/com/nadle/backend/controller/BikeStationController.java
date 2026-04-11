package com.nadle.backend.controller;

import com.nadle.backend.dto.ApiResponse;
import com.nadle.backend.dto.StationDetailResponse;
import com.nadle.backend.dto.StationResponse;
import com.nadle.backend.service.BikeStationService;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stations")
@Validated
public class BikeStationController {

    private final BikeStationService bikeStationService;

    public BikeStationController(BikeStationService bikeStationService) {
        this.bikeStationService = bikeStationService;
    }

    /**
     * 현재 위치 기준 반경 내 자전거 대여소 목록을 조회한다.
     *
     * @param lat    현재 위치 위도 (필수)
     * @param lng    현재 위치 경도 (필수)
     * @param radius 검색 반경(m), 기본값 1000m, 최대 5000m (선택)
     */
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<StationResponse>>> getNearbyStations(
            @RequestParam @NotNull Double lat,
            @RequestParam @NotNull Double lng,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) Integer number
    ) {
        List<StationResponse> stations = bikeStationService.findNearbyStations(lat, lng, radius, number);
        return ResponseEntity.ok(ApiResponse.success("대여소 조회 성공", stations));
    }

    /**
     * 특정 자전거 대여소 상세 정보를 조회한다.
     *
     * @param stationId 대여소 고유 ID (공공데이터 기준)
     */
    @GetMapping("/{stationId}")
    public ResponseEntity<ApiResponse<StationDetailResponse>> getStationDetail(
            @PathVariable String stationId
    ) {
        StationDetailResponse detail = bikeStationService.findStationById(stationId);
        return ResponseEntity.ok(ApiResponse.success("대여소 상세 조회 성공", detail));
    }
}
