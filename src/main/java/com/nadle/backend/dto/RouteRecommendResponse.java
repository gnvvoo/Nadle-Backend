package com.nadle.backend.dto;

import java.util.List;

// AI 여행 코스 추천 응답 DTO
public class RouteRecommendResponse {

    private int duration;
    private String aiSummary;
    private List<SpotDto> spots;

    public RouteRecommendResponse() {}

    public RouteRecommendResponse(int duration, String aiSummary, List<SpotDto> spots) {
        this.duration = duration;
        this.aiSummary = aiSummary;
        this.spots = spots;
    }

    public int getDuration() { return duration; }
    public String getAiSummary() { return aiSummary; }
    public List<SpotDto> getSpots() { return spots; }

    public void setDuration(int duration) { this.duration = duration; }
    public void setAiSummary(String aiSummary) { this.aiSummary = aiSummary; }
    public void setSpots(List<SpotDto> spots) { this.spots = spots; }
}
