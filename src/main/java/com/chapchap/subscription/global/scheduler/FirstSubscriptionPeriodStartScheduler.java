package com.chapchap.subscription.global.scheduler;

import com.chapchap.subscription.domain.subscription.service.FirstSubscriptionPeriodStartService;
import com.chapchap.subscription.domain.subscription.service.KstReferenceTimeProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 00:00 KST 직후 시작 작업이 누락돼도 같은 날 한 번 처리할 수 있도록 매분 확인한다. */
@Component
public class FirstSubscriptionPeriodStartScheduler {
    private final FirstSubscriptionPeriodStartService periodStartService;
    private final KstReferenceTimeProvider timeProvider;

    public FirstSubscriptionPeriodStartScheduler(
        FirstSubscriptionPeriodStartService periodStartService,
        KstReferenceTimeProvider timeProvider
    ) {
        this.periodStartService = periodStartService;
        this.timeProvider = timeProvider;
    }

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void startScheduledFirstPeriods() {
        periodStartService.startScheduledFirstPeriods(timeProvider.now().toLocalDate());
    }
}
