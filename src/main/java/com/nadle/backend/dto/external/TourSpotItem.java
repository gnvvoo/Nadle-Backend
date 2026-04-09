package com.nadle.backend.dto.external;

// 한국관광공사 locationBasedList API 응답의 관광지 항목
public class TourSpotItem {

    private String contentId;
    private String title;
    private Double mapx;
    private Double mapy;
    private String addr1;

    public TourSpotItem() {}

    public String getContentId() { return contentId; }
    public String getTitle() { return title; }
    public Double getMapx() { return mapx; }
    public Double getMapy() { return mapy; }
    public String getAddr1() { return addr1; }

    public void setContentId(String contentId) { this.contentId = contentId; }
    public void setTitle(String title) { this.title = title; }
    public void setMapx(Double mapx) { this.mapx = mapx; }
    public void setMapy(Double mapy) { this.mapy = mapy; }
    public void setAddr1(String addr1) { this.addr1 = addr1; }
}
