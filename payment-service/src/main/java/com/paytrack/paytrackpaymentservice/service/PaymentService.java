package com.paytrack.paytrackpaymentservice.service;

import com.paytrack.shared.dto.TransferRequest;
import com.paytrack.shared.dto.TransferResponse;
import jakarta.transaction.Transactional;

public interface PaymentService {

    TransferResponse initiateTransfer(TransferRequest request);

    void markDebitProcessed(String paymentId);

    @Transactional
    void markDebitFailed(String paymentId);
}