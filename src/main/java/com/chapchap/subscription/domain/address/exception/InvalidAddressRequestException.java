package com.chapchap.subscription.domain.address.exception;

import com.chapchap.subscription.global.exception.BusinessException;
import com.chapchap.subscription.global.exception.ErrorCode;

public class InvalidAddressRequestException extends BusinessException {

    public InvalidAddressRequestException() {
        super(ErrorCode.INVALID_REQUEST);
    }
}
