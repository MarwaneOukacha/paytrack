package com.paytrack.fraudservice.repository;

import com.paytrack.fraudservice.entity.FraudConfig;
import com.paytrack.shared.enums.FraudRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FraudConfigRepository extends JpaRepository<FraudConfig, UUID> {

    Optional<FraudConfig> findByRuleType(FraudRuleType ruleType);

    boolean existsByRuleType(FraudRuleType ruleType);

    List<FraudConfig> findAllByOrderByRuleTypeAsc();
}