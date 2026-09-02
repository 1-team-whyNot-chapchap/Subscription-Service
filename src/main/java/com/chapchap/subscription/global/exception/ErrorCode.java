package com.chapchap.subscription.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // === Payment ===
    PAYMENT_METHOD_INVALID("PAYMENT_001", HttpStatus.BAD_REQUEST, "사용할 수 없는 자동결제수단입니다.")
    , PAYMENT_PROVIDER_AUTHENTICATION_FAILED("PAYMENT_002", HttpStatus.INTERNAL_SERVER_ERROR, "결제 서비스 연동 중 오류가 발생했습니다.")
    , PAYMENT_PROVIDER_UNAVAILABLE("PAYMENT_003", HttpStatus.BAD_GATEWAY, "결제 서비스를 일시적으로 이용할 수 없습니다.")
    , PAYMENT_METHOD_REGISTRATION_CONFLICT("PAYMENT_004", HttpStatus.CONFLICT, "자동결제수단 등록 중 상태 충돌이 발생했습니다.")

    // === Address ===
    , ADDRESS_NOT_FOUND("ADDRESS_001", HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다.")
    , ADDRESS_OUT_OF_SERVICE_AREA("ADDRESS_002", HttpStatus.BAD_REQUEST, "배송 가능 지역이 아닙니다.")
    , DEFAULT_ADDRESS_DELETE_NOT_ALLOWED("ADDRESS_003", HttpStatus.CONFLICT, "기본 배송지는 삭제할 수 없습니다.")
    , ADDRESS_IN_USE("ADDRESS_004", HttpStatus.CONFLICT, "사용 중인 배송지는 삭제할 수 없습니다.")

    // === Common ===
    , INVALID_REQUEST("COMMON_001", HttpStatus.BAD_REQUEST, "요청이 유효하지 않습니다.")
    , DATABASE_ERROR("COMMON_098", HttpStatus.INTERNAL_SERVER_ERROR, "데이터 처리 중 오류가 발생했습니다.")
    , INTERNAL_SERVER_ERROR("COMMON_099", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;
}
