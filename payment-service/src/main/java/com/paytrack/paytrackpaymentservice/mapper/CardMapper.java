package com.paytrack.paytrackpaymentservice.mapper;

import com.paytrack.paytrackpaymentservice.entity.Card;
import com.paytrack.shared.dto.CardDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "cardNumber", source = "maskedNumber")
    CardDto toDto(Card card);
}