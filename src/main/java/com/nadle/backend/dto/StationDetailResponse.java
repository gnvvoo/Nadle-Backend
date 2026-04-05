package com.nadle.backend.dto;

// 자전거 대여소 상세 응답 DTO
public class StationDetailResponse {

    private String stationId;       // 대여소 고유 ID
    private String stationName;     // 대여소 명칭
    private String address;         // 대여소 주소
    private Double lat;             // 대여소 위도
    private Double lng;             // 대여소 경도
    private Integer totalSlots;     // 총 거치대 수
    private Integer availableBikes; // 현재 대여 가능한 자전거 수
    private String operatingHours;  // 운영 시간
    private String status;          // 대여소 상태 (ACTIVE / INACTIVE)

    public StationDetailResponse(String stationId, String stationName, String address,
                                 Double lat, Double lng, Integer totalSlots,
                                 Integer availableBikes, String operatingHours, String status) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.totalSlots = totalSlots;
        this.availableBikes = availableBikes;
        this.operatingHours = operatingHours;
        this.status = status;
    }

    public String getStationId() { return stationId; }
    public String getStationName() { return stationName; }
    public String getAddress() { return address; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public Integer getTotalSlots() { return totalSlots; }
    public Integer getAvailableBikes() { return availableBikes; }
    public String getOperatingHours() { return operatingHours; }
    public String getStatus() { return status; }
}
