package com.paytrack.paytrackpaymentservice.mapper;

import com.paytrack.paytrackpaymentservice.entity.SystemSetting;
import com.paytrack.shared.dto.SystemSettingDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SystemSettingMapper {

    SystemSettingDto toDto(SystemSetting setting);
}