package com.paytrack.shared.dto;

import com.paytrack.shared.enums.FraudRuleType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateFraudConfigRequest {

    @NotNull(message = "Rule type is required")
    private FraudRuleType ruleType;

    private Boolean enabled = true;

    @NotNull(message = "Threshold is required")
    @DecimalMin(value = "0.01", message = "Threshold must be greater than zero")
    private BigDecimal threshold;

    @Min(value = 1, message = "Window must be at least 1 minute")
    private Integer windowMinutes;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    @Size(max = 100, message = "UpdatedBy cannot exceed 100 characters")
    private String updatedBy;
}