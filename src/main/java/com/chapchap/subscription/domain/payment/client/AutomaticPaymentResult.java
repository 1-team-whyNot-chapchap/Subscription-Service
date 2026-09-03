package com.chapchap.subscription.domain.payment.client;

/**
 * 외부 결제 제공자의 응답을 내부 결제 처리에서 공통으로 사용할 수 있게 변환한 결과다.
 *
 * @param success 외부 결제 성공 여부
 * @param externalPaymentId 외부 결제 건 식별자
 * @param externalTransactionRef 성공한 외부 거래의 처리 식별정보
 * @param externalResultCode 외부 제공자가 반환한 결과 코드
 * @param failureReason 외부 결제 실패 사유
 */
public record AutomaticPaymentResult(
    boolean success,
    String externalPaymentId,
    String externalTransactionRef,
    String externalResultCode,
    String failureReason
) {
    // ========= [TODO: SUB-FN-004 / PortOne Client 단계] =========
    // 이유: 현재 success=false는 PG가 명시한 결제 거절을 임시로 표현한다.
    // 완료 조건: PortOne 실제 응답 및 오류 유형이 확정된다.
    // 후속 작업: 결제 거절은 DECLINED로 표현하고,
    //            인증·통신·서버 오류는 전용 예외로 분리한다.
    // 검토 사항: 정상 응답 종류가 늘어나면 boolean을 enum으로 변경한다.
    // ========= [/TODO] ==========================================

    /**
     * 외부 결제 성공 결과를 생성하며 외부 거래 식별정보를 필수로 보존한다.
     *
     * @param externalPaymentId 외부 결제 건 식별자
     * @param externalTransactionRef 성공한 외부 거래의 처리 식별정보
     * @param externalResultCode 외부 제공자가 반환한 결과 코드
     * @return 성공 상태의 공통 자동결제 결과
     */
    public static AutomaticPaymentResult success(
        String externalPaymentId,
        String externalTransactionRef,
        String externalResultCode
    ) {
        requireText(externalPaymentId, "externalPaymentId");
        requireText(externalTransactionRef, "externalTransactionRef");
        return new AutomaticPaymentResult(
            true,
            externalPaymentId,
            externalTransactionRef,
            externalResultCode,
            null
        );
    }

    /**
     * 외부 결제 실패 결과를 생성하며 성공 거래 식별정보 대신 정제된 실패 사유를 보존한다.
     *
     * @param externalPaymentId 외부 결제 건 식별자
     * @param externalResultCode 외부 제공자가 반환한 결과 코드
     * @param failureReason Secret과 결제수단 참조값을 제거한 실패 사유
     * @return 실패 상태의 공통 자동결제 결과
     */
    public static AutomaticPaymentResult failure(
        String externalPaymentId,
        String externalResultCode,
        String failureReason
    ) {
        requireText(failureReason, "failureReason");
        return new AutomaticPaymentResult(
            false,
            externalPaymentId,
            null,
            externalResultCode,
            failureReason
        );
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
