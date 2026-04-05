package com.nadle.backend.dto;

// 모든 API 응답에 사용되는 공통 래퍼
public class ApiResponse<T> {

    private boolean isSuccess;
    private int code;
    private String message;
    private T result;

    private ApiResponse(boolean isSuccess, int code, String message, T result) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.result = result;
    }

    public static <T> ApiResponse<T> success(String message, T result) {
        return new ApiResponse<>(true, 200, message, result);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    public boolean isSuccess() { return isSuccess; }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getResult() { return result; }
}
