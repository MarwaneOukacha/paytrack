package com.paytrack.paytrackpaymentservice.service;

import com.paytrack.shared.dto.PaymentDto;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface PaymentSseService {

    SseEmitter subscribe();

    void publishPayment(PaymentDto payment);
}