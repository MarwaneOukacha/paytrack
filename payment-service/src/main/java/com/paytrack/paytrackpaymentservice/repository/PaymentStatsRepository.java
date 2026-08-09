package com.paytrack.paytrackpaymentservice.repository;

import com.paytrack.shared.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentStatsRepository extends JpaRepository<PaymentStatus, UUID> {
}
