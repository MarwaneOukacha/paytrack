package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.config.ConfigSeeder;
import com.paytrack.paytrackpaymentservice.entity.SystemSetting;
import com.paytrack.paytrackpaymentservice.mapper.SystemSettingMapper;
import com.paytrack.paytrackpaymentservice.repository.SystemSettingRepository;
import com.paytrack.paytrackpaymentservice.service.SystemConfigService;
import com.paytrack.shared.dto.SystemSettingDto;
import com.paytrack.shared.dto.UpdateSystemSettingRequest;
import com.paytrack.shared.enums.ConfigCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemSettingRepository systemSettingRepository;
    private final SystemSettingMapper systemSettingMapper;
    private final ConfigSeeder configSeeder;

    @Override
    @Transactional(readOnly = true)
    public List<SystemSettingDto> getSettings(ConfigCategory category) {

        List<SystemSetting> settings = category != null
                ? systemSettingRepository.findByCategoryOrderByKeyAsc(category)
                : systemSettingRepository.findAllByOrderByCategoryAscKeyAsc();

        return settings.stream().map(systemSettingMapper::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SystemSettingDto getSetting(String key) {

        return systemSettingMapper.toDto(findByKey(key));
    }

    @Override
    @Transactional
    public SystemSettingDto updateSetting(String key, UpdateSystemSettingRequest request) {

        SystemSetting setting = findByKey(key);

        apply(setting, request);

        return systemSettingMapper.toDto(systemSettingRepository.save(setting));
    }

    @Override
    @Transactional
    public List<SystemSettingDto> updateSettings(List<UpdateSystemSettingRequest> requests) {

        List<SystemSettingDto> updated = new ArrayList<>();

        for (UpdateSystemSettingRequest request : requests) {
            if (request.getKey() == null || request.getKey().isBlank()) {
                throw new IllegalArgumentException(
                        "A key is required for each setting in a batch update"
                );
            }

            SystemSetting setting = findByKey(request.getKey());
            apply(setting, request);
            updated.add(systemSettingMapper.toDto(systemSettingRepository.save(setting)));
        }

        return updated;
    }

    @Override
    @Transactional
    public List<SystemSettingDto> resetDefaults() {

        systemSettingRepository.deleteAll();

        configSeeder.seedSystemSettingsIfEmpty();

        return getSettings(null);
    }

    private void apply(SystemSetting setting, UpdateSystemSettingRequest request) {

        if (request.getValue() != null) {
            setting.setValue(request.getValue());
        }
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }
        if (request.getUpdatedBy() != null && !request.getUpdatedBy().isBlank()) {
            setting.setUpdatedBy(request.getUpdatedBy());
        }
    }

    private SystemSetting findByKey(String key) {

        return systemSettingRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Setting not found with key: " + key
                ));
    }
}