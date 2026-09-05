package com.chapchap.subscription.domain.subscription.service;

import com.chapchap.subscription.domain.subscription.entity.Subscription;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionPeriod;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionPeriodStatus;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionStatus;
import com.chapchap.subscription.domain.subscription.entity.SubscriptionStatusHistory;
import com.chapchap.subscription.domain.subscription.repository.SubscriptionPeriodRepository;
import com.chapchap.subscription.domain.subscription.repository.SubscriptionRepository;
import com.chapchap.subscription.domain.subscription.repository.SubscriptionStatusHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirstSubscriptionPeriodStartServiceTest {
    private static final LocalDate START_DATE = LocalDate.of(2026, 9, 7);
    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 9, 7, 0, 1);

    @Mock private SubscriptionPeriodRepository periodRepository;
    @Mock private SubscriptionRepository subscriptionRepository;
    @Mock private SubscriptionStatusHistoryRepository historyRepository;
    @Mock private KstReferenceTimeProvider timeProvider;

    private FirstSubscriptionPeriodStartService service;
    private Subscription subscription;
    private SubscriptionPeriod period;

    @BeforeEach
    void setUp() {
        service = new FirstSubscriptionPeriodStartService(
            periodRepository, subscriptionRepository, historyRepository, timeProvider
        );
        subscription = Subscription.create(10L);
        ReflectionTestUtils.setField(subscription, "id", 1L);
        subscription.markScheduled();
        period = SubscriptionPeriod.createAwaitingConfirmation(
            1L, 1, START_DATE, STARTED_AT.minusDays(1)
        );
        ReflectionTestUtils.setField(period, "id", 2L);
        period.markScheduled();
        when(timeProvider.now()).thenReturn(STARTED_AT);
    }

    @Test
    void 오늘_시작하는_첫_이용기간은_구독과_함께_이용중으로_전환한다() {
        candidates(period);
        when(periodRepository.findWithLockById(2L)).thenReturn(Optional.of(period));
        when(subscriptionRepository.findWithLockById(1L)).thenReturn(Optional.of(subscription));

        service.startScheduledFirstPeriods(START_DATE);

        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.IN_PROGRESS);
        assertThat(period.getStatus()).isEqualTo(SubscriptionPeriodStatus.IN_PROGRESS);
        ArgumentCaptor<SubscriptionStatusHistory> captor = ArgumentCaptor.forClass(SubscriptionStatusHistory.class);
        verify(historyRepository).save(captor.capture());
        assertThat(captor.getValue().getPreviousStatus()).isEqualTo(SubscriptionStatus.SCHEDULED);
        assertThat(captor.getValue().getNextStatus()).isEqualTo(SubscriptionStatus.IN_PROGRESS);
        assertThat(captor.getValue().getChangeActor()).isEqualTo("SYSTEM");
        assertThat(captor.getValue().getChangeReason()).isEqualTo("FIRST_PERIOD_STARTED");
        assertThat(captor.getValue().getChangedAt()).isEqualTo(STARTED_AT);
    }

    @Test
    void 시작취소_등으로_구독이_시작예정이_아니면_상태를_바꾸지_않는다() {
        candidates(period);
        when(periodRepository.findWithLockById(2L)).thenReturn(Optional.of(period));
        subscription = Subscription.create(10L);
        ReflectionTestUtils.setField(subscription, "id", 1L);
        subscription.markPaymentFailed();
        when(subscriptionRepository.findWithLockById(1L)).thenReturn(Optional.of(subscription));

        service.startScheduledFirstPeriods(START_DATE);

        assertThat(period.getStatus()).isEqualTo(SubscriptionPeriodStatus.SCHEDULED);
        verify(historyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 이미_처리된_기간은_다시_선택되지_않아_이력을_추가하지_않는다() {
        candidates();

        service.startScheduledFirstPeriods(START_DATE);

        verify(periodRepository, never()).findWithLockById(org.mockito.ArgumentMatchers.anyLong());
        verify(historyRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private void candidates(SubscriptionPeriod... periods) {
        when(periodRepository.findAllByPeriodSequenceAndStatusAndPeriodStartDate(
            1, SubscriptionPeriodStatus.SCHEDULED, START_DATE
        )).thenReturn(List.of(periods));
    }
}
