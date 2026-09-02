package com.chapchap.subscription.domain.payment;

import com.chapchap.subscription.domain.payment.client.PaymentMethodVerificationResult;
import com.chapchap.subscription.domain.payment.client.PortOnePaymentMethodClient;
import com.chapchap.subscription.domain.payment.entity.PaymentMethod;
import com.chapchap.subscription.domain.payment.entity.PaymentMethodStatus;
import com.chapchap.subscription.domain.payment.entity.PaymentProviderCode;
import com.chapchap.subscription.domain.payment.repository.PaymentMethodRepository;
import com.chapchap.subscription.domain.payment.security.BillingKeyProtector;
import com.chapchap.subscription.global.exception.payment.PaymentMethodInvalidException;
import com.chapchap.subscription.global.exception.payment.PaymentMethodRegistrationConflictException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final String CURRENT_PAYMENT_METHOD_UNIQUE_CONSTRAINT = "uk_payment_methods_current_user_id";

    private final PaymentMethodRepository paymentMethodRepository;
    private final PortOnePaymentMethodClient portOnePaymentMethodClient;
    private final BillingKeyProtector billingKeyProtector;
    private final TransactionTemplate transactionTemplate;

    public PaymentMethod registerPaymentMethod(Long userId, String billingKey) {
        PaymentMethodVerificationResult verificationResult = portOnePaymentMethodClient.verifyBillingKey(billingKey);

        if (!verificationResult.valid()) {
            throw new PaymentMethodInvalidException();
        }

        String protectedExternalMethodRef = billingKeyProtector.protect(userId, billingKey);

        try {
            return transactionTemplate.execute(
                status -> registerVerifiedPaymentMethod(
                        userId
                        , protectedExternalMethodRef
                        , verificationResult
                )
            );
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException cause
                    && CURRENT_PAYMENT_METHOD_UNIQUE_CONSTRAINT.equals(cause.getConstraintName())
            ) {
                throw new PaymentMethodRegistrationConflictException();
            }
            throw e;
        }
    }

    private PaymentMethod registerVerifiedPaymentMethod(
        Long userId
        , String protectedExternalMethodRef
        , PaymentMethodVerificationResult verificationResult
    ) {
        boolean hasAvailablePaymentMethod = paymentMethodRepository.existsByUserIdAndStatus(userId, PaymentMethodStatus.AVAILABLE);

        LocalDateTime registeredAt = LocalDateTime.now(BUSINESS_ZONE_ID);

        String cardCompany = verificationResult.cardCompany();
        String maskedCardNumber = verificationResult.maskedCardNumber();

        PaymentMethod paymentMethod;

        if (hasAvailablePaymentMethod) {
            paymentMethod = PaymentMethod.createAsAdditional(
                userId
                , PaymentProviderCode.PORTONE
                , protectedExternalMethodRef
                , cardCompany
                , maskedCardNumber
                , registeredAt
            );
        } else {
            paymentMethod = PaymentMethod.createAsCurrent(
                userId
                , PaymentProviderCode.PORTONE
                , protectedExternalMethodRef
                , cardCompany
                , maskedCardNumber
                , registeredAt
            );
        }
        return paymentMethodRepository.save(paymentMethod);
    }
}