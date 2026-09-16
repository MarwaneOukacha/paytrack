package com.paytrack.paytrackpaymentservice.service;

import com.paytrack.paytrackpaymentservice.entity.Payment;
import com.paytrack.shared.dto.PaymentDto;
import com.paytrack.shared.dto.PaymentFilter;
import com.paytrack.shared.dto.TransferRequest;
import com.paytrack.shared.dto.TransferResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {

    TransferResponse initiateTransfer(TransferRequest request);

    Page<PaymentDto> getPayments(PaymentFilter filter, Pageable pageable);

}