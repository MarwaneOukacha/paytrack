package com.paytrack.paytrackpaymentservice.mapper;

import com.paytrack.paytrackpaymentservice.entity.OutboxEvent;
import com.paytrack.shared.dto.PaymentEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
@Mapper(componentModel = "spring")
public interface PaymentEventMapper {

    // PaymentEvent → OutboxEvent
    // topic and kafkaKey come as separate params, payload is serialized JSON
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sent", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "sentAt", ignore = true)
    @Mapping(target = "topic", source = "topic")
    @Mapping(target = "kafkaKey", source = "kafkaKey")
    @Mapping(target = "payload", expression = "java(toJson(event))")
    OutboxEvent toOutboxEvent(PaymentEvent event, String topic, String kafkaKey);

    // OutboxEvent → PaymentEvent
    // payload is a JSON string, deserialize it directly
    default PaymentEvent toPaymentEvent(OutboxEvent outboxEvent) {
        return fromJson(outboxEvent.getPayload());
    }

    // Helpers
    default String toJson(PaymentEvent event) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize PaymentEvent", e);
        }
    }

    default PaymentEvent fromJson(String payload) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(payload, PaymentEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize PaymentEvent", e);
        }
    }
}