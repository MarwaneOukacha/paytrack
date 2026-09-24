package com.paytrack.shared.dto;

import com.paytrack.shared.enums.CardNetwork;
import com.paytrack.shared.enums.CardStatus;
import com.paytrack.shared.enums.CardType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CardDto {

    private UUID id;

    /** Numéro de carte masqué, ex : 4147 32** **** 1234 */
    private String cardNumber;

    /** Les 4 derniers chiffres du PAN */
    private String lastFourDigits;

    private String cardholderName;

    private UUID accountId;

    private String accountNumber;

    private CardNetwork network;

    private CardType type;

    private String expiryMonth;

    private String expiryYear;

    private CardStatus status;

    private BigDecimal singleTransactionLimit;

    private BigDecimal dailyLimit;

    private BigDecimal monthlyLimit;

    private String currency;

    private LocalDateTime issuedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}