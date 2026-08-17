package com.paytrack.fraudservice.producer;

import com.paytrack.shared.dto.FraudEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraudProducer {

    private final KafkaTemplate<String, FraudEvent> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(FraudProducer.class);

    public void publish(FraudEvent event) {
        kafkaTemplate.send(
                "fraud.fraud-evaluated",
                String.valueOf(event.getPaymentId()),
                event
        );
        log.info("Fraud result published — paymentId={} decision={}", 
                event.getPaymentId(), event.getDecision());
    }
}