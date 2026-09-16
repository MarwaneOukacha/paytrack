package com.paytrack.paytrackpaymentservice.controller;


import com.paytrack.paytrackpaymentservice.service.PaymentService;
import com.paytrack.paytrackpaymentservice.service.PaymentSseService;
import com.paytrack.shared.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentSseService paymentSseService;

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
    @GetMapping("/{id}")
    public ResponseEntity<PaymentDto> getPaymentById(
            @PathVariable String id) {

        return ResponseEntity.ok(
                paymentService.getPaymentById(UUID.fromString(id))
        );
    }
    @GetMapping("/stats")
    public ResponseEntity<PaymentStatsDto> getPaymentStats() {

        return ResponseEntity.ok(
                paymentService.getPaymentStats()
        );
    }

    @GetMapping(
            value = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter streamPayments() {

        return paymentSseService.subscribe();
    }

}