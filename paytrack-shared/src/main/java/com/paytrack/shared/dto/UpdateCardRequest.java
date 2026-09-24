package com.paytrack.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCardRequest {

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal singleTransactionLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal dailyLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monthlyLimit;
}