package com.chapchap.subscription.domain.terms.repository;

import com.chapchap.subscription.domain.terms.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermsRepository extends JpaRepository<Terms, Long> {

    Optional<Terms> findByTermsTypeAndIsCurrentTrueAndIsRequiredTrue(
            String termsType
    );
}
