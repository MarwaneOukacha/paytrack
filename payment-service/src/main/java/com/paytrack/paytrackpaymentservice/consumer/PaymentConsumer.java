package com.paytrack.paytrackpaymentservice.consumer;

import com.paytrack.paytrackpaymentservice.entity.Account;
import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.paytrackpaymentservice.repository.AccountRepository;
import com.paytrack.paytrackpaymentservice.repository.PaymentRepository;
import com.paytrack.paytrackpaymentservice.service.impl.PaymentServiceImp;
import com.paytrack.shared.dto.PaymentEvent;
import com.paytrack.shared.enums.PaymentStatus;
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
            groupId = "payment-service"
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
                .findByAccountNumber(event.getToAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Destination account not found: " + event.getToAccountNumber()
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
    }
}
