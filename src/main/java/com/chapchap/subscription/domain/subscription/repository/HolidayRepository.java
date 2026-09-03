package com.chapchap.subscription.domain.subscription.repository;

import com.chapchap.subscription.domain.subscription.entity.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {
}
