package com.paytrack.shared.dto;

import com.paytrack.shared.enums.CardNetwork;
import com.paytrack.shared.enums.CardType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateCardRequest {

    @NotNull
    private UUID accountId;

    @NotNull
    private CardType type;

    @NotNull
    private CardNetwork network;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal singleTransactionLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal dailyLimit;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal monthlyLimit;

    private String currency;
}