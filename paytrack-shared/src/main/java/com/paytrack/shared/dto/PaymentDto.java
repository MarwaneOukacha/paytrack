package com.paytrack.shared.dto;

import com.paytrack.shared.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentDto {
    private UUID id;
    private String toAccountNumber;
    private String accountId;
    private BigDecimal amount;
    private String description;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}