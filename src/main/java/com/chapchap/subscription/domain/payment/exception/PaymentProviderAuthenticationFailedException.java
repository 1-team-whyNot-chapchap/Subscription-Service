package com.chapchap.subscription.domain.payment.exception;

import com.chapchap.subscription.global.exception.BusinessException;
import com.chapchap.subscription.global.exception.ErrorCode;

public class PaymentProviderAuthenticationFailedException extends BusinessException {

    public PaymentProviderAuthenticationFailedException() {
        super(ErrorCode.PAYMENT_PROVIDER_AUTHENTICATION_FAILED);
    }
}
