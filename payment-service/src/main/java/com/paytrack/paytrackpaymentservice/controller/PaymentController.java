package com.paytrack.paytrackpaymentservice.controller;


import com.paytrack.paytrackpaymentservice.service.PaymentService;
import com.paytrack.shared.dto.TransferRequest;
import com.paytrack.shared.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
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

}