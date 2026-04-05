package com.nadle.backend.dto;

// 자전거 대여소 응답 DTO
public class StationResponse {

    private String stationId;       // 대여소 고유 ID
    private String stationName;     // 대여소 명칭
    private Double lat;             // 대여소 위도
    private Double lng;             // 대여소 경도
    private Integer rackTotCnt;     // 총 거치대 수
    private Integer parkingBikeTotCnt; // 현재 대여 가능 자전거 수
    private Double distance;        // 현재 위치에서의 거리(m)

    public StationResponse(String stationId, String stationName, Double lat, Double lng,
                           Integer rackTotCnt, Integer parkingBikeTotCnt, Double distance) {
        this.stationId = stationId;
        this.stationName = stationName;
        this.lat = lat;
        this.lng = lng;
        this.rackTotCnt = rackTotCnt;
        this.parkingBikeTotCnt = parkingBikeTotCnt;
        this.distance = distance;
    }

    public String getStationId() { return stationId; }
    public String getStationName() { return stationName; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public Integer getRackTotCnt() { return rackTotCnt; }
    public Integer getParkingBikeTotCnt() { return parkingBikeTotCnt; }
    public Double getDistance() { return distance; }
}
