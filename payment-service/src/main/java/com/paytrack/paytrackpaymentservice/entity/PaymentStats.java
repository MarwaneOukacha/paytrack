package com.paytrack.paytrackpaymentservice.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payment_stats")
@Data
public class PaymentStats {

    @Id
    private LocalDate date;  // une ligne par jour

    @Column(name = "total_count", nullable = false)
    private int totalCount = 0;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "fraud_count", nullable = false)
    private int fraudCount = 0;

    @Column(name = "dlt_count", nullable = false)
    private int dltCount = 0;

}
