package com.chapchap.subscription.domain.address.exception;

import com.chapchap.subscription.global.exception.BusinessException;
import com.chapchap.subscription.global.exception.ErrorCode;

public class AddressNotFoundException extends BusinessException {

    public AddressNotFoundException() {
        super(ErrorCode.ADDRESS_NOT_FOUND);
    }
}
