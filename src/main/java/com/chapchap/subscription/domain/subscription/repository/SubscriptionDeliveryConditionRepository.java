package com.chapchap.subscription.domain.subscription.repository;

import com.chapchap.subscription.domain.subscription.entity.SubscriptionDeliveryCondition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionDeliveryConditionRepository
        extends JpaRepository<SubscriptionDeliveryCondition, Long> {

    // 구독 설정ID로 요일 별 배송 조건 불러오기
    List<SubscriptionDeliveryCondition> findAllBySubscriptionSettingId(
            Long subscriptionSettingId
    );
}
