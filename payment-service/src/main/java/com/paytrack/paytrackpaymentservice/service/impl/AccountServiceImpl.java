package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.paytrackpaymentservice.mapper.AccountMapper;
import com.paytrack.paytrackpaymentservice.repository.AccountRepository;
import com.paytrack.paytrackpaymentservice.repository.PaymentRepository;
import com.paytrack.paytrackpaymentservice.service.AccountService;
import com.paytrack.shared.dto.AccountBalanceDto;
import com.paytrack.shared.dto.AccountDto;
import com.paytrack.shared.dto.AccountStatementDto;
import com.paytrack.shared.dto.AccountStatementEntryDto;
import com.paytrack.shared.dto.CreateAccountRequest;
import com.paytrack.shared.dto.UpdateAccountRequest;
import com.paytrack.shared.enums.AccountOperationType;
import com.paytrack.shared.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
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
    @Transactional(readOnly = true)
    public Page<AccountDto> getAccounts(AccountStatus status, String ownerEmail, Pageable pageable) {

        Page<Account> accounts;

        if (status != null && ownerEmail != null && !ownerEmail.isBlank()) {
            accounts = accountRepository
                    .findByStatusAndOwnerEmailContainingIgnoreCase(status, ownerEmail, pageable);
        } else if (status != null) {
            accounts = accountRepository.findByStatus(status, pageable);
        } else if (ownerEmail != null && !ownerEmail.isBlank()) {
            accounts = accountRepository.findByOwnerEmailContainingIgnoreCase(ownerEmail, pageable);
        } else {
            accounts = accountRepository.findAll(pageable);
        }

        return accounts.map(accountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountById(UUID id) {

        return accountMapper.toDto(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountByAccountNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with account number: " + accountNumber
                ));

        return accountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDto getAccountByOwnerEmail(String ownerEmail) {

        Account account = accountRepository.findByOwnerEmail(ownerEmail)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with owner email: " + ownerEmail
                ));

        return accountMapper.toDto(account);
    }

    @Override
    @Transactional
    public AccountDto updateAccount(UUID id, UpdateAccountRequest request) {

        Account account = findById(id);

        if (request.getOwnerName() != null && !request.getOwnerName().isBlank()) {
            account.setOwnerName(request.getOwnerName());
        }

        if (request.getOwnerEmail() != null && !request.getOwnerEmail().isBlank()) {
            if (!account.getOwnerEmail().equalsIgnoreCase(request.getOwnerEmail())
                    && accountRepository.existsByOwnerEmail(request.getOwnerEmail())) {
                throw new IllegalArgumentException(
                        "An account already exists with this email"
                );
            }
            account.setOwnerEmail(request.getOwnerEmail());
        }

        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            account.setCurrency(request.getCurrency().toUpperCase());
        }

        Account updatedAccount = accountRepository.save(account);

        return accountMapper.toDto(updatedAccount);
    }

    @Override
    @Transactional
    public AccountDto closeAccount(UUID id) {

        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with id: " + id
                ));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalArgumentException("Account is already closed");
        }

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Account balance must be zero before closing"
            );
        }

        account.setStatus(AccountStatus.CLOSED);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto changeStatus(UUID id, AccountStatus targetStatus) {

        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with id: " + id
                ));

        if (account.getStatus() == targetStatus) {
            throw new IllegalArgumentException(
                    "Account is already " + targetStatus
            );
        }

        switch (targetStatus) {
            case ACTIVE -> {
                if (account.getStatus() != AccountStatus.INACTIVE
                        && account.getStatus() != AccountStatus.BLOCKED) {
                    throw new IllegalStateException(
                            "Only an inactive or blocked account can be activated"
                    );
                }
            }
            case INACTIVE -> {
                if (account.getStatus() != AccountStatus.ACTIVE) {
                    throw new IllegalStateException(
                            "Only an active account can be deactivated"
                    );
                }
            }
            case BLOCKED -> {
                if (account.getStatus() != AccountStatus.ACTIVE
                        && account.getStatus() != AccountStatus.INACTIVE) {
                    throw new IllegalStateException(
                            "Only active or inactive accounts can be blocked"
                    );
                }
            }
            case CLOSED -> closeInternal(account);
        }

        account.setStatus(targetStatus);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto deposit(UUID id, BigDecimal amount, String description) {

        validateAmount(amount);

        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with id: " + id
                ));

        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("Cannot deposit into a closed account");
        }

        account.credit(amount);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountDto withdraw(UUID id, BigDecimal amount, String description) {

        validateAmount(amount);

        Account account = accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with id: " + id
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Only an active account can be debited"
            );
        }

        account.debit(amount);

        return accountMapper.toDto(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountBalanceDto getBalance(UUID id) {

        Account account = findById(id);

        AccountBalanceDto dto = new AccountBalanceDto();
        dto.setAccountNumber(account.getAccountNumber());
        dto.setOwnerName(account.getOwnerName());
        dto.setBalance(account.getBalance());
        dto.setCurrency(account.getCurrency());
        dto.setStatus(account.getStatus());
        dto.setUpdatedAt(account.getUpdatedAt());

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public AccountStatementDto getStatement(UUID id, Pageable pageable) {

        Account account = findById(id);

        Page<Payment> payments = paymentRepository
                .findByAccountNumberInvolved(account.getAccountNumber(), pageable);

        List<AccountStatementEntryDto> entries = payments.stream()
                .map(p -> toStatementEntry(account.getAccountNumber(), p))
                .toList();

        AccountStatementDto statement = new AccountStatementDto();
        statement.setAccountNumber(account.getAccountNumber());
        statement.setOwnerName(account.getOwnerName());
        statement.setCurrency(account.getCurrency());
        statement.setBalance(account.getBalance());
        statement.setEntries(entries);

        return statement;
    }

    private Account findById(UUID id) {

        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Account not found with id: " + id
                ));
    }

    private void closeInternal(Account account) {

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException(
                    "Account balance must be zero before closing"
            );
        }
    }

    private void validateAmount(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private AccountStatementEntryDto toStatementEntry(String accountNumber, Payment payment) {

        boolean isDebit = accountNumber.equals(payment.getAccountId());

        AccountStatementEntryDto entry = new AccountStatementEntryDto();
        entry.setReference(payment.getId());
        entry.setType(isDebit ? AccountOperationType.DEBIT : AccountOperationType.CREDIT);
        entry.setCounterpartyAccountNumber(
                isDebit ? payment.getToAccountNumber() : payment.getAccountId()
        );
        entry.setAmount(payment.getAmount());
        entry.setStatus(payment.getStatus());
        entry.setDescription(payment.getDescription());
        entry.setDate(payment.getCreatedAt());

        return entry;
    }

    private String generateAccountNumber() {

        return "ACC-" +
                String.format(
                        "%08d",
                        System.currentTimeMillis() % 100000000
                );
    }
}