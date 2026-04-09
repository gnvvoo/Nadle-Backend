package com.nadle.backend.dto;

public class SpotResponse {

    private String spotId;
    private String spotName;
    private SpotCategory category;
    private String address;
    private Double lat;
    private Double lng;
    private String imageUrl;
    private Double distance;

    public SpotResponse(String spotId, String spotName, SpotCategory category,
                        String address, Double lat, Double lng,
                        String imageUrl, Double distance) {
        this.spotId = spotId;
        this.spotName = spotName;
        this.category = category;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.imageUrl = imageUrl;
        this.distance = distance;
    }

    public String getSpotId() { return spotId; }
    public String getSpotName() { return spotName; }
    public SpotCategory getCategory() { return category; }
    public String getAddress() { return address; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public String getImageUrl() { return imageUrl; }
    public Double getDistance() { return distance; }
}
