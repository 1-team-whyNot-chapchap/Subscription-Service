package com.chapchap.subscription.domain.payment.exception;

import com.chapchap.subscription.global.exception.BusinessException;
import com.chapchap.subscription.global.exception.ErrorCode;

public class PaymentMethodInvalidException extends BusinessException{
    public PaymentMethodInvalidException() {
        super(ErrorCode.PAYMENT_METHOD_INVALID);
    }
}
