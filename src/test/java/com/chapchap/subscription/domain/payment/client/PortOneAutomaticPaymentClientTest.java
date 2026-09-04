package com.chapchap.subscription.domain.payment.client;

import com.chapchap.subscription.global.exception.payment.PaymentProviderUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class PortOneAutomaticPaymentClientTest {
    private static final String BASE_URL = "https://api.portone.test";
    private static final String API_SECRET = "test-api-secret";
    private static final String BILLING_KEY = "test-sensitive-billing-key";
    private static final String PAYMENT_ID = "PAY-test-payment";
    private static final String IDEMPOTENCY_KEY = "FIRST-PAYMENT-test-key-0001";

    private MockRestServiceServer server;
    private PortOneAutomaticPaymentClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + API_SECRET);
        server = MockRestServiceServer.bindTo(builder).build();
        client = new PortOneAutomaticPaymentClient(builder.build());
    }

    @Test
    void PortOne_빌링키_결제_요청과_멱등성_헤더를_전송하고_PAID를_변환한다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "PortOne " + API_SECRET))
            .andExpect(header("Idempotency-Key", '"' + IDEMPOTENCY_KEY + '"'))
            .andExpect(content().json("""
                {
                  "billingKey": "test-sensitive-billing-key",
                  "orderName": "챱챱 첫 구독 결제",
                  "amount": {"total": 100000},
                  "currency": "KRW"
                }
                """))
            .andRespond(withSuccess("""
                {
                  "payment": {
                    "status": "PAID",
                    "id": "PAY-test-payment",
                    "transactionId": "portone-transaction-1"
                  }
                }
                """, MediaType.APPLICATION_JSON));

        AutomaticPaymentResult result = client.pay(request());

        assertThat(result.status()).isEqualTo(AutomaticPaymentStatus.PAID);
        assertThat(result.externalTransactionRef()).isEqualTo("portone-transaction-1");
        assertThat(result.failureReason()).isNull();
        server.verify();
    }

    @Test
    void FAILED_응답은_거절로_변환하고_실패_사유의_빌링키를_제거한다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withSuccess("""
                {
                  "payment": {
                    "status": "FAILED",
                    "id": "PAY-test-payment",
                    "transactionId": "failed-transaction-1",
                    "failure": {
                      "reason": "test-sensitive-billing-key 카드 승인이 거절되었습니다.",
                      "pgCode": "DECLINED",
                      "pgMessage": "승인 거절"
                    }
                  }
                }
                """, MediaType.APPLICATION_JSON));

        AutomaticPaymentResult result = client.pay(request());

        assertThat(result.status()).isEqualTo(AutomaticPaymentStatus.DECLINED);
        assertThat(result.externalResultCode()).isEqualTo("DECLINED");
        assertThat(result.failureReason()).doesNotContain(BILLING_KEY).doesNotContain("승인 거절");
        assertThat(result.externalTransactionRef()).isNull();
        server.verify();
    }

    @Test
    void 인증_실패는_저장_가능한_서버_연동_실패_결과로_변환한다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"type\":\"UNAUTHORIZED\",\"message\":\"secret response\"}"));

        AutomaticPaymentResult result = client.pay(request());

        assertThat(result.status()).isEqualTo(AutomaticPaymentStatus.PROVIDER_CONFIGURATION_FAILED);
        assertThat(result.externalResultCode()).isEqualTo("HTTP_401");
        assertThat(result.failureReason()).doesNotContain("secret response").doesNotContain(BILLING_KEY);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "BILLING_KEY_NOT_FOUND",
        "BILLING_KEY_ALREADY_DELETED",
        "BillingKeyNotFoundError",
        "BillingKeyAlreadyDeletedError"
    })
    void 사용할_수_없는_빌링키_오류는_명시적_결제_거절로_변환한다(String errorType) {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"type\":\"" + errorType + "\",\"message\":\"provider detail\"}"));

        AutomaticPaymentResult result = client.pay(request());

        assertThat(result.status()).isEqualTo(AutomaticPaymentStatus.DECLINED);
        assertThat(result.externalResultCode()).isEqualTo(errorType);
        assertThat(result.failureReason()).doesNotContain("provider detail");
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "INVALID_REQUEST",
        "CHANNEL_NOT_FOUND",
        "InvalidRequestError",
        "ChannelNotFoundError"
    })
    void 서버가_만든_요청과_채널_설정_오류는_서버_연동_실패로_변환한다(String errorType) {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"type\":\"" + errorType + "\",\"message\":\"provider detail\"}"));

        AutomaticPaymentResult result = client.pay(request());

        assertThat(result.status()).isEqualTo(AutomaticPaymentStatus.PROVIDER_CONFIGURATION_FAILED);
        assertThat(result.externalResultCode()).isEqualTo(errorType);
        assertThat(result.failureReason()).doesNotContain("provider detail");
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ALREADY_PAID",
        "PG_PROVIDER",
        "AlreadyPaidError",
        "PgProviderError"
    })
    void 승인_여부를_확정할_수_없는_오류는_처리_대기_예외로_변환한다(String errorType) {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withStatus(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"type\":\"" + errorType + "\",\"message\":\"provider detail\"}"));

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class)
            .hasMessageNotContaining("provider detail");
        server.verify();
    }

    @Test
    void 요청_제한_응답은_자동_재시도하지_않고_처리_대기_예외로_변환한다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class);
        server.verify();
    }

    @Test
    void 연결_실패는_결과를_확정하지_않고_처리_대기_예외로_변환한다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(request -> {
                throw new ResourceAccessException("simulated connection failure");
            });

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class)
            .hasMessageNotContaining("simulated connection failure");
        server.verify();
    }

    @Test
    void 오류_Body를_파싱할_수_없으면_결과를_추측하지_않는다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("not-json"));

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class);
        server.verify();
    }

    @Test
    void 서버_오류는_일시적_사용불가_예외로_변환한다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withStatus(HttpStatus.BAD_GATEWAY));

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class);
        server.verify();
    }

    @Test
    void 알_수_없는_결제_상태는_성공이나_거절로_추측하지_않는다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withSuccess("""
                {
                  "payment": {
                    "status": "PENDING",
                    "id": "PAY-test-payment",
                    "transactionId": null
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class);
        server.verify();
    }

    @Test
    void 응답_결제_ID가_요청과_다르면_결과를_확정하지_않는다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withSuccess("""
                {
                  "payment": {
                    "status": "PAID",
                    "id": "different-payment-id",
                    "transactionId": "portone-transaction-1"
                  }
                }
                """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class);
        server.verify();
    }

    @Test
    void 성공_HTTP에_결제_결과가_없으면_결과를_확정하지_않는다() {
        server.expect(requestTo(BASE_URL + "/payments/" + PAYMENT_ID + "/billing-key"))
            .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.pay(request()))
            .isInstanceOf(PaymentProviderUnavailableException.class);
        server.verify();
    }

    private AutomaticPaymentRequest request() {
        return new AutomaticPaymentRequest(
            PAYMENT_ID,
            IDEMPOTENCY_KEY,
            BILLING_KEY,
            "챱챱 첫 구독 결제",
            100_000L,
            "KRW"
        );
    }
}
