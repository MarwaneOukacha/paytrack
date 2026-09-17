package com.paytrack.shared.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BalanceOperationRequest {

    @NotNull(message = "The amount is required")
    @DecimalMin(value = "0.01", message = "The amount must be greater than 0")
    @Digits(integer = 15, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max = 100, message = "The description cannot exceed 100 characters")
    private String description;
}