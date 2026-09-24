package com.paytrack.paytrackpaymentservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "limit_config")
@Data
public class LimitConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency = "MAD";

    @Column(name = "default_single_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultSingleTransactionLimit = new BigDecimal("10000.00");

    @Column(name = "default_daily_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultDailyLimit = new BigDecimal("30000.00");

    @Column(name = "default_monthly_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal defaultMonthlyLimit = new BigDecimal("150000.00");

    @Column(name = "max_single_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxSingleTransactionLimit = new BigDecimal("100000.00");

    @Column(name = "max_daily_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxDailyLimit = new BigDecimal("300000.00");

    @Column(name = "max_monthly_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxMonthlyLimit = new BigDecimal("1500000.00");

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}