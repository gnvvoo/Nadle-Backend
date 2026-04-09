package com.nadle.backend.dto;

// AI 추천 여행 코스의 개별 관광지 정보
public class SpotDto {

    private String contentId;
    private String title;
    private int sequence;
    private String reason;
    private Double mapx;
    private Double mapy;

    public SpotDto() {}

    public SpotDto(String contentId, String title, int sequence, String reason, Double mapx, Double mapy) {
        this.contentId = contentId;
        this.title = title;
        this.sequence = sequence;
        this.reason = reason;
        this.mapx = mapx;
        this.mapy = mapy;
    }

    public String getContentId() { return contentId; }
    public String getTitle() { return title; }
    public int getSequence() { return sequence; }
    public String getReason() { return reason; }
    public Double getMapx() { return mapx; }
    public Double getMapy() { return mapy; }

    public void setContentId(String contentId) { this.contentId = contentId; }
    public void setTitle(String title) { this.title = title; }
    public void setSequence(int sequence) { this.sequence = sequence; }
    public void setReason(String reason) { this.reason = reason; }
    public void setMapx(Double mapx) { this.mapx = mapx; }
    public void setMapy(Double mapy) { this.mapy = mapy; }
}
