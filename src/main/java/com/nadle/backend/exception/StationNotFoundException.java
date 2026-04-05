package com.nadle.backend.exception;

// 존재하지 않는 대여소 ID 요청 시 발생하는 예외
public class StationNotFoundException extends RuntimeException {

    public StationNotFoundException(String stationId) {
        super("대여소를 찾을 수 없습니다. stationId: " + stationId);
    }
}
