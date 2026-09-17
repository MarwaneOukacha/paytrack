package com.paytrack.shared.dto;

import com.paytrack.shared.enums.AccountOperationType;
import com.paytrack.shared.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountStatementEntryDto {

    private UUID reference;

    private AccountOperationType type;

    private String counterpartyAccountNumber;

    private BigDecimal amount;

    private PaymentStatus status;

    private String description;

    private LocalDateTime date;
}