package com.paytrack.fraudservice.service;

import com.paytrack.shared.dto.CreateFraudConfigRequest;
import com.paytrack.shared.dto.FraudConfigDto;
import com.paytrack.shared.dto.UpdateFraudConfigRequest;

import java.util.List;
import java.util.UUID;

public interface FraudConfigService {

    FraudConfigDto createConfig(CreateFraudConfigRequest request);

    List<FraudConfigDto> getConfigs();

    FraudConfigDto getConfigById(UUID id);

    FraudConfigDto updateConfig(UUID id, UpdateFraudConfigRequest request);

    FraudConfigDto toggleConfig(UUID id);

    void deleteConfig(UUID id);

    List<FraudConfigDto> resetDefaults();
}