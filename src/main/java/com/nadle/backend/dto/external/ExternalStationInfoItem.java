package com.nadle.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// 공공데이터 대여소 기본정보 API(/inf_101_00010001_v2) 응답 item 매핑
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalStationInfoItem {

    @JsonProperty("rntstnId")
    private String stationId;       // 대여소 ID

    @JsonProperty("rntstnNm")
    private String stationName;     // 대여소명

    @JsonProperty("lat")
    private Double lat;             // 위도

    @JsonProperty("lot")
    private Double lng;             // 경도 (API 필드명: lot)

    @JsonProperty("roadNmAddr")
    private String address;         // 도로명 주소

    @JsonProperty("operBgngHrCn")
    private String operStartHour;   // 운영 시작 시간

    @JsonProperty("operEndHrCn")
    private String operEndHour;     // 운영 종료 시간

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getOperStartHour() { return operStartHour; }
    public void setOperStartHour(String operStartHour) { this.operStartHour = operStartHour; }

    public String getOperEndHour() { return operEndHour; }
    public void setOperEndHour(String operEndHour) { this.operEndHour = operEndHour; }

    // 운영 시간 문자열 조합 (예: "09:00 ~ 22:00", 둘 다 없으면 "24시간")
    public String getOperatingHours() {
        if (operStartHour == null && operEndHour == null) return "24시간";
        if (operStartHour == null) return operEndHour;
        if (operEndHour == null) return operStartHour;
        return operStartHour + " ~ " + operEndHour;
    }
}
