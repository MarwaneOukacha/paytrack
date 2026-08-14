package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.entity.OutboxEvent;
import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.paytrackpaymentservice.mapper.TransferMapper;
import com.paytrack.paytrackpaymentservice.repository.AccountRepository;
import com.paytrack.paytrackpaymentservice.repository.OutboxEventRepository;
import com.paytrack.paytrackpaymentservice.repository.PaymentRepository;
import com.paytrack.paytrackpaymentservice.service.PaymentService;
import com.paytrack.shared.dto.PaymentEvent;
import com.paytrack.shared.dto.TransferRequest;
import com.paytrack.shared.dto.TransferResponse;
import com.paytrack.shared.enums.AccountStatus;
import com.paytrack.shared.enums.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImp.class);

    private final TransferMapper                       transferMapper;
    private final AccountRepository                    accountRepository;
    private final PaymentRepository                    paymentRepository;
    private final OutboxEventRepository                outboxRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Override
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {

        // 1. Source and destination accounts must be different
        if (request.getFromAccountNumber().equals(request.getToAccountNumber())) {
            throw new IllegalArgumentException(
                    "The source and destination accounts must be different."
            );
        }

        // 2. Retrieve source account
        Account source = accountRepository
                .findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Source account not found: " + request.getFromAccountNumber()
                ));

        // 3. Retrieve destination account
        Account destination = accountRepository
                .findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination account not found: " + request.getToAccountNumber()
                ));

        // 4. Business validations
        if (!source.canTransfer(request.getAmount())) {
            throw new IllegalStateException(
                    "Insufficient balance or source account is inactive."
            );
        }
        if (destination.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Destination account is inactive or blocked."
            );
        }

        // 5. Save payment with PENDING status
        //    → no balance change here, happens in the consumer
        Payment payment = transferMapper.toDebitPayment(request);
        payment.setStatus(PaymentStatus.PENDING);
        Payment savedPayment = paymentRepository.save(payment);

        // 6. Build PaymentEvent
        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setPaymentId(savedPayment.getId());
        paymentEvent.setAccountId(request.getFromAccountNumber());
        paymentEvent.setToAccountNumber(request.getToAccountNumber());
        paymentEvent.setAmount(request.getAmount());
        paymentEvent.setCurrency(source.getCurrency());
        paymentEvent.setStatus(savedPayment.getStatus());
        paymentEvent.setDescription(request.getDescription());

        // 7. Save OutboxEvent (same transaction)
        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setSent(false);
        outboxEvent.setTopic("payment.initiated");
        outboxEvent.setPayload(paymentEvent.toString());
        outboxEvent.setKafkaKey(String.valueOf(savedPayment.getId()));
        outboxRepository.save(outboxEvent);

        // 8. Publish to Kafka
        //    → consumer will debit source + credit destination
        kafkaTemplate.send("payment.initiated", String.valueOf(savedPayment.getId()), paymentEvent);

        log.info("Transfer initiated — paymentId={} | {} → {} | {} {}",
                savedPayment.getId(),
                source.getAccountNumber(),
                destination.getAccountNumber(),
                request.getAmount(),
                source.getCurrency()
        );

        return transferMapper.toResponse(savedPayment, source);
    }

    @Override
    @Transactional
    public void markDebitProcessed(String paymentId) {
        paymentRepository.findById(UUID.fromString(paymentId))
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.PROCESSED);
                    paymentRepository.save(payment);
                    log.info("Payment {} → PROCESSED", paymentId);
                });
    }


    @Transactional
    @Override
    public void markDebitFailed(String paymentId) {
        paymentRepository.findById(UUID.fromString(paymentId))
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                    log.warn("Payment {} → FAILED", paymentId);
                });
    }
}
