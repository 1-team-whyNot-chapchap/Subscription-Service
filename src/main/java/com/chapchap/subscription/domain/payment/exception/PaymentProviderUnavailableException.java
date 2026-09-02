package com.chapchap.subscription.domain.payment.exception;

import com.chapchap.subscription.global.exception.BusinessException;
import com.chapchap.subscription.global.exception.ErrorCode;

public class PaymentProviderUnavailableException extends BusinessException {

    public PaymentProviderUnavailableException() {
        super(ErrorCode.PAYMENT_PROVIDER_UNAVAILABLE);
    }
}