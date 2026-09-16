package com.paytrack.paytrackpaymentservice.controller;


import com.paytrack.paytrackpaymentservice.service.PaymentService;
import com.paytrack.shared.dto.PaymentDto;
import com.paytrack.shared.dto.PaymentFilter;
import com.paytrack.shared.dto.TransferRequest;
import com.paytrack.shared.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> initiateTransfer(
            @RequestBody TransferRequest request) {

        TransferResponse response = paymentService.initiateTransfer(request);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<PaymentDto>> getPayments(
            @ModelAttribute PaymentFilter filter,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        Page<PaymentDto> payments =
                paymentService.getPayments(filter, pageable);

        return ResponseEntity.ok(payments);
    }

}