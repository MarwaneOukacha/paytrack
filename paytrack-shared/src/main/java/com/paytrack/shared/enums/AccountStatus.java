package com.paytrack.shared.enums;

public enum AccountStatus {
    ACTIVE,     // compte opérationnel
    INACTIVE,   // compte désactivé manuellement
    BLOCKED,    // bloqué par le Fraud Service
    CLOSED      // compte fermé définitivement
}
