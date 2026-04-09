package com.nadle.backend.dto;

public enum SpotCategory {
    TOUR,
    FOOD,
    CULTURE,
    NATURE;

    /**
     * 외부 API 응답의 contenttypeid, cat1 기준으로 카테고리를 반환한다.
     * - contenttypeid 39 → FOOD
     * - contenttypeid 14 → CULTURE
     * - contenttypeid 12 + cat1 A01 → NATURE
     * - contenttypeid 12 (기타) → TOUR
     */
    public static SpotCategory fromItem(String contentTypeId, String cat1) {
        if ("39".equals(contentTypeId)) return FOOD;
        if ("14".equals(contentTypeId)) return CULTURE;
        if ("12".equals(contentTypeId) && "A01".equals(cat1)) return NATURE;
        return TOUR;
    }
}
