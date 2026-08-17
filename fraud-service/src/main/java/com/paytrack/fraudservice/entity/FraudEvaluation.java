package com.paytrack.fraudservice.entity;


import com.paytrack.shared.enums.FraudDecision;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_evaluations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private String accountId;

    @Column(nullable = false)
    private BigDecimal amount;

    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FraudDecision decision; // APPROVED, REJECTED

    private String reason;

    @Column(nullable = false)
    private LocalDateTime evaluatedAt;

    @PrePersist
    public void prePersist() {
        this.evaluatedAt = LocalDateTime.now();
    }
}
