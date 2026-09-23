package com.paytrack.fraudservice.service.impl;

import com.paytrack.fraudservice.config.FraudConfigDefaults;
import com.paytrack.fraudservice.entity.FraudConfig;
import com.paytrack.fraudservice.mapper.FraudConfigMapper;
import com.paytrack.fraudservice.repository.FraudConfigRepository;
import com.paytrack.fraudservice.service.FraudConfigService;
import com.paytrack.shared.dto.CreateFraudConfigRequest;
import com.paytrack.shared.dto.FraudConfigDto;
import com.paytrack.shared.dto.UpdateFraudConfigRequest;
import com.paytrack.shared.enums.FraudRuleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FraudConfigServiceImpl implements FraudConfigService {

    private final FraudConfigRepository configRepository;
    private final FraudConfigMapper configMapper;

    @Override
    @Transactional
    public FraudConfigDto createConfig(CreateFraudConfigRequest request) {

        FraudRuleType ruleType = request.getRuleType();

        if (configRepository.existsByRuleType(ruleType)) {
            throw new IllegalArgumentException(
                    "A fraud config already exists for rule type: " + ruleType
            );
        }

        FraudConfig config = configMapper.toEntity(request);

        if (config.getEnabled() == null) {
            config.setEnabled(true);
        }
        if (config.getWindowMinutes() == null) {
            config.setWindowMinutes(FraudConfigDefaults.DEFAULT_WINDOW_MINUTES);
        }

        validateValues(
                config.getRuleType(),
                config.getThreshold(),
                config.getWindowMinutes()
        );

        return configMapper.toDto(configRepository.save(config));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FraudConfigDto> getConfigs() {
        return configRepository.findAllByOrderByRuleTypeAsc().stream()
                .map(configMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FraudConfigDto getConfigById(UUID id) {
        return configMapper.toDto(findById(id));
    }

    @Override
    @Transactional
    public FraudConfigDto updateConfig(UUID id, UpdateFraudConfigRequest request) {

        FraudConfig config = findById(id);

        if (request.getRuleType() != null && request.getRuleType() != config.getRuleType()) {
            if (configRepository.existsByRuleType(request.getRuleType())) {
                throw new IllegalArgumentException(
                        "A fraud config already exists for rule type: " + request.getRuleType()
                );
            }
            config.setRuleType(request.getRuleType());
        }

        if (request.getEnabled() != null) {
            config.setEnabled(request.getEnabled());
        }
        if (request.getThreshold() != null) {
            config.setThreshold(request.getThreshold());
        }
        if (request.getWindowMinutes() != null) {
            config.setWindowMinutes(request.getWindowMinutes());
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            config.setDescription(request.getDescription());
        }
        if (request.getUpdatedBy() != null && !request.getUpdatedBy().isBlank()) {
            config.setUpdatedBy(request.getUpdatedBy());
        }

        validateValues(
                config.getRuleType(),
                config.getThreshold(),
                config.getWindowMinutes()
        );

        return configMapper.toDto(configRepository.save(config));
    }

    @Override
    @Transactional
    public FraudConfigDto toggleConfig(UUID id) {

        FraudConfig config = findById(id);
        config.setEnabled(!Boolean.TRUE.equals(config.getEnabled()));

        return configMapper.toDto(configRepository.save(config));
    }

    @Override
    @Transactional
    public void deleteConfig(UUID id) {

        FraudConfig config = findById(id);
        configRepository.delete(config);
    }

    @Override
    @Transactional
    public List<FraudConfigDto> resetDefaults() {

        for (FraudRuleType type : FraudRuleType.values()) {
            FraudConfig defaults = FraudConfigDefaults.newConfig(type);
            FraudConfig config = configRepository.findByRuleType(type).orElse(null);

            if (config == null) {
                configRepository.save(defaults);
            } else {
                config.setEnabled(true);
                config.setThreshold(defaults.getThreshold());
                config.setWindowMinutes(defaults.getWindowMinutes());
                config.setDescription(defaults.getDescription());
                config.setUpdatedBy("system");
                configRepository.save(config);
            }
        }

        return getConfigs();
    }

    private FraudConfig findById(UUID id) {
        return configRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Fraud config not found with id: " + id
                ));
    }

    private void validateValues(FraudRuleType ruleType, BigDecimal threshold, Integer windowMinutes) {

        if (threshold == null) {
            throw new IllegalArgumentException("Threshold is required");
        }

        if (threshold.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Threshold must be greater than zero");
        }

        if (ruleType == FraudRuleType.REJECTION_RATE || ruleType == FraudRuleType.VELOCITY) {
            if (threshold.stripTrailingZeros().scale() > 0) {
                throw new IllegalArgumentException(
                        "Threshold must be a whole number for rule type: " + ruleType
                );
            }
            if (windowMinutes == null || windowMinutes < 1) {
                throw new IllegalArgumentException(
                        "Window (minutes) is required for rule type: " + ruleType
                );
            }
        }
    }
}