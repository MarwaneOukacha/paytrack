package com.paytrack.paytrackpaymentservice.entity;


import com.paytrack.shared.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "accounts")
@Data
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;          // ex : ACC-00123

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "owner_email", nullable = false, unique = true)
    private String ownerEmail;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "MAD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ── Méthodes métier ──────────────────────────────────────────────────

    /**
     * Débite le compte source.
     * Appelé dans @Transactional avant de publier sur Kafka.
     */
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Solde insuffisant");
        }
        this.balance = this.balance.subtract(amount);
    }

    /**
     * Crédite le compte destination.
     * Appelé dans le TransferConsumer après réception du message Kafka.
     */
    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        this.balance = this.balance.add(amount);
    }

    /**
     * Vérifie si le compte peut émettre un paiement.
     */
    public boolean canTransfer(BigDecimal amount) {
        return this.status == AccountStatus.ACTIVE
                && this.balance.compareTo(amount) >= 0;
    }


}
