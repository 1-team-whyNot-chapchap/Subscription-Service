package com.chapchap.subscription.domain.payment.service;

import com.chapchap.subscription.domain.payment.client.PortOnePaymentMethodClient;
import com.chapchap.subscription.domain.payment.entity.PaymentMethod;
import com.chapchap.subscription.domain.payment.entity.PaymentMethodStatus;
import com.chapchap.subscription.domain.payment.entity.PaymentProviderCode;
import com.chapchap.subscription.domain.payment.repository.PaymentMethodRepository;
import com.chapchap.subscription.domain.payment.security.BillingKeyProtector;
import com.chapchap.subscription.global.exception.ErrorCode;
import com.chapchap.subscription.global.exception.payment.PaymentMethodNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDateTime REGISTERED_AT = LocalDateTime.of(2026, 9, 2, 10, 0);

    @Mock
    private PaymentMethodRepository paymentMethodRepository;

    @Mock
    private PortOnePaymentMethodClient portOnePaymentMethodClient;

    @Mock
    private BillingKeyProtector billingKeyProtector;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private PaymentMethodService paymentMethodService;

    @Test
    void 다른_사용_가능_수단을_현재_결제수단으로_선택한다() {
        PaymentMethod currentPaymentMethod = createCurrentPaymentMethod();
        PaymentMethod selectedPaymentMethod = createAdditionalPaymentMethod();

        when(paymentMethodRepository.findByPublicIdAndUserIdAndStatusAndDeletedAtIsNull(
                selectedPaymentMethod.getPublicId()
                , USER_ID
                , PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.of(selectedPaymentMethod));
        when(paymentMethodRepository.findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
                USER_ID
                , PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.of(currentPaymentMethod));

        PaymentMethod result = paymentMethodService.selectCurrentPaymentMethod(
                USER_ID
                , selectedPaymentMethod.getPublicId()
        );

        assertThat(result).isSameAs(selectedPaymentMethod);
        assertThat(currentPaymentMethod.isCurrent()).isFalse();
        assertThat(selectedPaymentMethod.isCurrent()).isTrue();
        assertThat(selectedPaymentMethod.getLastSelectedAt()).isNotNull();
        verify(paymentMethodRepository).flush();
    }

    @Test
    void 이미_현재_수단이면_상태와_선택_시각을_변경하지_않는다() {
        PaymentMethod currentPaymentMethod = createCurrentPaymentMethod();
        LocalDateTime originalLastSelectedAt = currentPaymentMethod.getLastSelectedAt();

        when(paymentMethodRepository.findByPublicIdAndUserIdAndStatusAndDeletedAtIsNull(
                currentPaymentMethod.getPublicId()
                , USER_ID
                , PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.of(currentPaymentMethod));

        PaymentMethod result = paymentMethodService.selectCurrentPaymentMethod(
                USER_ID
                , currentPaymentMethod.getPublicId()
        );

        assertThat(result).isSameAs(currentPaymentMethod);
        assertThat(currentPaymentMethod.isCurrent()).isTrue();
        assertThat(currentPaymentMethod.getLastSelectedAt()).isEqualTo(originalLastSelectedAt);
        verify(paymentMethodRepository, never())
                .findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
                    USER_ID
                    , PaymentMethodStatus.AVAILABLE
                );
        verify(paymentMethodRepository, never()).flush();
    }

    @Test
    void 선택_가능한_결제수단이_없으면_PAYMENT_005를_반환한다() {
        String paymentMethodId = "PAY-550e8400-e29b-41d4-a716-446655440000";

        when(paymentMethodRepository.findByPublicIdAndUserIdAndStatusAndDeletedAtIsNull(
                paymentMethodId
                , USER_ID
                , PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentMethodService.selectCurrentPaymentMethod(USER_ID, paymentMethodId))
                .isInstanceOf(PaymentMethodNotFoundException.class)
                .satisfies(exception -> assertThat(((PaymentMethodNotFoundException) exception).getErrorCode())
                        .isEqualTo(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        verify(paymentMethodRepository, never())
                .findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
                    USER_ID
                    , PaymentMethodStatus.AVAILABLE
                );
        verify(paymentMethodRepository, never()).flush();
    }

    private PaymentMethod createCurrentPaymentMethod() {
        return PaymentMethod.createAsCurrent(
                USER_ID
                , PaymentProviderCode.PORTONE
                , "protected-current"
                , "신한카드"
                , "****-****-****-1234"
                , REGISTERED_AT
        );
    }

    private PaymentMethod createAdditionalPaymentMethod() {
        return PaymentMethod.createAsAdditional(
                USER_ID
                , PaymentProviderCode.PORTONE
                , "protected-additional"
                , "국민카드"
                , "****-****-****-5678"
                , REGISTERED_AT
        );
    }
}
