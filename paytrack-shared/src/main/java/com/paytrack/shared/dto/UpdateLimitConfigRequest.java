package com.paytrack.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateLimitConfigRequest {

    private String defaultCurrency;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal defaultSingleTransactionLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal defaultDailyLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal defaultMonthlyLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal maxSingleTransactionLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal maxDailyLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal maxMonthlyLimit;
}