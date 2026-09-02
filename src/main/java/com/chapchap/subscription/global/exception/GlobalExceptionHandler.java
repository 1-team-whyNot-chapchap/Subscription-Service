package com.chapchap.subscription.global.exception;

import com.chapchap.subscription.global.response.GlobalResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<GlobalResponse<Void>> generateErrorResponse(
        ErrorCode errorCode
    ) {
        return ResponseEntity
            .status(errorCode.getHttpStatus())
            .body(GlobalResponse.from(errorCode));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<GlobalResponse<Void>> handleBusinessException(
        BusinessException e
    ) {
        return generateErrorResponse(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalResponse<Void>> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e
    ) {
        return generateErrorResponse(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalResponse<Void>> handleHttpMessageNotReadableException(
        HttpMessageNotReadableException e
    ) {
        return generateErrorResponse(ErrorCode.INVALID_REQUEST);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<GlobalResponse<Void>> handleDataAccessException(
        DataAccessException e
    ) {
        log.error(
            "Database error. errorCode={}"
            , ErrorCode.DATABASE_ERROR.getCode()
            , e
        );
        return generateErrorResponse(ErrorCode.DATABASE_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalResponse<Void>> handleException(
        Exception e
    ) {
        log.error(
            "Unexpected server error. errorCode={}"
            , ErrorCode.INTERNAL_SERVER_ERROR.getCode()
            , e
        );
        return generateErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR
        );
    }
}
