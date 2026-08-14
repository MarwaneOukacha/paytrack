package com.paytrack.shared.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotBlank(message = "The source account is required")
    private String fromAccountNumber;

    @NotBlank(message = "The destination account is required")
    private String toAccountNumber;

    @NotNull(message = "The amount is required")
    @DecimalMin(value = "0.01", message = "The amount must be greater than 0")
    @DecimalMax(value = "10000.00", message = "The amount cannot exceed 10,000 MAD")
    @Digits(integer = 8, fraction = 2, message = "Invalid amount format")
    private BigDecimal amount;

    @Size(max = 100, message = "The description cannot exceed 100 characters")
    private String description;
}
 