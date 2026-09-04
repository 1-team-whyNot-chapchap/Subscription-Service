package com.chapchap.subscription.domain.payment.client;

/**
 * 구독 결제 업무 코드가 특정 외부 결제 제공자의 API 형식에 의존하지 않도록 분리한 자동결제 경계다.
 */
public interface AutomaticPaymentClient {
    /**
     * 요청 시점에 확정한 결제수단 참조값과 금액으로 자동결제를 요청한다.
     *
     * <p>구현체는 제공자 응답을 내부 공통 결과로 변환해야 하며, 결제수단 참조값과
     * API Secret을 로그나 예외 메시지에 노출해서는 안 된다.</p>
     *
     * @param request 외부 요청 식별자·결제수단 참조값·주문명·금액·통화가 확정된 요청
     * @return 외부 제공자 응답을 성공 또는 실패로 변환한 공통 결과
     */
    AutomaticPaymentResult pay(AutomaticPaymentRequest request);
}
