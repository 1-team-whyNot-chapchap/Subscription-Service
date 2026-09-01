package com.chapchap.subscription.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_REQUEST(
            "COMMON_001",
            HttpStatus.BAD_REQUEST,
            "잘못된 요청입니다."
    ),

    DATABASE_ERROR(
            "COMMON_098",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "데이터 처리 중 오류가 발생했습니다."
    ),

    INTERNAL_SERVER_ERROR(
            "COMMON_099",
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다."
    ),

    ADDRESS_NOT_FOUND(
            "ADDRESS_001",
            HttpStatus.NOT_FOUND,
            "배송지를 찾을 수 없습니다."
    ),

    ADDRESS_OUT_OF_SERVICE_AREA(
            "ADDRESS_002",
            HttpStatus.BAD_REQUEST,
            "배송 가능 지역이 아닙니다."
    ),

    DEFAULT_ADDRESS_DELETE_NOT_ALLOWED(
            "ADDRESS_003",
            HttpStatus.CONFLICT,
            "기본 배송지는 삭제할 수 없습니다."
    ),

    ADDRESS_IN_USE(
            "ADDRESS_004",
            HttpStatus.CONFLICT,
            "사용 중인 배송지는 삭제할 수 없습니다."
    );

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(
            String code,
            HttpStatus httpStatus,
            String message
    ) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
