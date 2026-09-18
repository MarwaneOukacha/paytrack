package com.paytrack.fraudservice.repository;


import com.paytrack.fraudservice.entity.FraudEvaluation;
import com.paytrack.shared.enums.FraudDecision;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface FraudEvaluationRepository extends JpaRepository<FraudEvaluation, UUID> {
    List<FraudEvaluation> findByAccountIdAndEvaluatedAtAfter(String accountId, LocalDateTime since);
    long countByAccountIdAndDecisionAndEvaluatedAtAfter(String accountId, FraudDecision decision, LocalDateTime since);
    List<FraudEvaluation> findAllByOrderByEvaluatedAtDesc(Pageable pageable);
    List<FraudEvaluation> findAllByDecisionOrderByEvaluatedAtDesc(FraudDecision decision, Pageable pageable);
    long countByDecision(FraudDecision decision);
}