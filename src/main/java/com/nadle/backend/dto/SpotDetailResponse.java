package com.nadle.backend.dto;

import java.util.List;

public class SpotDetailResponse {

    private String spotId;
    private String spotName;
    private String description;
    private SpotCategory category;
    private String address;
    private Double lat;
    private Double lng;
    private List<String> images;
    private String tel;

    public SpotDetailResponse(String spotId, String spotName, String description,
                              SpotCategory category, String address,
                              Double lat, Double lng,
                              List<String> images, String tel) {
        this.spotId = spotId;
        this.spotName = spotName;
        this.description = description;
        this.category = category;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.images = images;
        this.tel = tel;
    }

    public String getSpotId() { return spotId; }
    public String getSpotName() { return spotName; }
    public String getDescription() { return description; }
    public SpotCategory getCategory() { return category; }
    public String getAddress() { return address; }
    public Double getLat() { return lat; }
    public Double getLng() { return lng; }
    public List<String> getImages() { return images; }
    public String getTel() { return tel; }
}
