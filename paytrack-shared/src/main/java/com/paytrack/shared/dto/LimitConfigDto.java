package com.paytrack.shared.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LimitConfigDto {

    private String defaultCurrency;

    private BigDecimal defaultSingleTransactionLimit;

    private BigDecimal defaultDailyLimit;

    private BigDecimal defaultMonthlyLimit;

    private BigDecimal maxSingleTransactionLimit;

    private BigDecimal maxDailyLimit;

    private BigDecimal maxMonthlyLimit;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}