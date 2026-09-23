package com.paytrack.fraudservice.entity;

import com.paytrack.shared.enums.FraudRuleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fraud_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rule_type", nullable = false, unique = true)
    private FraudRuleType ruleType;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private BigDecimal threshold;

    @Column(name = "window_minutes")
    private Integer windowMinutes;

    private String description;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}