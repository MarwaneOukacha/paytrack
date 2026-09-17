package com.paytrack.paytrackpaymentservice.controller;

import com.paytrack.paytrackpaymentservice.service.AccountService;
import com.paytrack.shared.dto.AccountBalanceDto;
import com.paytrack.shared.dto.AccountDto;
import com.paytrack.shared.dto.AccountStatementDto;
import com.paytrack.shared.dto.BalanceOperationRequest;
import com.paytrack.shared.dto.CreateAccountRequest;
import com.paytrack.shared.dto.UpdateAccountRequest;
import com.paytrack.shared.enums.AccountStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        AccountDto account = accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(account);
    }

    @GetMapping
    public ResponseEntity<Page<AccountDto>> getAccounts(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) String ownerEmail,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        Page<AccountDto> accounts =
                accountService.getAccounts(status, ownerEmail, pageable);

        return ResponseEntity.ok(accounts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable UUID id) {

        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountDto> getAccountByAccountNumber(
            @PathVariable String accountNumber) {

        return ResponseEntity.ok(
                accountService.getAccountByAccountNumber(accountNumber)
        );
    }

    @GetMapping("/by-email/{email}")
    public ResponseEntity<AccountDto> getAccountByOwnerEmail(
            @PathVariable String email) {

        return ResponseEntity.ok(accountService.getAccountByOwnerEmail(email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AccountDto> updateAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {

        AccountDto account = accountService.updateAccount(id, request);

        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<AccountDto> patchAccount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateAccountRequest request) {

        AccountDto account = accountService.updateAccount(id, request);

        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> closeAccountById(@PathVariable UUID id) {

        accountService.closeAccount(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<AccountDto> activateAccount(@PathVariable UUID id) {

        return ResponseEntity.ok(
                accountService.changeStatus(id, AccountStatus.ACTIVE)
        );
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<AccountDto> deactivateAccount(@PathVariable UUID id) {

        return ResponseEntity.ok(
                accountService.changeStatus(id, AccountStatus.INACTIVE)
        );
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<AccountDto> blockAccount(@PathVariable UUID id) {

        return ResponseEntity.ok(
                accountService.changeStatus(id, AccountStatus.BLOCKED)
        );
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<AccountDto> unblockAccount(@PathVariable UUID id) {

        return ResponseEntity.ok(
                accountService.changeStatus(id, AccountStatus.ACTIVE)
        );
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<AccountDto> closeAccountByStatus(@PathVariable UUID id) {

        return ResponseEntity.ok(
                accountService.changeStatus(id, AccountStatus.CLOSED)
        );
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<AccountDto> deposit(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceOperationRequest request) {

        AccountDto account = accountService.deposit(
                id,
                request.getAmount(),
                request.getDescription()
        );

        return ResponseEntity.ok(account);
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<AccountDto> withdraw(
            @PathVariable UUID id,
            @Valid @RequestBody BalanceOperationRequest request) {

        AccountDto account = accountService.withdraw(
                id,
                request.getAmount(),
                request.getDescription()
        );

        return ResponseEntity.ok(account);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<AccountBalanceDto> getBalance(@PathVariable UUID id) {

        return ResponseEntity.ok(accountService.getBalance(id));
    }

    @GetMapping("/{id}/statement")
    public ResponseEntity<AccountStatementDto> getStatement(
            @PathVariable UUID id,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        return ResponseEntity.ok(
                accountService.getStatement(id, pageable)
        );
    }
}