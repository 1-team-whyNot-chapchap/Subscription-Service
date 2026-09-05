package com.chapchap.subscription.domain.payment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.GeneratedColumn;

import java.time.LocalDateTime;

/** 환불 대상과 여러 원 결제 취소의 집계 결과를 보존하는 환불 업무다. */
@Getter
@Entity
@Table(
    name = "refunds",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_refunds_public_id", columnNames = "public_id"),
        @UniqueConstraint(name = "uk_refunds_business_key", columnNames = "business_deduplication_key"),
        @UniqueConstraint(name = "uk_refunds_period", columnNames = "subscription_period_id"),
        @UniqueConstraint(name = "uk_refunds_setting", columnNames = "subscription_setting_id"),
        @UniqueConstraint(name = "uk_refunds_order", columnNames = "order_id"),
        @UniqueConstraint(name = "uk_refunds_external_delivery", columnNames = "external_delivery_id")
    },
    indexes = {
        @Index(name = "idx_refunds_subscription_requested", columnList = "subscription_id, requested_at"),
        @Index(name = "idx_refunds_status_requested", columnList = "status, requested_at")
    }
)
@Check(name = "ck_refunds_amount", constraints = "refund_amount >= 1")
@Check(
    name = "ck_refunds_successful_amount",
    constraints = "successful_refund_amount >= 0 AND successful_refund_amount <= refund_amount"
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "BIGINT UNSIGNED")
    private Long id;

    @Column(name = "public_id", nullable = false, length = 40, columnDefinition = "CHAR(40)")
    private String publicId;

    @Column(name = "subscription_id", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private RefundType refundType;

    @Column(name = "subscription_period_id", columnDefinition = "BIGINT UNSIGNED")
    private Long subscriptionPeriodId;

    @Column(name = "subscription_setting_id", columnDefinition = "BIGINT UNSIGNED")
    private Long subscriptionSettingId;

    @Column(name = "order_id", columnDefinition = "BIGINT UNSIGNED")
    private Long orderId;

    @Column(name = "external_delivery_id", length = 36, columnDefinition = "CHAR(36)")
    private String externalDeliveryId;

    @Column(name = "refund_amount", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long refundAmount;

    @Column(name = "successful_refund_amount", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long successfulRefundAmount;

    @GeneratedColumn("refund_amount - successful_refund_amount")
    @Column(name = "unprocessed_amount", nullable = false, columnDefinition = "BIGINT UNSIGNED")
    private Long unprocessedAmount;

    @Column(name = "business_deduplication_key", nullable = false, length = 255)
    private String businessDeduplicationKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private RefundStatus status;

    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    @Column(name = "requested_at", nullable = false, insertable = false, updatable = false,
        columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime requestedAt;

    @Column(name = "completed_at", columnDefinition = "DATETIME(6)")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false,
        columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false,
        columnDefinition = "DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)")
    private LocalDateTime updatedAt;
}
