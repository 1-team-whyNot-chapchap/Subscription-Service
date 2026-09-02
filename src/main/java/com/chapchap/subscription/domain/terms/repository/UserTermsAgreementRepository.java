package com.chapchap.subscription.domain.terms.repository;

import com.chapchap.subscription.domain.terms.entity.UserTermsAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTermsAgreementRepository
        extends JpaRepository<UserTermsAgreement, Long> {

    Optional <UserTermsAgreement> findByUserIdAndTermsId(
            Long userId,
            Long termsId
    );
}