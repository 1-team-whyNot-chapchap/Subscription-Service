package com.chapchap.subscription.domain.subscription.service;

import com.chapchap.subscription.domain.subscription.entity.Subscription;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionPeriod;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionPeriodStatus;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionStatus;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionStatusHistory;
import com.chapchap.subscription.domain.subscription.repository.SubscriptionPeriodRepository;
import com.chapchap.subscription.domain.subscription.repository.SubscriptionRepository;
import com.chapchap.subscription.domain.subscription.repository.SubscriptionStatusHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 첫 이용 기간 시작일에 구독과 이용 기간을 함께 이용 중으로 전환한다. */
@Service
public class FirstSubscriptionPeriodStartService {
    private static final int FIRST_PERIOD_SEQUENCE = 1;
    private static final String ACTOR = "SYSTEM";
    private static final String REASON = "FIRST_PERIOD_STARTED";

    private final SubscriptionPeriodRepository periodRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionStatusHistoryRepository historyRepository;
    private final KstReferenceTimeProvider timeProvider;

    public FirstSubscriptionPeriodStartService(
        SubscriptionPeriodRepository periodRepository,
        SubscriptionRepository subscriptionRepository,
        SubscriptionStatusHistoryRepository historyRepository,
        KstReferenceTimeProvider timeProvider
    ) {
        this.periodRepository = periodRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.historyRepository = historyRepository;
        this.timeProvider = timeProvider;
    }

    /** KST 오늘 시작하는 첫 이용 기간만 처리한다. */
    @Transactional
    public void startScheduledFirstPeriods(LocalDate today) {
        List<Long> periodIds = periodRepository.findAllByPeriodSequenceAndStatusAndPeriodStartDate(
            FIRST_PERIOD_SEQUENCE, SubscriptionPeriodStatus.SCHEDULED, today
        ).stream().map(SubscriptionPeriod::getId).toList();

        LocalDateTime changedAt = timeProvider.now();
        for (Long periodId : periodIds) {
            startIfStillScheduled(periodId, changedAt);
        }
    }

    private void startIfStillScheduled(Long periodId, LocalDateTime changedAt) {
        SubscriptionPeriod period = periodRepository.findWithLockById(periodId).orElse(null);
        if (period == null || period.getStatus() != SubscriptionPeriodStatus.SCHEDULED) {
            return;
        }

        Subscription subscription = subscriptionRepository.findWithLockById(period.getSubscriptionId()).orElse(null);
        if (subscription == null || subscription.getStatus() != SubscriptionStatus.SCHEDULED) {
            return;
        }

        SubscriptionStatus previousStatus = subscription.startFirstPeriod();
        period.start();
        historyRepository.save(SubscriptionStatusHistory.create(
            subscription.getId(), previousStatus, SubscriptionStatus.IN_PROGRESS, ACTOR, REASON, changedAt
        ));
    }
}
