package com.paytrack.paytrackpaymentservice.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    // Topic Kafka cible : "payment.initiated", "payment.processed"...
    @Column(nullable = false)
    private String topic;

    // Clé Kafka : accountId — pour garantir l'ordre par compte (même partition)
    @Column(name = "kafka_key")
    private String kafkaKey;

    // Payload JSON sérialisé du PaymentEvent
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    // false = pas encore envoyé sur Kafka
    @Column(nullable = false)
    private boolean sent = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }


}