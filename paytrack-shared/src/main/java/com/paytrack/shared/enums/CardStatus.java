package com.paytrack.shared.enums;

public enum CardStatus {
    ACTIVE,     // carte opérationnelle
    INACTIVE,   // carte désactivée manuellement
    BLOCKED,    // carte bloquée (perte, vol, fraude…)
    EXPIRED,    // carte arrivée à expiration
    LOST,       // déclarée perdue
    STOLEN,     // déclarée volée
    CANCELLED   // carte annulée définitivement
}