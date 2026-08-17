package com.paytrack.fraudservice.service;

import com.paytrack.fraudservice.entity.FraudEvaluation;
import com.paytrack.shared.dto.PaymentEvent;

public interface FraudService {
    FraudEvaluation evaluate(PaymentEvent event);
}