package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.entity.LimitConfig;
import com.paytrack.paytrackpaymentservice.mapper.LimitConfigMapper;
import com.paytrack.paytrackpaymentservice.repository.LimitConfigRepository;
import com.paytrack.paytrackpaymentservice.service.LimitConfigService;
import com.paytrack.shared.dto.LimitConfigDto;
import com.paytrack.shared.dto.UpdateLimitConfigRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LimitConfigServiceImpl implements LimitConfigService {

    private final LimitConfigRepository limitConfigRepository;
    private final LimitConfigMapper limitConfigMapper;

    @Override
    @Transactional(readOnly = true)
    public LimitConfigDto getConfig() {

        return limitConfigMapper.toDto(getConfigEntity());
    }

    @Override
    @Transactional
    public LimitConfigDto updateConfig(UpdateLimitConfigRequest request) {

        LimitConfig config = getConfigEntity();

        if (request.getDefaultCurrency() != null && !request.getDefaultCurrency().isBlank()) {
            config.setDefaultCurrency(request.getDefaultCurrency().trim().toUpperCase());
        }
        if (request.getDefaultSingleTransactionLimit() != null) {
            config.setDefaultSingleTransactionLimit(request.getDefaultSingleTransactionLimit());
        }
        if (request.getDefaultDailyLimit() != null) {
            config.setDefaultDailyLimit(request.getDefaultDailyLimit());
        }
        if (request.getDefaultMonthlyLimit() != null) {
            config.setDefaultMonthlyLimit(request.getDefaultMonthlyLimit());
        }
        if (request.getMaxSingleTransactionLimit() != null) {
            config.setMaxSingleTransactionLimit(request.getMaxSingleTransactionLimit());
        }
        if (request.getMaxDailyLimit() != null) {
            config.setMaxDailyLimit(request.getMaxDailyLimit());
        }
        if (request.getMaxMonthlyLimit() != null) {
            config.setMaxMonthlyLimit(request.getMaxMonthlyLimit());
        }

        validate(config);

        return limitConfigMapper.toDto(limitConfigRepository.save(config));
    }

    @Override
    @Transactional
    public LimitConfig getConfigEntity() {

        return limitConfigRepository.findAll().stream()
                .findFirst()
                .orElseGet(this::createDefault);
    }

    private LimitConfig createDefault() {

        LimitConfig config = new LimitConfig();
        return limitConfigRepository.save(config);
    }

    private void validate(LimitConfig config) {

        if (greaterThan(config.getDefaultSingleTransactionLimit(), config.getMaxSingleTransactionLimit())) {
            throw new IllegalArgumentException(
                    "Default single transaction limit cannot exceed the max"
            );
        }
        if (greaterThan(config.getDefaultDailyLimit(), config.getMaxDailyLimit())) {
            throw new IllegalArgumentException(
                    "Default daily limit cannot exceed the max"
            );
        }
        if (greaterThan(config.getDefaultMonthlyLimit(), config.getMaxMonthlyLimit())) {
            throw new IllegalArgumentException(
                    "Default monthly limit cannot exceed the max"
            );
        }
        if (greaterThan(config.getDefaultSingleTransactionLimit(), config.getDefaultDailyLimit())) {
            throw new IllegalArgumentException(
                    "Default single transaction limit cannot exceed the default daily limit"
            );
        }
        if (greaterThan(config.getDefaultDailyLimit(), config.getDefaultMonthlyLimit())) {
            throw new IllegalArgumentException(
                    "Default daily limit cannot exceed the default monthly limit"
            );
        }
    }

    private boolean greaterThan(BigDecimal value, BigDecimal threshold) {

        return value != null && threshold != null && value.compareTo(threshold) > 0;
    }
}