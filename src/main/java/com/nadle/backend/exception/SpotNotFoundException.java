package com.nadle.backend.exception;

public class SpotNotFoundException extends RuntimeException {

    public SpotNotFoundException(String spotId) {
        super("관광지를 찾을 수 없습니다. spotId: " + spotId);
    }
}
