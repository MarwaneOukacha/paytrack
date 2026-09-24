package com.paytrack.shared.dto;

import com.paytrack.shared.enums.CardNetwork;
import com.paytrack.shared.enums.CardType;
import lombok.Data;

import java.util.UUID;

@Data
public class CardNumberDto {

    private UUID cardId;

    private String cardNumber;

    private String cvv;

    private String expiryMonth;

    private String expiryYear;

    private CardNetwork network;

    private CardType type;
}