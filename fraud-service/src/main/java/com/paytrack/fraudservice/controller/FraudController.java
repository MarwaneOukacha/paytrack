package com.paytrack.fraudservice.controller;

import com.paytrack.fraudservice.dto.FraudEvaluationDto;
import com.paytrack.fraudservice.dto.FraudStatsDto;
import com.paytrack.fraudservice.entity.FraudEvaluation;
import com.paytrack.fraudservice.repository.FraudEvaluationRepository;
import com.paytrack.shared.enums.FraudDecision;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudEvaluationRepository fraudRepository;

    @GetMapping("/evaluations")
    public List<FraudEvaluationDto> getEvaluations(
            @RequestParam(defaultValue = "false") boolean rejectedOnly,
            @RequestParam(defaultValue = "100") int limit) {

        PageRequest pageable = PageRequest.of(0, Math.max(1, Math.min(limit, 500)));

        List<FraudEvaluation> evaluations = rejectedOnly
                ? fraudRepository.findAllByDecisionOrderByEvaluatedAtDesc(FraudDecision.REJECTED, pageable)
                : fraudRepository.findAllByOrderByEvaluatedAtDesc(pageable);

        return evaluations.stream().map(this::toDto).toList();
    }

    @GetMapping("/stats")
    public FraudStatsDto getStats() {

        FraudStatsDto dto = new FraudStatsDto();

        dto.setTotalEvaluations(fraudRepository.count());
        dto.setApprovedCount(fraudRepository.countByDecision(FraudDecision.APPROVED));
        dto.setRejectedCount(fraudRepository.countByDecision(FraudDecision.REJECTED));

        return dto;
    }

    private FraudEvaluationDto toDto(FraudEvaluation evaluation) {

        FraudEvaluationDto dto = new FraudEvaluationDto();

        dto.setId(evaluation.getId());
        dto.setPaymentId(evaluation.getPaymentId());
        dto.setAccountId(evaluation.getAccountId());
        dto.setAmount(evaluation.getAmount());
        dto.setCurrency(evaluation.getCurrency());
        dto.setDecision(evaluation.getDecision());
        dto.setReason(evaluation.getReason());
        dto.setEvaluatedAt(evaluation.getEvaluatedAt());

        return dto;
    }
}