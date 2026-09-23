package com.paytrack.fraudservice.service.impl;

import com.paytrack.fraudservice.config.FraudConfigDefaults;
import com.paytrack.fraudservice.entity.FraudConfig;
import com.paytrack.fraudservice.entity.FraudEvaluation;
import com.paytrack.fraudservice.repository.FraudConfigRepository;
import com.paytrack.fraudservice.repository.FraudEvaluationRepository;
import com.paytrack.fraudservice.service.FraudService;
import com.paytrack.shared.dto.PaymentEvent;
import com.paytrack.shared.enums.FraudDecision;
import com.paytrack.shared.enums.FraudRuleType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FraudServiceImpl implements FraudService {

    private final FraudEvaluationRepository fraudRepository;
    private final FraudConfigRepository configRepository;
    private static final Logger log = LoggerFactory.getLogger(FraudServiceImpl.class);

    @Override
    @Transactional
    public FraudEvaluation evaluate(PaymentEvent event) {

        String reason = null;
        FraudDecision decision = FraudDecision.APPROVED;

        Map<FraudRuleType, FraudConfig> configs = configRepository.findAll().stream()
                .collect(Collectors.toMap(FraudConfig::getRuleType, Function.identity()));

        // Rule 1 — high amount
        FraudConfig highAmountConfig = configs.get(FraudRuleType.HIGH_AMOUNT);
        if (isActive(highAmountConfig)
                && event.getAmount().compareTo(resolveThreshold(
                        highAmountConfig, FraudConfigDefaults.HIGH_AMOUNT_THRESHOLD)) > 0) {
            decision = FraudDecision.REJECTED;
            reason = "Amount exceeds threshold: " + event.getAmount();
        }

        // Rule 2 — too many rejections in the sliding window
        FraudConfig rejectionConfig = configs.get(FraudRuleType.REJECTION_RATE);
        if (decision == FraudDecision.APPROVED && isActive(rejectionConfig)) {
            long recentRejections = fraudRepository.countByAccountIdAndDecisionAndEvaluatedAtAfter(
                    event.getAccountId(),
                    FraudDecision.REJECTED,
                    LocalDateTime.now().minusMinutes(resolveWindow(
                            rejectionConfig, FraudConfigDefaults.DEFAULT_WINDOW_MINUTES))
            );
            if (recentRejections >= rejectionConfig.getThreshold().intValue()) {
                decision = FraudDecision.REJECTED;
                reason = "Too many rejected transactions in the last "
                        + resolveWindow(rejectionConfig, FraudConfigDefaults.DEFAULT_WINDOW_MINUTES)
                        + " minutes";
            }
        }

        // Rule 3 — velocity check (too many transactions in the sliding window)
        FraudConfig velocityConfig = configs.get(FraudRuleType.VELOCITY);
        if (decision == FraudDecision.APPROVED && isActive(velocityConfig)) {
            long recentCount = fraudRepository.findByAccountIdAndEvaluatedAtAfter(
                    event.getAccountId(),
                    LocalDateTime.now().minusMinutes(resolveWindow(
                            velocityConfig, FraudConfigDefaults.DEFAULT_WINDOW_MINUTES))
            ).size();
            if (recentCount >= velocityConfig.getThreshold().intValue()) {
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

    private boolean isActive(FraudConfig config) {
        return config != null && Boolean.TRUE.equals(config.getEnabled());
    }

    private BigDecimal resolveThreshold(FraudConfig config, BigDecimal fallback) {
        return config.getThreshold() != null ? config.getThreshold() : fallback;
    }

    private int resolveWindow(FraudConfig config, int fallback) {
        Integer window = config.getWindowMinutes();
        return window != null && window > 0 ? window : fallback;
    }
}