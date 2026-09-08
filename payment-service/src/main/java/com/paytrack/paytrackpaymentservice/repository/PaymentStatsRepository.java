package com.paytrack.paytrackpaymentservice.repository;

import com.paytrack.paytrackpaymentservice.entity.PaymentStats;
import com.paytrack.shared.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface PaymentStatsRepository extends JpaRepository<PaymentStats, UUID> {
}
