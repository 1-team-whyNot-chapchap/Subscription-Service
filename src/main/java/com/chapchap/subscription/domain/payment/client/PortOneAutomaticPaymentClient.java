package com.chapchap.subscription.domain.payment.client;

import com.chapchap.subscription.global.exception.payment.PaymentProviderAuthenticationFailedException;
import com.chapchap.subscription.global.exception.payment.PaymentProviderUnavailableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;

/** PortOne V2 빌링키 결제 API를 Provider 중립 자동결제 계약에 연결한다. */
@Component
public class PortOneAutomaticPaymentClient implements AutomaticPaymentClient {
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String PAID_STATUS = "PAID";
    private static final String FAILED_STATUS = "FAILED";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(60);
    private static final String GENERIC_DECLINE_REASON = "외부 결제 승인이 거절되었습니다.";

    private final RestClient restClient;

    // ========= [TODO: SUB-FN-004 / PG별 결제 요청 계약] =========
    // 이유: 현재 Source에는 실제 사용할 PG·채널과 필수 고객정보 계약이 확정되어 있지 않다.
    // 완료 조건: 팀이 실제 PG·채널과 customer 필드별 값의 출처·외부 식별자 정책을 확정한다.
    // 후속 작업: 필요한 고객정보만 AutomaticPaymentRequest와 PortOne 요청 DTO에 추가하고 Mock·실결제로 검증한다.
    // 검토 사항: 내부 사용자 PK를 근거 없이 외부 customer.id로 전송하거나 개인정보를 일반 로그에 남기지 않는다.
    // ========= [/TODO] =============================================

    // ========= [TODO: SUB-FN-004 / PortOne 오류 분류 계약] =========
    // 이유: 현재 HTTP 오류는 인증 실패 외에는 결과 불명확 오류로 보수적으로 처리한다.
    // 완료 조건: PortOne 오류 type별 명시적 실패·결과 불명확 구분과 내부 상태 전이 계약을 확정한다.
    // 후속 작업: 확정된 type만 실패 결과로 변환하고 나머지는 PROCESSING을 유지하도록 예외를 분리한다.
    // 검토 사항: 409 재요청 결과와 502 PG 오류를 실제 결제 실패로 섣불리 확정하지 않는다.
    // ========= [/TODO] =============================================

    /** PortOne V2 주소·Secret과 공식 권장 timeout으로 실제 HTTP Client를 구성한다. */
    @Autowired
    public PortOneAutomaticPaymentClient(
        RestClient.Builder restClientBuilder,
        @Value("${portone.api.base-url}") String baseUrl,
        @Value("${portone.api.secret}") String apiSecret
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = restClientBuilder
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret)
            .requestFactory(requestFactory)
            .build();
    }

    /** 테스트에서 외부 네트워크 없이 요청·응답 계약을 검증하기 위한 생성자다. */
    PortOneAutomaticPaymentClient(RestClient restClient) {
        this.restClient = restClient;
    }

    /** 빌링키 결제를 요청하고 PortOne의 확정 상태를 내부 성공·거절 결과로 변환한다. */
    @Override
    public AutomaticPaymentResult pay(AutomaticPaymentRequest request) {
        try {
            PortOneBillingKeyPaymentResponse response = restClient.post()
                .uri("/payments/{paymentId}/billing-key", request.externalPaymentId())
                .header(IDEMPOTENCY_KEY_HEADER, quoteStructuredField(request.idempotencyKey()))
                .body(PortOneBillingKeyPaymentRequest.from(request))
                .retrieve()
                .body(PortOneBillingKeyPaymentResponse.class);

            return toAutomaticPaymentResult(request, response);
        } catch (RestClientResponseException exception) {
            throw mapProviderError(exception);
        } catch (ResourceAccessException exception) {
            throw new PaymentProviderUnavailableException();
        } catch (RestClientException exception) {
            throw new PaymentProviderUnavailableException();
        }
    }

    private AutomaticPaymentResult toAutomaticPaymentResult(
        AutomaticPaymentRequest request,
        PortOneBillingKeyPaymentResponse response
    ) {
        if (response == null || response.payment() == null) {
            throw new PaymentProviderUnavailableException();
        }

        PortOneBillingKeyPaymentResponse.Payment payment = response.payment();
        if (!request.externalPaymentId().equals(payment.id())) {
            throw new PaymentProviderUnavailableException();
        }
        if (PAID_STATUS.equals(payment.status())) {
            return AutomaticPaymentResult.success(
                payment.id(),
                payment.transactionId(),
                PAID_STATUS
            );
        }
        if (FAILED_STATUS.equals(payment.status())) {
            return AutomaticPaymentResult.declined(
                payment.id(),
                failureCode(payment.failure()),
                sanitizedFailureReason(payment.failure(), request.externalMethodReference())
            );
        }
        throw new PaymentProviderUnavailableException();
    }

    private RuntimeException mapProviderError(RestClientResponseException exception) {
        HttpStatusCode status = exception.getStatusCode();
        if (status.isSameCodeAs(HttpStatus.UNAUTHORIZED)
            || status.isSameCodeAs(HttpStatus.FORBIDDEN)) {
            return new PaymentProviderAuthenticationFailedException();
        }
        return new PaymentProviderUnavailableException();
    }

    private String failureCode(PortOneBillingKeyPaymentResponse.Failure failure) {
        if (failure == null || failure.pgCode() == null || failure.pgCode().isBlank()) {
            return FAILED_STATUS;
        }
        return failure.pgCode();
    }

    private String sanitizedFailureReason(
        PortOneBillingKeyPaymentResponse.Failure failure,
        String billingKey
    ) {
        if (failure == null) {
            return GENERIC_DECLINE_REASON;
        }
        String reason = firstNonBlank(failure.reason(), failure.pgMessage());
        if (reason == null) {
            return GENERIC_DECLINE_REASON;
        }
        return reason.replace(billingKey, "***");
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second != null && !second.isBlank() ? second : null;
    }

    private String quoteStructuredField(String value) {
        return '"' + value + '"';
    }
}
