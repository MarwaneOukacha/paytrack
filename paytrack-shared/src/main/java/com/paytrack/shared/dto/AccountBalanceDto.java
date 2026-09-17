package com.paytrack.shared.dto;

import com.paytrack.shared.enums.AccountStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountBalanceDto {

    private String accountNumber;

    private String ownerName;

    private BigDecimal balance;

    private String currency;

    private AccountStatus status;

    private LocalDateTime updatedAt;
}