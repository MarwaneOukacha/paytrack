package com.paytrack.paytrackpaymentservice.service;

import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.shared.dto.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PaymentService {

    TransferResponse initiateTransfer(TransferRequest request);

    Page<PaymentDto> getPayments(PaymentFilter filter, Pageable pageable);
    PaymentDto getPaymentById(UUID id);
    PaymentStatsDto getPaymentStats();

}