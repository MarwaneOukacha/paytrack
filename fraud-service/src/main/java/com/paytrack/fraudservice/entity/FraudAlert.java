package com.paytrack.fraudservice.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_alerts")
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private String accountId;

    // Nombre de transactions détectées dans la fenêtre
    @Column(name = "tx_count", nullable = false)
    private int txCount;

    // Durée de la fenêtre de détection en secondes (par défaut 60)
    @Column(name = "window_seconds", nullable = false)
    private int windowSeconds = 60;

    @Column(name = "detected_at", nullable = false)
    private LocalDateTime detectedAt;

    @PrePersist
    protected void onCreate() {
        detectedAt = LocalDateTime.now();
    }


}
