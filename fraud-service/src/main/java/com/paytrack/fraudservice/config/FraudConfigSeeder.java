package com.paytrack.fraudservice.config;

import com.paytrack.fraudservice.repository.FraudConfigRepository;
import com.paytrack.shared.enums.FraudRuleType;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FraudConfigSeeder implements ApplicationRunner {

    private final FraudConfigRepository configRepository;
    private static final Logger log = LoggerFactory.getLogger(FraudConfigSeeder.class);

    @Override
    public void run(ApplicationArguments args) {
        for (FraudRuleType type : FraudRuleType.values()) {
            if (!configRepository.existsByRuleType(type)) {
                configRepository.save(FraudConfigDefaults.newConfig(type));
                log.info("Seeded default fraud config for rule type {}", type);
            }
        }
    }
}