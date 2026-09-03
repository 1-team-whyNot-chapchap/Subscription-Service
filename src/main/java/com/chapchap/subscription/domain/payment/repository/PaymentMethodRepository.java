package com.chapchap.subscription.domain.payment.repository;

import com.chapchap.subscription.domain.payment.entity.PaymentMethod;
import com.chapchap.subscription.domain.payment.entity.PaymentMethodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    boolean existsByUserIdAndStatus(Long userId, PaymentMethodStatus status);

    // 인증 고객의 사용 가능한 자동결제수단 조회
    List<PaymentMethod> findAllByUserIdAndStatusAndDeletedAtIsNullOrderByIdAsc(Long userId, PaymentMethodStatus status);

    Optional<PaymentMethod> findByPublicIdAndUserIdAndStatusAndDeletedAtIsNull(
        String publicId
        , Long userId
        , PaymentMethodStatus status
    );

    Optional<PaymentMethod> findByUserIdAndStatusAndIsCurrentTrueAndDeletedAtIsNull(
        Long userId
        , PaymentMethodStatus status
    );
}
