package com.nadle.backend.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 필수 쿼리 파라미터 누락 시 처리
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException e) {
        String message = String.format("필수 파라미터가 누락되었습니다: %s", e.getParameterName());
        return ResponseEntity.badRequest().body(new ErrorResponse(400, message));
    }

    /**
     * 파라미터 유효성 검증 실패 시 처리
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(400, "요청 파라미터가 올바르지 않습니다."));
    }

    /**
     * 존재하지 않는 대여소 ID 요청 시 처리
     */
    @ExceptionHandler(StationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStationNotFound(StationNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, e.getMessage()));
    }

    /**
     * 존재하지 않는 관광지 ID 요청 시 처리
     */
    @ExceptionHandler(SpotNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSpotNotFound(SpotNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, e.getMessage()));
    }

    /**
     * 외부 API 호출 실패 시 처리
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException e) {
        log.error("외부 API 호출 실패: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse(502, "외부 API 호출에 실패했습니다."));
    }

    /**
     * AI 추천 및 외부 서비스 처리 실패 시 처리
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("처리 중 오류 발생: {}", e.getMessage());
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(500, e.getMessage()));
    }

    /**
     * 그 외 서버 내부 오류 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("예상치 못한 오류 발생: {} - {}", e.getClass().getName(), e.getMessage(), e);
        return ResponseEntity.internalServerError()
                .body(new ErrorResponse(500, "서버 내부 오류가 발생했습니다."));
    }
}
