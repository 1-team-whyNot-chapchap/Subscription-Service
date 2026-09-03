package com.chapchap.subscription.domain.subscription.repository;

import com.chapchap.subscription.domain.subscription.entity.SubscriptionPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPeriodRepository extends JpaRepository<SubscriptionPeriod, Long> {

    // 특정 구독에 속한 기간 중 가장 최근 구독 기간 순번을 불러옴
    Optional<SubscriptionPeriod> findTopBySubscriptionIdOrderByPeriodSequenceDesc(Long subscriptionId);
}
