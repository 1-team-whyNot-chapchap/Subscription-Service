package com.chapchap.subscription.domain.payment.repository;

import com.chapchap.subscription.domain.payment.entity.PaymentMethod;
import com.chapchap.subscription.domain.payment.entity.PaymentMethodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    boolean existsByUserIdAndStatus(Long userId, PaymentMethodStatus status);
}
