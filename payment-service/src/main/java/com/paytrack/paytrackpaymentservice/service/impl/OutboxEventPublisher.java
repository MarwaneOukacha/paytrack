package com.paytrack.paytrackpaymentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytrack.paytrackpaymentservice.entity.OutboxEvent;
import com.paytrack.paytrackpaymentservice.repository.OutboxEventRepository;
import com.paytrack.shared.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Runs every 5 seconds.
     */
    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findBySentFalseOrderByCreatedAtAsc();

        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events", events.size());

        for (OutboxEvent event : events) {
            publishEvent(event);
        }
    }

    private void publishEvent(OutboxEvent event) {

        try {

            PaymentEvent paymentEvent =
                    objectMapper.readValue(
                            event.getPayload(),
                            PaymentEvent.class
                    );

            kafkaTemplate.send(
                    event.getTopic(),
                    event.getKafkaKey(),
                    paymentEvent
            ).whenComplete((result, exception) -> {

                if (exception != null) {

                    log.error(
                            "Failed to publish outbox event id={} topic={}",
                            event.getId(),
                            event.getTopic(),
                            exception
                    );

                    return;
                }

                markAsSent(event);

                log.info(
                        "Outbox event published successfully " +
                                "id={} topic={} key={} partition={} offset={}",
                        event.getId(),
                        event.getTopic(),
                        event.getKafkaKey(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
            });

        } catch (Exception exception) {

            log.error(
                    "Error processing outbox event id={}",
                    event.getId(),
                    exception
            );
        }
    }

    @Transactional
    protected void markAsSent(OutboxEvent event) {

        event.setSent(true);
        event.setSentAt(LocalDateTime.now());

        outboxEventRepository.save(event);
    }
}