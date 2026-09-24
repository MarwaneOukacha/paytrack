package com.paytrack.paytrackpaymentservice.mapper;

import com.paytrack.paytrackpaymentservice.entity.LimitConfig;
import com.paytrack.shared.dto.LimitConfigDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LimitConfigMapper {

    LimitConfigDto toDto(LimitConfig config);
}