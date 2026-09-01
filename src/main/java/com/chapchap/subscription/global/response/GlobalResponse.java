package com.chapchap.subscription.global.response;

import com.chapchap.subscription.global.exception.ErrorCode;

public record GlobalResponse<T>(
        String code,
        String message,
        T data
) {

    private static final String SUCCESS_CODE = "00";
    private static final String SUCCESS_MESSAGE = "SUCCESS";

    public static <T> GlobalResponse<T> success(T data) {
        return new GlobalResponse<>(
                SUCCESS_CODE,
                SUCCESS_MESSAGE,
                data
        );
    }

    public static GlobalResponse<Void> from(ErrorCode errorCode) {
        return new GlobalResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
    }
}