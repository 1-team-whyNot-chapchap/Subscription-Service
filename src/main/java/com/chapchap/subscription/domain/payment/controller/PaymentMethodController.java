package com.chapchap.subscription.domain.payment.controller;

import com.chapchap.subscription.domain.payment.PaymentMethodService;
import com.chapchap.subscription.domain.payment.entity.PaymentMethod;
import com.chapchap.subscription.domain.payment.request.PaymentMethodCreateRequest;
import com.chapchap.subscription.domain.payment.response.PaymentMethodCreateResponse;
import com.chapchap.subscription.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription/payment-methods")
public class PaymentMethodController {

    private final PaymentMethodService paymentMethodService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<GlobalResponse<PaymentMethodCreateResponse>> create(
        @Valid @RequestBody PaymentMethodCreateRequest request
        , Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        PaymentMethod paymentMethod = paymentMethodService.registerVerifiedPaymentMethod(userId, request.billingKey());
        PaymentMethodCreateResponse response = PaymentMethodCreateResponse.from(paymentMethod);

        return ResponseEntity.ok(GlobalResponse.success(response));
    }
}