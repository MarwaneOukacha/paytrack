package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.entity.OutboxEvent;
import com.paytrack.paytrackpaymentservice.mapper.PaymentEventMapper;
import com.paytrack.paytrackpaymentservice.repository.OutboxEventRepository;
import com.paytrack.shared.dto.PaymentEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final PaymentEventMapper paymentEventMapper;
    private static final Logger log =  LoggerFactory.getLogger(OutboxScheduler.class);

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findBySentFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : pending) {
            try {
                PaymentEvent paymentEvent = paymentEventMapper.fromJson(event.getPayload());
                kafkaTemplate.send(event.getTopic(), event.getKafkaKey(), paymentEvent);
                event.setSent(true);
                outboxRepository.save(event);
                log.info("Outbox event published — topic={} key={}", event.getTopic(), event.getKafkaKey());
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}", event.getId(), e);
            }
        }
    }
}