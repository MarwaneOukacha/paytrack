package com.paytrack.shared.dto;

import com.paytrack.shared.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class TransferResponse {
 
    private UUID transferId;          // id du Payment DEBIT — sert de référence du virement
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;     // PENDING au retour immédiat
    private String message;
    private LocalDateTime createdAt;
 

}