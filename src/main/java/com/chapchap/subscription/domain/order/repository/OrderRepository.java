package com.chapchap.subscription.domain.order.repository;

import com.chapchap.subscription.domain.order.entity.Order;
import com.chapchap.subscription.domain.order.entity.OrderKafkaDeliveryStatus;
import com.chapchap.subscription.domain.order.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

/** 주문의 저장과 이용 기간별 최초 주문 존재 여부 조회를 담당한다. */
public interface OrderRepository extends JpaRepository<Order, Long> {
    /**
     * 같은 이용 기간에 최초 주문 묶음이 이미 생성됐는지 확인한다.
     *
     * @param subscriptionPeriodId 확인할 이용 기간 식별자
     * @return 주문이 하나라도 존재하면 {@code true}
     */
    boolean existsBySubscriptionPeriodId(Long subscriptionPeriodId);

    /**
     * 한 이용 기간에 생성된 첫 주문 묶음 전체를 조회한다.
     *
     * @param subscriptionPeriodId 조회할 이용 기간 식별자
     * @return 해당 이용 기간의 주문 목록
     */
    List<Order> findAllBySubscriptionPeriodId(Long subscriptionPeriodId);

    /** 같은 실행에서 중복 발행하지 않도록 대상 주문을 잠근 뒤 조회한다. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Order> findAllByDeliveryDateAndStatusAndKafkaDeliveryStatus(
        LocalDate deliveryDate,
        OrderStatus status,
        OrderKafkaDeliveryStatus kafkaDeliveryStatus
    );
}
