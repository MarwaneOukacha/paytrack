package com.paytrack.paytrackpaymentservice.service;


import com.paytrack.shared.dto.AccountDto;
import com.paytrack.shared.dto.CreateAccountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {

    AccountDto createAccount(CreateAccountRequest request);

    Page<AccountDto> getAccounts(Pageable pageable);
}