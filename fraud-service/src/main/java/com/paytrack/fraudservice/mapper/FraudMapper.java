package com.paytrack.fraudservice.mapper;

import com.paytrack.fraudservice.entity.FraudEvaluation;
import com.paytrack.shared.dto.FraudEvent;
import com.paytrack.shared.dto.PaymentEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FraudMapper {

    @Mapping(target = "paymentId", source = "evaluation.paymentId")
    @Mapping(target = "decision", source = "evaluation.decision")
    @Mapping(target = "reason", source = "evaluation.reason")
    @Mapping(target = "accountId", source = "evaluation.accountId")
    @Mapping(target = "amount", source = "evaluation.amount")
    @Mapping(target = "currency", source = "evaluation.currency")
    @Mapping(target = "status", source = "originalEvent.status")
    @Mapping(target = "toAccountNumber", source = "originalEvent.toAccountNumber")
    @Mapping(target = "description", source = "originalEvent.description")
    FraudEvent toFraudEvent(FraudEvaluation evaluation, PaymentEvent originalEvent);
}