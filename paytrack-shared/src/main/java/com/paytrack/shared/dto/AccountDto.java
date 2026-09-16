package com.paytrack.shared.dto;

import com.paytrack.shared.enums.AccountStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountDto {

    private UUID id;

    private String accountNumber;

    private String ownerName;

    private String ownerEmail;

    private BigDecimal balance;

    private String currency;

    private AccountStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}