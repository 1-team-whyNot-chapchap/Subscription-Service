package com.chapchap.subscription.domain.subscription.controller;

import com.chapchap.subscription.domain.subscription.request.FirstSubscriptionRequest;
import com.chapchap.subscription.domain.subscription.response.CurrentSubscriptionResponse;
import com.chapchap.subscription.domain.subscription.response.FirstSubscriptionResponse;
import com.chapchap.subscription.domain.subscription.service.CurrentSubscriptionQueryService;
import com.chapchap.subscription.domain.subscription.service.FirstSubscriptionService;
import com.chapchap.subscription.global.response.GlobalResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 인증 고객의 첫 구독 신청과 첫 결제 HTTP 진입점을 제공한다. */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/subscription/subscriptions")
public class SubscriptionController {
    private final FirstSubscriptionService firstSubscriptionService;
    private final CurrentSubscriptionQueryService currentSubscriptionQueryService;

    /** Gateway 인증 고객의 현재 구독 상태와 적용 설정을 조회한다. */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public GlobalResponse<CurrentSubscriptionResponse> getCurrentSubscription(
        Authentication authentication
    ) {
        return GlobalResponse.success(
            currentSubscriptionQueryService.getCurrentSubscription(
                Long.parseLong(authentication.getName())
            )
        );
    }

    /** Gateway 인증 고객을 기준으로 첫 구독 신청을 처리한다. */
    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public GlobalResponse<FirstSubscriptionResponse> create(
        Authentication authentication,
        @Valid @RequestBody FirstSubscriptionRequest request
    ) {
        return GlobalResponse.success(
            firstSubscriptionService.subscribe(Long.parseLong(authentication.getName()), request)
        );
    }
}
