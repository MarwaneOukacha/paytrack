package com.paytrack.fraudservice.dto;

import lombok.Data;

@Data
public class FraudStatsDto {

    private long totalEvaluations;
    private long approvedCount;
    private long rejectedCount;
}