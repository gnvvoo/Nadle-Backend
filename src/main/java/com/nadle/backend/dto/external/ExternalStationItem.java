package com.nadle.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// 공공데이터 자전거 대여가능 현황 API 응답 item 매핑
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalStationItem {

    private String stationId;
    private String stationName;
    private String latRaw;
    private String lngRaw;
    private String bikeTotCntRaw;

    public void setStationId(String stationId) { this.stationId = stationId; }
    public void setStationName(String stationName) { this.stationName = stationName; }
    public void setLatRaw(String latRaw) { this.latRaw = latRaw; }
    public void setLngRaw(String lngRaw) { this.lngRaw = lngRaw; }
    public void setBikeTotCntRaw(String bikeTotCntRaw) { this.bikeTotCntRaw = bikeTotCntRaw; }

    public String getStationId() { return stationId; }
    public String getStationName() { return stationName; }

    public Double getLat() {
        try { return latRaw != null ? Double.parseDouble(latRaw) : null; }
        catch (NumberFormatException e) { return null; }
    }

    public Double getLng() {
        try { return lngRaw != null ? Double.parseDouble(lngRaw) : null; }
        catch (NumberFormatException e) { return null; }
    }

    public Integer getBikeTotCnt() {
        try { return bikeTotCntRaw != null ? Integer.parseInt(bikeTotCntRaw) : null; }
        catch (NumberFormatException e) { return null; }
    }
}
