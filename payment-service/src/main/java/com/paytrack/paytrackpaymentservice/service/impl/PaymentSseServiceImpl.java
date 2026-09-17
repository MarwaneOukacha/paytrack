package com.paytrack.paytrackpaymentservice.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytrack.paytrackpaymentservice.service.PaymentSseService;
import com.paytrack.shared.dto.PaymentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class PaymentSseServiceImpl implements PaymentSseService {
    private final ObjectMapper objectMapper=new ObjectMapper();
    private final List<SseEmitter> emitters =
            new CopyOnWriteArrayList<>();

    @Override
    public SseEmitter subscribe() {

        SseEmitter emitter = new SseEmitter(
                30 * 60 * 1000L
        );

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("Connected to payment stream")
            );
        } catch (IOException e) {
            emitters.remove(emitter);
        }

        log.info(
                "New payment SSE client connected. Clients: {}",
                emitters.size()
        );

        return emitter;
    }

    public void publishPayment(PaymentDto payment) {

        for (SseEmitter emitter : emitters) {

            try {

                String json = objectMapper.writeValueAsString(payment);

                emitter.send(
                        SseEmitter.event()
                                .name("payment")
                                .data(json)
                );

            } catch (Exception e) {

                emitter.completeWithError(e);
                emitters.remove(emitter);

                log.error("Error sending payment through SSE", e);
            }
        }
    }
}