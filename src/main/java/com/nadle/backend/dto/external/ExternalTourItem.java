package com.nadle.backend.dto.external;

public class ExternalTourItem {

    private String contentid;
    private String title;
    private String contenttypeid;
    private String addr1;
    private String firstimage;
    private String mapx;   // 경도 (longitude)
    private String mapy;   // 위도 (latitude)
    private String dist;   // 기준 좌표로부터의 거리 (m)
    private String cat1;
    private String cat2;
    private String cat3;

    public String getContentid() { return contentid; }
    public void setContentid(String contentid) { this.contentid = contentid; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContenttypeid() { return contenttypeid; }
    public void setContenttypeid(String contenttypeid) { this.contenttypeid = contenttypeid; }

    public String getAddr1() { return addr1; }
    public void setAddr1(String addr1) { this.addr1 = addr1; }

    public String getFirstimage() { return firstimage; }
    public void setFirstimage(String firstimage) { this.firstimage = firstimage; }

    public String getMapx() { return mapx; }
    public void setMapx(String mapx) { this.mapx = mapx; }

    public String getMapy() { return mapy; }
    public void setMapy(String mapy) { this.mapy = mapy; }

    public String getDist() { return dist; }
    public void setDist(String dist) { this.dist = dist; }

    public String getCat1() { return cat1; }
    public void setCat1(String cat1) { this.cat1 = cat1; }

    public String getCat2() { return cat2; }
    public void setCat2(String cat2) { this.cat2 = cat2; }

    public String getCat3() { return cat3; }
    public void setCat3(String cat3) { this.cat3 = cat3; }

    public Double getLat() { return toDouble(mapy); }
    public Double getLng() { return toDouble(mapx); }
    public Double getDistance() { return toDouble(dist); }

    private Double toDouble(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Double.parseDouble(value); }
        catch (NumberFormatException e) { return null; }
    }
}
