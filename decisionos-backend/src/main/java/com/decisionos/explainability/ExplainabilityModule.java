package com.decisionos.explainability;

import com.decisionos.common.exceptions.LlmUnavailableException;
import com.decisionos.groq.LlmClient;
import com.decisionos.simulation.SimulationRun;
import com.decisionos.simulation.SimulationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class ExplainabilityService {

    private static final Logger log = LoggerFactory.getLogger(ExplainabilityService.class);

    private final SimulationService simulationService;
    private final LlmClient llmClient;

    @Transactional(readOnly = true)
    public Map<String, Object> explain(UUID runId) {
        SimulationRun run = simulationService.get(runId);
        String narrative;
        try {
            narrative = llmClient.complete(
                    "You explain pre-computed simulation results. Do NOT invent numbers. Only use the numbers given to you in the user message. If information is insufficient, say so explicitly.",
                    "Inputs: " + run.getInputSnapshot() + ". Aggregates: " + run.getAggregateStats()
                            + ". Risk: " + run.getRiskProfile()
                            + ". Explain in 3-4 sentences what drove the outcome, using only these numbers.");
        } catch (LlmUnavailableException e) {
            log.debug("LLM unavailable for explainability");
            narrative = "LLM explanation unavailable. Numeric results: " + run.getAggregateStats();
        }
        return Map.of("runId", run.getId().toString(), "narrative", narrative,
                "aggregateStats", run.getAggregateStats(), "riskProfile", run.getRiskProfile(),
                "assumptions", run.getAssumptions());
    }
}

@RestController
@RequiredArgsConstructor
class ExplainabilityController {

    private final ExplainabilityService service;

    @GetMapping("/api/v1/simulation-runs/{id}/explanation")
    public ResponseEntity<Map<String, Object>> explain(@PathVariable UUID id) {
        return ResponseEntity.ok(service.explain(id));
    }
}
