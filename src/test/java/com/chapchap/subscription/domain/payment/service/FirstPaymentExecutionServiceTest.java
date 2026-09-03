package com.chapchap.subscription.domain.payment.service;

import com.chapchap.subscription.domain.payment.client.AutomaticPaymentClient;
import com.chapchap.subscription.domain.payment.client.AutomaticPaymentRequest;
import com.chapchap.subscription.domain.payment.client.AutomaticPaymentResult;
import com.chapchap.subscription.domain.payment.entity.PaymentMethod;
import com.chapchap.subscription.domain.payment.entity.PaymentMethodStatus;
import com.chapchap.subscription.domain.payment.entity.PaymentProviderCode;
import com.chapchap.subscription.domain.payment.entity.PaymentTransaction;
import com.chapchap.subscription.domain.payment.entity.PaymentTransactionStatus;
import com.chapchap.subscription.domain.payment.repository.PaymentMethodRepository;
import com.chapchap.subscription.domain.payment.repository.PaymentTransactionRepository;
import com.chapchap.subscription.domain.payment.security.BillingKeyProtector;
import com.chapchap.subscription.domain.payment.service.command.FirstPaymentExecutionCommand;
import com.chapchap.subscription.domain.payment.service.exception.CurrentPaymentMethodUnavailableException;
import com.chapchap.subscription.domain.payment.service.result.FirstPaymentExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirstPaymentExecutionServiceTest {
    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;
    @Mock
    private PaymentMethodRepository paymentMethodRepository;
    @Mock
    private BillingKeyProtector billingKeyProtector;
    @Mock
    private AutomaticPaymentClient automaticPaymentClient;
    @Mock
    private ObjectProvider<AutomaticPaymentClient> automaticPaymentClientProvider;

    @InjectMocks
    private FirstPaymentExecutionService service;

    @Test
    void 현재_결제수단을_고정하고_보호값을_복호화해_외부_결제를_요청한다() {
        PaymentTransaction transaction = processingTransaction();
        PaymentMethod paymentMethod = currentPaymentMethod();
        AutomaticPaymentResult providerResult = AutomaticPaymentResult.success(
            "portone-payment-1",
            "transaction-ref-1",
            "PAID"
        );
        when(paymentTransactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(paymentMethodRepository.findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
            1L,
            PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.of(paymentMethod));
        when(billingKeyProtector.unprotect(1L, "protected-billing-key"))
            .thenReturn("decrypted-billing-key");
        when(automaticPaymentClient.pay(org.mockito.ArgumentMatchers.any()))
            .thenReturn(providerResult);
        when(automaticPaymentClientProvider.getIfAvailable()).thenReturn(automaticPaymentClient);

        FirstPaymentExecutionResult result = service.execute(
            new FirstPaymentExecutionCommand(100L, "챱챱 첫 구독 결제")
        );

        ArgumentCaptor<AutomaticPaymentRequest> requestCaptor =
            ArgumentCaptor.forClass(AutomaticPaymentRequest.class);
        verify(automaticPaymentClient).pay(requestCaptor.capture());
        AutomaticPaymentRequest request = requestCaptor.getValue();
        assertThat(request.externalPaymentId()).isEqualTo(transaction.getPublicId());
        assertThat(request.idempotencyKey()).isEqualTo("request-key-1");
        assertThat(request.externalMethodReference()).isEqualTo("decrypted-billing-key");
        assertThat(request.totalAmount()).isEqualTo(100_000L);
        assertThat(result.paymentMethodId()).isEqualTo(200L);
        assertThat(result.providerResult()).isSameAs(providerResult);
    }

    @Test
    void 현재_사용_가능한_결제수단이_없으면_외부_결제를_호출하지_않는다() {
        when(paymentTransactionRepository.findById(100L))
            .thenReturn(Optional.of(processingTransaction()));
        when(paymentMethodRepository.findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
            1L,
            PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(
            new FirstPaymentExecutionCommand(100L, "챱챱 첫 구독 결제")
        )).isInstanceOf(CurrentPaymentMethodUnavailableException.class);

        verifyNoInteractions(billingKeyProtector, automaticPaymentClient);
    }

    @Test
    void 완료된_거래는_외부_결제를_다시_호출하지_않는다() {
        PaymentTransaction transaction = processingTransaction();
        transaction.markAsSucceeded();
        when(paymentTransactionRepository.findById(100L)).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> service.execute(
            new FirstPaymentExecutionCommand(100L, "챱챱 첫 구독 결제")
        )).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(paymentMethodRepository, billingKeyProtector, automaticPaymentClient);
    }

    @Test
    void 자동결제_Client가_아직_구성되지_않으면_보호값을_복호화하지_않는다() {
        when(paymentTransactionRepository.findById(100L))
            .thenReturn(Optional.of(processingTransaction()));
        when(paymentMethodRepository.findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
            1L,
            PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.of(currentPaymentMethod()));
        when(automaticPaymentClientProvider.getIfAvailable()).thenReturn(null);

        assertThatThrownBy(() -> service.execute(
            new FirstPaymentExecutionCommand(100L, "챱챱 첫 구독 결제")
        )).isInstanceOf(IllegalStateException.class)
            .hasMessage("Automatic payment client is not configured");

        verifyNoInteractions(billingKeyProtector, automaticPaymentClient);
    }

    @Test
    void Provider_응답을_받지_못하면_거래는_PROCESSING으로_남는다() {
        PaymentTransaction transaction = processingTransaction();
        PaymentMethod paymentMethod = currentPaymentMethod();
        when(paymentTransactionRepository.findById(100L)).thenReturn(Optional.of(transaction));
        when(paymentMethodRepository.findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
            1L,
            PaymentMethodStatus.AVAILABLE
        )).thenReturn(Optional.of(paymentMethod));
        when(billingKeyProtector.unprotect(1L, "protected-billing-key"))
            .thenReturn("decrypted-billing-key");
        when(automaticPaymentClient.pay(org.mockito.ArgumentMatchers.any()))
            .thenThrow(new IllegalStateException("provider unavailable"));
        when(automaticPaymentClientProvider.getIfAvailable()).thenReturn(automaticPaymentClient);

        assertThatThrownBy(() -> service.execute(
            new FirstPaymentExecutionCommand(100L, "챱챱 첫 구독 결제")
        )).isInstanceOf(IllegalStateException.class);

        assertThat(transaction.getStatus()).isEqualTo(PaymentTransactionStatus.PROCESSING);
        assertThat(transaction.getExternalRequestIdempotencyKey()).isEqualTo("request-key-1");
        verify(paymentTransactionRepository, never()).save(transaction);
    }

    private PaymentTransaction processingTransaction() {
        PaymentTransaction transaction = PaymentTransaction.createFirstSubscriptionPayment(
            1L,
            20L,
            30L,
            100_000L,
            LocalDateTime.of(2026, 9, 3, 15, 0),
            LocalDate.of(2026, 9, 7),
            LocalDate.of(2026, 10, 4),
            "request-key-1",
            LocalDateTime.of(2026, 9, 3, 15, 0)
        );
        ReflectionTestUtils.setField(transaction, "id", 100L);
        return transaction;
    }

    private PaymentMethod currentPaymentMethod() {
        PaymentMethod paymentMethod = PaymentMethod.createAsCurrent(
            1L,
            PaymentProviderCode.PORTONE,
            "protected-billing-key",
            "신한카드",
            "****-****-****-1234",
            LocalDateTime.of(2026, 9, 3, 14, 0)
        );
        ReflectionTestUtils.setField(paymentMethod, "id", 200L);
        return paymentMethod;
    }
}
