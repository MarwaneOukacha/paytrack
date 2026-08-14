package com.paytrack.paytrackpaymentservice.mapper;


import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.shared.dto.TransferRequest;
import com.paytrack.shared.dto.TransferResponse;
import org.mapstruct.*;
 
@Mapper(componentModel = "spring")
public interface TransferMapper {
 
    // ── TransferRequest → Payment (DEBIT) ────────────────────────────────
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "accountId",        source = "request.fromAccountNumber")
    @Mapping(target = "toAccountNumber",  source = "request.toAccountNumber")
    @Mapping(target = "amount",           source = "request.amount")
    @Mapping(target = "description",      source = "request.description")
    @Mapping(target = "status",           constant = "PENDING")
    @Mapping(target = "type",             constant = "DEBIT")
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    Payment toDebitPayment(TransferRequest request);
 
    // ── Payment (DEBIT) + Account source → TransferResponse ─────────────
    @Mapping(target = "transferId",         source = "payment.id")
    @Mapping(target = "fromAccountNumber",  source = "payment.accountId")
    @Mapping(target = "toAccountNumber",    source = "payment.toAccountNumber")
    @Mapping(target = "amount",             source = "payment.amount")
    @Mapping(target = "currency",           source = "account.currency")
    @Mapping(target = "status",             source = "payment.status")
    @Mapping(target = "createdAt",          source = "payment.createdAt")
    @Mapping(target = "message",            constant = "Virement en cours de traitement")
    TransferResponse toResponse(Payment payment, Account account);
 
    // ── Payment CREDIT ────────────────────────────────────────────────────
    @Mapping(target = "id",               ignore = true)
    @Mapping(target = "accountId",        source = "toAccountNumber")
    @Mapping(target = "toAccountNumber",  source = "fromAccountNumber")
    @Mapping(target = "amount",           source = "amount")
    @Mapping(target = "description",      expression = "java(\"Virement reçu — \" + description)")
    @Mapping(target = "status",           constant = "PROCESSED")
    @Mapping(target = "type",             constant = "CREDIT")
    @Mapping(target = "createdAt",        ignore = true)
    @Mapping(target = "updatedAt",        ignore = true)
    Payment toCreditPayment(
        @MappingTarget Payment target,
        String fromAccountNumber,
        String toAccountNumber,
        java.math.BigDecimal amount,
        String description
    );
}