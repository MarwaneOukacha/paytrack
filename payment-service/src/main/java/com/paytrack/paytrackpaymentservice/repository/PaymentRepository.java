package com.paytrack.paytrackpaymentservice.repository;

import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.shared.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> , JpaSpecificationExecutor<Payment> {

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByAccountId(String accountId, Pageable pageable);

    Page<Payment> findByStatusAndAccountId(PaymentStatus status, String accountId, Pageable pageable);

    List<Payment> findByAccountIdAndCreatedAtAfter(String accountId, LocalDateTime after);

    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :start AND p.createdAt < :end")
    List<Payment> findAllBetween(
            @Param("start") LocalDateTime start,
            @Param("end")   LocalDateTime end
    );

    long countByStatus(PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status AND p.createdAt >= :since")
    long countByStatusSince(
            @Param("status") PaymentStatus status,
            @Param("since")  LocalDateTime since
    );
}