package com.chapchap.subscription.domain.subscription.repository;

import com.chapchap.subscription.domain.subscription.entity.SubscriptionSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionSettingRepository extends JpaRepository<SubscriptionSetting, Long> {

    // 특정 구독의 구독 기간 중 순번이 가장 큰(마지만 회차) 찾기
    Optional<SubscriptionSetting> findTopBySubscriptionIdOrderBySettingSequenceDesc(Long subscriptionId);
}
