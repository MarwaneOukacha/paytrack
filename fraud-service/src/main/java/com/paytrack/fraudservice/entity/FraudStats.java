package com.paytrack.fraudservice.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fraud_stats")
public class FraudStats {

    // Une ligne par compte — PK = accountId
    @Id
    @Column(name = "account_id")
    private String accountId;

    @Column(name = "total_flags", nullable = false)
    private int totalFlags = 0;

    @Column(name = "last_flag")
    private LocalDateTime lastFlag;
}
