package com.paytrack.shared.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
public class PaymentStatsDto {

    private long totalPayments;

    private BigDecimal totalAmount;

    private long successfulPayments;
    private BigDecimal successfulAmount;

    private long pendingPayments;
    private BigDecimal pendingAmount;

    private long failedPayments;
    private BigDecimal failedAmount;

    private BigDecimal averageAmount;

    private Map<String, Long> paymentsByStatus;
}