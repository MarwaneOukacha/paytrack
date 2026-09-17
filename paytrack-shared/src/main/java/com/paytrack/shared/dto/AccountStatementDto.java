package com.paytrack.shared.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class AccountStatementDto {

    private String accountNumber;

    private String ownerName;

    private String currency;

    private BigDecimal balance;

    private List<AccountStatementEntryDto> entries;
}