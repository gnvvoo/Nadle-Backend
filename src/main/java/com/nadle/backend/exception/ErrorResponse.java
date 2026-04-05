package com.nadle.backend.exception;

// 에러 응답 DTO
public class ErrorResponse {

    private boolean isSuccess;
    private int code;
    private String message;

    public ErrorResponse(int code, String message) {
        this.isSuccess = false;
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() { return isSuccess; }
    public int getCode() { return code; }
    public String getMessage() { return message; }
}
