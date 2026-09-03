package com.chapchap.subscription.domain.payment.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AutomaticPaymentContractTest {
    @Test
    void convertsProviderNeutralRequestToPortOneBody() {
        AutomaticPaymentRequest request = new AutomaticPaymentRequest(
            "external-payment-1",
            "idempotency-key-1",
            "test-external-method-ref",
            "챱챱 첫 구독 결제",
            100_000L,
            "KRW"
        );

        PortOneBillingKeyPaymentRequest body = PortOneBillingKeyPaymentRequest.from(request);

        assertEquals("test-external-method-ref", body.billingKey());
        assertEquals("챱챱 첫 구독 결제", body.orderName());
        assertEquals(100_000L, body.amount().total());
        assertEquals("KRW", body.currency());
    }

    @Test
    void successAndFailureResultsKeepDifferentFields() {
        AutomaticPaymentResult success = AutomaticPaymentResult.success(
            "external-payment-1",
            "external-transaction-1",
            "PAID"
        );
        AutomaticPaymentResult failure = AutomaticPaymentResult.failure(
            "external-payment-2",
            "PAYMENT_FAILED",
            "카드 승인이 거절되었습니다."
        );

        assertNull(success.failureReason());
        assertNull(failure.externalTransactionRef());
    }

    @Test
    void nonPositivePaymentAmountIsRejected() {
        assertThrows(
            IllegalArgumentException.class,
            () -> new AutomaticPaymentRequest(
                "external-payment-1",
                "idempotency-key-1",
                "test-external-method-ref",
                "챱챱 첫 구독 결제",
                0L,
                "KRW"
            )
        );
    }

    @Test
    void sensitiveMethodReferenceIsRedactedFromStringRepresentations() {
        AutomaticPaymentRequest request = new AutomaticPaymentRequest(
            "external-payment-1",
            "idempotency-key-1",
            "test-sensitive-method-reference",
            "챱챱 첫 구독 결제",
            100_000L,
            "KRW"
        );
        PortOneBillingKeyPaymentRequest body = PortOneBillingKeyPaymentRequest.from(request);

        org.junit.jupiter.api.Assertions.assertFalse(request.toString().contains("test-sensitive-method-reference"));
        org.junit.jupiter.api.Assertions.assertFalse(body.toString().contains("test-sensitive-method-reference"));
    }
}
