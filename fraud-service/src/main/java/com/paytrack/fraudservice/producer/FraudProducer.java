package com.paytrack.fraudservice.producer;

import com.paytrack.shared.dto.FraudEvent;
import com.paytrack.shared.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraudProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(FraudProducer.class);

    public void publish(FraudEvent event, PaymentEvent paymentEvent) {

        kafkaTemplate.send(
                "payment.initiated",
                String.valueOf(event.getPaymentId()),
                paymentEvent
        );
        log.info("Fraud result published — paymentId={} decision={}",
                event.getPaymentId(), event.getDecision());
    }
}