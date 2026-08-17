package com.paytrack.fraudservice.consumer;

import com.paytrack.fraudservice.entity.FraudEvaluation;
import com.paytrack.fraudservice.mapper.FraudMapper;
import com.paytrack.fraudservice.producer.FraudProducer;
import com.paytrack.fraudservice.service.FraudService;
import com.paytrack.shared.dto.FraudEvent;
import com.paytrack.shared.dto.PaymentEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraudConsumer {

    private final FraudService fraudService;
    private final FraudProducer fraudProducer;
    private final FraudMapper fraudMapper;
    private static final Logger log = LoggerFactory.getLogger(FraudConsumer.class);

    @KafkaListener(topics = "fraud.fraud-check", groupId = "fraud-group")
    @Transactional
    public void consume(PaymentEvent event,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {

        log.info("Fraud check received — paymentId={} partition={}", 
                event.getPaymentId(), partition);

        FraudEvaluation evaluation = fraudService.evaluate(event);
        FraudEvent fraudEvent = fraudMapper.toFraudEvent(evaluation, event);
        fraudProducer.publish(fraudEvent);
    }
}