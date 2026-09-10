package com.paytrack.paytrackpaymentservice.consumer;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.paytrackpaymentservice.repository.AccountRepository;
import com.paytrack.paytrackpaymentservice.repository.PaymentRepository;
import com.paytrack.paytrackpaymentservice.service.impl.PaymentServiceImp;
import com.paytrack.shared.dto.FraudEvent;
import com.paytrack.shared.dto.PaymentEvent;
import com.paytrack.shared.enums.FraudDecision;
import com.paytrack.shared.enums.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentConsumer {
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;
    private final PaymentRepository paymentRepository;
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImp.class);
    private final AccountRepository accountRepository;
    @KafkaListener(
            topics = "payment.initiated",
            groupId = "payment-group"
    )
    public void validate(PaymentEvent event,@Header(KafkaHeaders.RECEIVED_PARTITION) int partition){
        log.info("Received payment event from partition={}", partition);
        // 2. Retrieve source account
        Account source = accountRepository
                .findByAccountNumber(event.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source account not found: " + event.getAccountId()
                ));

        // 3. Retrieve destination account
        Account destination = accountRepository
                .findByAccountNumber(event.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination account not found: " + event.getAccountId()
                ));

        source.setBalance(source.getBalance().subtract(event.getAmount()));
        destination.setBalance(destination.getBalance().add(event.getAmount()));
        // Save the updated accounts
        accountRepository.save(source);
        accountRepository.save(destination);

        Payment payment=paymentRepository.findById(event.getPaymentId()).orElseThrow(() -> new IllegalArgumentException(
                "Payment not found: " + event.getPaymentId()
        ));
        payment.setStatus(PaymentStatus.PROCESSED);
        paymentRepository.save(payment);
        // Forward to fraud service for analysis
        event.setStatus(PaymentStatus.PROCESSED);
        kafkaTemplate.send("fraud.fraud-check", String.valueOf(event.getPaymentId()), event);
        log.info("Payment forwarded to fraud-check — paymentId={}", event.getPaymentId());
    }
    @KafkaListener(
            topics = "payment.failed",
            groupId = "payment-group"
    )
    public void handleFailedPayment(PaymentEvent event) {

        log.warn(
                "Received payment.failed — paymentId={} | reason={}",
                event.getPaymentId(),
                event.getDescription()
        );
        if (event.getPaymentId() == null) {
            log.info("payment.failed event with no paymentId — from={} reason={}",
                    event.getAccountId(), event.getDescription());
            return;
        }
        paymentRepository.findById(event.getPaymentId())
                .ifPresent(payment -> {

                    payment.setStatus(PaymentStatus.FAILED);

                    paymentRepository.save(payment);

                    log.info(
                            "Payment {} marked as FAILED",
                            event.getPaymentId()
                    );
                });
    }
    @KafkaListener(topics = "payment.fraud-check", groupId = "payment-service")
    @Transactional
    public void handleFraudResult(FraudEvent event,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition) {

        log.info("Fraud result received — paymentId={} decision={} partition={}",
                event.getPaymentId(), event.getDecision(), partition);

        Payment payment = paymentRepository.findById(event.getPaymentId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Payment not found: " + event.getPaymentId()));

        if (event.getDecision() == FraudDecision.APPROVED) {
            payment.setStatus(PaymentStatus.COMPLETED);
            log.info("Payment {} → COMPLETED", event.getPaymentId());
        } else {
            // Reverse the debit/credit
            Account source = accountRepository.findByAccountNumber(event.getAccountId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Source account not found: " + event.getAccountId()));

            Account destination = accountRepository.findByAccountNumber(event.getToAccountNumber())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Destination account not found: " + event.getToAccountNumber()));

            source.setBalance(source.getBalance().add(event.getAmount()));
            destination.setBalance(destination.getBalance().subtract(event.getAmount()));
            accountRepository.save(source);
            accountRepository.save(destination);

            payment.setStatus(PaymentStatus.REJECTED);
            log.warn("Payment {} → REJECTED — reason: {}", event.getPaymentId(), event.getReason());
        }

        paymentRepository.save(payment);
    }
}
