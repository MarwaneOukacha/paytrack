package com.paytrack.fraudservice.dto;

import com.paytrack.shared.enums.FraudDecision;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class FraudEvaluationDto {

    private UUID id;
    private UUID paymentId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private FraudDecision decision;
    private String reason;
    private LocalDateTime evaluatedAt;
}
