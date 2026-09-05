package com.chapchap.subscription.domain.payment.repository;

import com.chapchap.subscription.domain.payment.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** 환불 업무 결과를 저장하고 대상 주문별로 조회한다. */
public interface RefundRepository extends JpaRepository<Refund, Long> {
    Optional<Refund> findByOrderId(Long orderId);
}
