package com.nadle.backend.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// 공공데이터 포털 API 공통 응답 구조 매핑
// 실제 응답: { "header": {...}, "body": { "item": [...] } }
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExternalApiResponse {

    private Header header;
    private Body body;

    public Header getHeader() { return header; }
    public void setHeader(Header header) { this.header = header; }
    public Body getBody() { return body; }
    public void setBody(Body body) { this.body = body; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;

        public String getResultCode() { return resultCode; }
        public void setResultCode(String resultCode) { this.resultCode = resultCode; }
        public String getResultMsg() { return resultMsg; }
        public void setResultMsg(String resultMsg) { this.resultMsg = resultMsg; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        @JsonProperty("item")
        private List<ExternalStationItem> items;  // 응답 필드명은 "item" (배열)

        private int totalCount;

        public List<ExternalStationItem> getItems() { return items; }
        public void setItems(List<ExternalStationItem> items) { this.items = items; }
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
    }
}
