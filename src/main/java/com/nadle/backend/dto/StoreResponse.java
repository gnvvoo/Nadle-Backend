package com.nadle.backend.dto;

public class StoreResponse {

    private String bizesId;
    private String bizesNm;
    private String indsSclsCdNm;
    private String indsMclsCdNm;
    private String rdnWhlAddr;
    private Double lon;
    private Double lat;

    public StoreResponse() {}

    public StoreResponse(String bizesId, String bizesNm, String indsSclsCdNm, String indsMclsCdNm,
                         String rdnWhlAddr, Double lon, Double lat) {
        this.bizesId = bizesId;
        this.bizesNm = bizesNm;
        this.indsSclsCdNm = indsSclsCdNm;
        this.indsMclsCdNm = indsMclsCdNm;
        this.rdnWhlAddr = rdnWhlAddr;
        this.lon = lon;
        this.lat = lat;
    }

    public String getBizesId() { return bizesId; }
    public String getBizesNm() { return bizesNm; }
    public String getIndsSclsCdNm() { return indsSclsCdNm; }
    public String getIndsMclsCdNm() { return indsMclsCdNm; }
    public String getRdnWhlAddr() { return rdnWhlAddr; }
    public Double getLon() { return lon; }
    public Double getLat() { return lat; }
}
