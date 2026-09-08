package com.paytrack.fraudservice.service.impl;

import com.paytrack.fraudservice.entity.FraudEvaluation;
import com.paytrack.fraudservice.repository.FraudEvaluationRepository;
import com.paytrack.fraudservice.service.FraudService;
import com.paytrack.shared.dto.PaymentEvent;
import com.paytrack.shared.enums.FraudDecision;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FraudServiceImpl implements FraudService {

    private final FraudEvaluationRepository fraudRepository;
    private static final Logger log = LoggerFactory.getLogger(FraudServiceImpl.class);

    // Thresholds
    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("10000.00");
    private static final int MAX_REJECTIONS_PER_HOUR = 3;
    private static final int MAX_TRANSACTIONS_PER_HOUR = 10;

    @Override
    @Transactional
    public FraudEvaluation evaluate(PaymentEvent event) {

        String reason = null;
        FraudDecision decision = FraudDecision.APPROVED;

        // Rule 1 — high amount
        if (event.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            decision = FraudDecision.REJECTED;
            reason = "Amount exceeds threshold: " + event.getAmount();
        }

        // Rule 2 — too many rejections in the last hour
        if (decision == FraudDecision.APPROVED) {
            long recentRejections = fraudRepository.countByAccountIdAndDecisionAndEvaluatedAtAfter(
                    event.getAccountId(),
                    FraudDecision.REJECTED,
                    LocalDateTime.now().minusHours(1)
            );
            if (recentRejections >= MAX_REJECTIONS_PER_HOUR) {
                decision = FraudDecision.REJECTED;
                reason = "Too many rejected transactions in the last hour";
            }
        }

        // Rule 3 — velocity check (too many transactions per hour)
        if (decision == FraudDecision.APPROVED) {
            long recentCount = fraudRepository.findByAccountIdAndEvaluatedAtAfter(
                    event.getAccountId(),
                    LocalDateTime.now().minusHours(1)
            ).size();
            if (recentCount >= MAX_TRANSACTIONS_PER_HOUR) {
                decision = FraudDecision.REJECTED;
                reason = "Transaction velocity exceeded for account: " + event.getAccountId();
            }
        }

        FraudEvaluation evaluation = FraudEvaluation.builder()
                .paymentId(event.getPaymentId())
                .accountId(event.getAccountId())
                .amount(event.getAmount())
                .decision(decision)
                .reason(reason)
                .build();

        FraudEvaluation saved = fraudRepository.save(evaluation);
        log.info("Fraud evaluation — paymentId={} decision={} reason={}", 
                event.getPaymentId(), decision, reason);

        return saved;
    }
}