package com.paytrack.paytrackpaymentservice.service.impl;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.entity.OutboxEvent;
import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.paytrackpaymentservice.mapper.PaymentEventMapper;
import com.paytrack.paytrackpaymentservice.mapper.PaymentMapper;
import com.paytrack.paytrackpaymentservice.mapper.TransferMapper;
import com.paytrack.paytrackpaymentservice.repository.AccountRepository;
import com.paytrack.paytrackpaymentservice.repository.OutboxEventRepository;
import com.paytrack.paytrackpaymentservice.repository.PaymentRepository;
import com.paytrack.paytrackpaymentservice.service.PaymentService;
import com.paytrack.paytrackpaymentservice.service.PaymentSseService;
import com.paytrack.paytrackpaymentservice.specification.PaymentSpecification;
import com.paytrack.shared.dto.*;
import com.paytrack.shared.enums.AccountStatus;
import com.paytrack.shared.enums.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImp implements PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentServiceImp.class);
    private final PaymentSseService paymentSseService;
    private static final String FAILED_TOPIC = "payment.failed";
    private final PaymentMapper paymentMapper;
    private final PaymentEventMapper paymentEventMapper;
    private final TransferMapper transferMapper;
    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    @Override
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request) {

        Payment savedPayment = null;

        try {

            // 1. Source and destination accounts must be different
            if (request.getFromAccountNumber()
                    .equals(request.getToAccountNumber())) {

                throw new IllegalArgumentException(
                        "The source and destination accounts must be different."
                );
            }

            // 2. Retrieve source account
            Account source = accountRepository
                    .findByAccountNumber(request.getFromAccountNumber())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Source account not found: "
                                    + request.getFromAccountNumber()
                    ));

            // 3. Retrieve destination account
            Account destination = accountRepository
                    .findByAccountNumber(request.getToAccountNumber())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Destination account not found: "
                                    + request.getToAccountNumber()
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

            // 5. Save payment
            Payment payment = transferMapper.toDebitPayment(request);
            payment.setStatus(PaymentStatus.PENDING);

            savedPayment = paymentRepository.save(payment);

            source.debit(request.getAmount());
            destination.credit(request.getAmount());

            // 6. Build PaymentEvent
            PaymentEvent paymentEvent = new PaymentEvent();

            paymentEvent.setPaymentId(savedPayment.getId());
            paymentEvent.setAccountId(request.getFromAccountNumber());
            paymentEvent.setAccountId(request.getToAccountNumber());
            paymentEvent.setAmount(request.getAmount());
            //paymentEvent.setCurrency(source.getCurrency());
            paymentEvent.setStatus(PaymentStatus.PENDING);
            paymentEvent.setDescription(request.getDescription());

            // 7. Save outbox event
            OutboxEvent outboxEvent = paymentEventMapper.toOutboxEvent(
                    paymentEvent,
                    "payment.initiated",
                    String.valueOf(savedPayment.getId())
            );
            PaymentDto paymentDto =
                    paymentMapper.toDto(savedPayment);

            paymentSseService.publishPayment(paymentDto);
            outboxEvent.setSent(false);
            outboxRepository.save(outboxEvent);

            log.info(
                    "Transfer initiated — paymentId={} | {} → {} | {} {}",
                    savedPayment.getId(),
                    source.getAccountNumber(),
                    destination.getAccountNumber(),
                    request.getAmount(),
                    source.getCurrency()
            );

            return transferMapper.toResponse(savedPayment, source);

        } catch (RuntimeException exception) {

            // Publish FAILED event
            //TODO:: YOU SHOULD SAVE THE PAYMENT IN CASE OF REJECTED
            Payment payment = transferMapper.toDebitPayment(request);
            payment.setStatus(PaymentStatus.FAILED);

            savedPayment = paymentRepository.save(payment);
            publishFailedEvent(request, savedPayment, exception);
            throw exception;

        }
    }

    @Override
    public PaymentStatsDto getPaymentStats() {

        List<Payment> payments = paymentRepository.findAll();

        PaymentStatsDto stats = new PaymentStatsDto();

        stats.setTotalPayments(payments.size());

        BigDecimal totalAmount = payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        stats.setTotalAmount(totalAmount);

        List<Payment> successful = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PROCESSED)
                .toList();

        List<Payment> pending = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                .toList();

        List<Payment> failed = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.FAILED)
                .toList();

        stats.setSuccessfulPayments(successful.size());

        stats.setSuccessfulAmount(
                successful.stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        stats.setPendingPayments(pending.size());

        stats.setPendingAmount(
                pending.stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        stats.setFailedPayments(failed.size());

        stats.setFailedAmount(
                failed.stream()
                        .map(Payment::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        if (!payments.isEmpty()) {
            stats.setAverageAmount(
                    totalAmount.divide(
                            BigDecimal.valueOf(payments.size()),
                            2,
                            RoundingMode.HALF_UP
                    )
            );
        } else {
            stats.setAverageAmount(BigDecimal.ZERO);
        }

        Map<String, Long> paymentsByStatus = payments.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getStatus().name(),
                        Collectors.counting()
                ));

        stats.setPaymentsByStatus(paymentsByStatus);

        return stats;
    }

    @Override
    public PaymentDto getPaymentById(UUID id) {

        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found with id: " + id
                        )
                );

        return paymentMapper.toDto(payment);
    }

    @Override
    public Page<PaymentDto> getPayments(PaymentFilter filter, Pageable pageable) {

        Specification<Payment> specification = Specification
                .where(PaymentSpecification.hasAccountId(filter.getAccountId()))
                .and(PaymentSpecification.hasToAccountNumber(
                        filter.getToAccountNumber()))
                .and(PaymentSpecification.hasStatus(
                        filter.getStatus()))
                .and(PaymentSpecification.amountGreaterThanOrEqualTo(
                        filter.getMinAmount()))
                .and(PaymentSpecification.amountLessThanOrEqualTo(
                        filter.getMaxAmount()))
                .and(PaymentSpecification.createdAfter(
                        filter.getFromDate()))
                .and(PaymentSpecification.createdBefore(
                        filter.getToDate()));


        return paymentRepository
                .findAll(specification, pageable)
                .map(paymentMapper::toDto);
    }

    private void publishFailedEvent(
            TransferRequest request,
            Payment payment,
            Exception exception) {

        PaymentEvent failedEvent = new PaymentEvent();

        if (payment != null) {
            failedEvent.setPaymentId(payment.getId());
        }

        failedEvent.setAccountId(request.getFromAccountNumber());
        failedEvent.setToAccountNumber(request.getToAccountNumber());
        failedEvent.setAmount(request.getAmount());

        failedEvent.setStatus(PaymentStatus.FAILED);

        failedEvent.setDescription(
                exception.getMessage()
        );

        kafkaTemplate.send(FAILED_TOPIC, request.getFromAccountNumber(), failedEvent)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish payment.failed — paymentId={}",
                                payment != null ? payment.getId() : null, ex);
                    } else {
                        log.info("payment.failed ACKED — offset={}", result.getRecordMetadata().offset());
                    }
                });

        log.error(
                "Transfer failed — from={} | to={} | reason={}",
                request.getFromAccountNumber(),
                request.getToAccountNumber(),
                exception.getMessage()

        );
    }
}