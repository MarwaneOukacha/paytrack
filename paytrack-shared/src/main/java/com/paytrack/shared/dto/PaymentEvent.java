package com.paytrack.shared.dto;


import com.paytrack.shared.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class PaymentEvent {

    private UUID        paymentId;
    private String      accountId;          // fromAccountNumber
    private String      toAccountNumber;    // destination
    private BigDecimal  amount;
    private String      currency;
    private PaymentStatus status;
    private String      description;
}
