package com.paytrack.shared.dto;

import com.paytrack.shared.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PaymentFilter {

    private String accountId;

    private String toAccountNumber;

    private PaymentStatus status;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private LocalDateTime fromDate;

    private LocalDateTime toDate;

}