package com.nadle.backend.dto;

import java.util.List;

public class SpotListResponse {

    private int page;
    private int size;
    private int totalElements;
    private int totalPages;
    private List<SpotResponse> spots;

    public SpotListResponse(int page, int size, int totalElements, List<SpotResponse> spots) {
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
        this.spots = spots;
    }

    public int getPage() { return page; }
    public int getSize() { return size; }
    public int getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public List<SpotResponse> getSpots() { return spots; }
}
