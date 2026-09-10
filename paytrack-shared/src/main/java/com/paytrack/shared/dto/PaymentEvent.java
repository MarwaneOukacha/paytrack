package com.paytrack.shared.dto;

import com.paytrack.shared.enums.PaymentStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private UUID paymentId;
    private String accountId;
    private String toAccountNumber;
    private BigDecimal amount;
    private String description;
    private PaymentStatus status;
    private LocalDateTime createdAt;
}