package com.paytrack.paytrackpaymentservice.service;


import com.paytrack.shared.dto.AccountBalanceDto;
import com.paytrack.shared.dto.AccountDto;
import com.paytrack.shared.dto.AccountStatementDto;
import com.paytrack.shared.dto.CreateAccountRequest;
import com.paytrack.shared.dto.UpdateAccountRequest;
import com.paytrack.shared.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface AccountService {

    AccountDto createAccount(CreateAccountRequest request);

    Page<AccountDto> getAccounts(AccountStatus status, String ownerEmail, Pageable pageable);

    AccountDto getAccountById(UUID id);

    AccountDto getAccountByAccountNumber(String accountNumber);

    AccountDto getAccountByOwnerEmail(String ownerEmail);

    AccountDto updateAccount(UUID id, UpdateAccountRequest request);

    AccountDto closeAccount(UUID id);

    AccountDto changeStatus(UUID id, AccountStatus targetStatus);

    AccountDto deposit(UUID id, BigDecimal amount, String description);

    AccountDto withdraw(UUID id, BigDecimal amount, String description);

    AccountBalanceDto getBalance(UUID id);

    AccountStatementDto getStatement(UUID id, Pageable pageable);
}