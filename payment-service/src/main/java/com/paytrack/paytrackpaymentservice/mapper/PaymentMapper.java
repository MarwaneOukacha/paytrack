package com.paytrack.paytrackpaymentservice.mapper;

import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.shared.dto.PaymentDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentDto toDto(Payment payment);
}