package com.paytrack.paytrackpaymentservice.service;

import com.paytrack.shared.dto.SystemSettingDto;
import com.paytrack.shared.dto.UpdateSystemSettingRequest;
import com.paytrack.shared.enums.ConfigCategory;

import java.util.List;

public interface SystemConfigService {

    List<SystemSettingDto> getSettings(ConfigCategory category);

    SystemSettingDto getSetting(String key);

    SystemSettingDto updateSetting(String key, UpdateSystemSettingRequest request);

    List<SystemSettingDto> updateSettings(List<UpdateSystemSettingRequest> requests);

    List<SystemSettingDto> resetDefaults();
}