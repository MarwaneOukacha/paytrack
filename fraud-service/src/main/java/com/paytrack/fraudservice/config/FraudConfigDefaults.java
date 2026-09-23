package com.paytrack.fraudservice.config;

import com.paytrack.fraudservice.entity.FraudConfig;
import com.paytrack.shared.enums.FraudRuleType;

import java.math.BigDecimal;

public final class FraudConfigDefaults {

    public static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000.00");
    public static final int REJECTION_THRESHOLD = 3;
    public static final int VELOCITY_THRESHOLD = 10;
    public static final int DEFAULT_WINDOW_MINUTES = 60;

    private FraudConfigDefaults() {
    }

    public static BigDecimal threshold(FraudRuleType type) {
        return switch (type) {
            case HIGH_AMOUNT -> HIGH_AMOUNT_THRESHOLD;
            case REJECTION_RATE -> new BigDecimal(REJECTION_THRESHOLD);
            case VELOCITY -> new BigDecimal(VELOCITY_THRESHOLD);
        };
    }

    public static String description(FraudRuleType type) {
        return switch (type) {
            case HIGH_AMOUNT -> "Rejette les paiements supérieurs au seuil de montant";
            case REJECTION_RATE -> "Rejette après trop de rejets récents sur la fenêtre glissante";
            case VELOCITY -> "Rejette en cas de volume de transactions anormal sur la fenêtre";
        };
    }

    public static FraudConfig newConfig(FraudRuleType type) {
        return FraudConfig.builder()
                .ruleType(type)
                .enabled(true)
                .threshold(threshold(type))
                .windowMinutes(DEFAULT_WINDOW_MINUTES)
                .description(description(type))
                .updatedBy("system")
                .build();
    }
}