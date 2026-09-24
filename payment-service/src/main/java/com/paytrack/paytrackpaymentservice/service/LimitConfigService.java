package com.paytrack.paytrackpaymentservice.service;

import com.paytrack.paytrackpaymentservice.entity.LimitConfig;
import com.paytrack.shared.dto.LimitConfigDto;
import com.paytrack.shared.dto.UpdateLimitConfigRequest;

public interface LimitConfigService {

    LimitConfigDto getConfig();

    LimitConfigDto updateConfig(UpdateLimitConfigRequest request);

    LimitConfig getConfigEntity();
}