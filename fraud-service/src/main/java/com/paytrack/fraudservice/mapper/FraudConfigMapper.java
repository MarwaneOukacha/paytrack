package com.paytrack.fraudservice.mapper;

import com.paytrack.fraudservice.entity.FraudConfig;
import com.paytrack.shared.dto.CreateFraudConfigRequest;
import com.paytrack.shared.dto.FraudConfigDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FraudConfigMapper {

    FraudConfigDto toDto(FraudConfig config);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FraudConfig toEntity(CreateFraudConfigRequest request);
}