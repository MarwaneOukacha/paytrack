package com.paytrack.shared.dto;

import com.paytrack.shared.enums.FraudDecision;
import com.paytrack.shared.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudEvent {
    private UUID paymentId;
    private String accountId;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private PaymentStatus status;
    private FraudDecision decision;
    private String reason;
}