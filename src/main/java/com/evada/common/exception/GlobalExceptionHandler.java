package com.evada.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

/**
 * 모든 API 오류를 {"code": "...", "message": "..."} 형태로 통일한다.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        return ResponseEntity.status(code.getStatus())
                .body(new ErrorResponse(code.name(), code.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null
                ? ErrorCode.INVALID_REQUEST.getMessage()
                : fieldError.getField() + ": " + fieldError.getDefaultMessage();
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.INVALID_REQUEST.name(), message));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            HandlerMethodValidationException.class})
    public ResponseEntity<ErrorResponse> handleUnreadable(Exception e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.INVALID_REQUEST.name(), ErrorCode.INVALID_REQUEST.getMessage()));
    }

    // 기존 관리자 코드(ResponseStatusException)도 같은 형태로 응답
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException e) {
        HttpStatus status = HttpStatus.valueOf(e.getStatusCode().value());
        String message = e.getReason() == null ? status.getReasonPhrase() : e.getReason();
        return ResponseEntity.status(status).body(new ErrorResponse(status.name(), message));
    }
}
