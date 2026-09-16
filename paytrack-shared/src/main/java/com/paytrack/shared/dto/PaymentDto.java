package com.paytrack.shared.dto;

import com.paytrack.shared.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {

    private String toAccountNumber;
    private String accountId;
    private BigDecimal amount;
    private String description;
    private PaymentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}