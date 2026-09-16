package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.mapper.AccountMapper;
import com.paytrack.paytrackpaymentservice.repository.AccountRepository;
import com.paytrack.paytrackpaymentservice.service.AccountService;
import com.paytrack.shared.dto.AccountDto;
import com.paytrack.shared.dto.CreateAccountRequest;
import com.paytrack.shared.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public AccountDto createAccount(CreateAccountRequest request) {

        if (accountRepository.existsByOwnerEmail(request.getOwnerEmail())) {
            throw new IllegalArgumentException(
                    "An account already exists with this email"
            );
        }

        Account account = accountMapper.toEntity(request);

        account.setAccountNumber(generateAccountNumber());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(AccountStatus.ACTIVE);

        Account savedAccount = accountRepository.save(account);

        return accountMapper.toDto(savedAccount);
    }

    @Override
    public Page<AccountDto> getAccounts(Pageable pageable) {

        return accountRepository
                .findAll(pageable)
                .map(accountMapper::toDto);
    }

    private String generateAccountNumber() {

        return "ACC-" +
                String.format(
                        "%08d",
                        System.currentTimeMillis() % 100000000
                );
    }
}