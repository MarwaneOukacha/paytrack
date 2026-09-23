package com.paytrack.shared.dto;

import com.paytrack.shared.enums.FraudRuleType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FraudConfigDto {

    private UUID id;

    private FraudRuleType ruleType;

    private Boolean enabled;

    private BigDecimal threshold;

    private Integer windowMinutes;

    private String description;

    private String updatedBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}