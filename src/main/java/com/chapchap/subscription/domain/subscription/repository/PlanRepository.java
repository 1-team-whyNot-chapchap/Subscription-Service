package com.chapchap.subscription.domain.subscription.repository;

import com.chapchap.subscription.domain.subscription.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanRepository extends JpaRepository<Plan, Long> {

    Optional<Plan> findByPublicId(String publicId);
}
