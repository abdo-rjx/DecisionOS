package com.decisionos.recommendation;

import com.decisionos.common.exceptions.LlmUnavailableException;
import com.decisionos.groq.LlmClient;
import com.decisionos.scenario.Scenario;
import com.decisionos.scenario.ScenarioRepository;
import com.decisionos.simulation.SimulationRun;
import com.decisionos.simulation.SimulationRunRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationService.class);

    private final ScenarioRepository scenarioRepository;
    private final SimulationRunRepository runRepository;
    private final LlmClient llmClient;

    @Transactional(readOnly = true)
    public Map<String, Object> recommend(UUID decisionId, Map<String, Double> priorities) {
        List<Scenario> scenarios = scenarioRepository.findByDecisionId(decisionId);
        double wGrowth = num(priorities.get("growth"), 1.0);
        double wRisk = num(priorities.get("risk"), 1.0);
        double wCost = num(priorities.get("cost"), 1.0);
        List<Map<String, Object>> ranked = new ArrayList<>();
        for (Scenario s : scenarios) {
            List<SimulationRun> runs = runRepository.findByScenarioIdOrderByCreatedAtDesc(s.getId());
            if (runs.isEmpty()) {
                continue;
            }
            SimulationRun latest = runs.get(0);
            double growth = mean(latest.getAggregateStats(), "revenue");
            double loss = num(((Map<String, Object>) latest.getRiskProfile()).get("potentialLoss"), 0);
            double score = wGrowth * norm(growth) - wRisk * norm(loss) - wCost * norm(loss);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("scenarioId", s.getId().toString());
            row.put("scenarioType", s.getType().name());
            row.put("score", score);
            row.put("growth", growth);
            row.put("riskLevel", latest.getRiskProfile().get("riskLevel"));
            row.put("runId", latest.getId().toString());
            ranked.add(row);
        }
        ranked.sort((a, b) -> Double.compare(num(b.get("score"), 0), num(a.get("score"), 0)));
        String justification = justify(ranked);
        return Map.of("ranking", ranked, "justification", justification,
                "topScenario", ranked.isEmpty() ? null : ranked.get(0));
    }

    private String justify(List<Map<String, Object>> ranked) {
        if (ranked.isEmpty()) {
            return "No simulation runs available yet. Run simulations first.";
        }
        try {
            return llmClient.complete(
                    "You justify a pre-computed scenario ranking. Do NOT invent numbers. Only use the numbers given to you in the user message. If information is insufficient, say so explicitly.",
                    "Ranking data: " + ranked + ". In 2-3 sentences, justify the top-ranked scenario using only these numbers.");
        } catch (LlmUnavailableException e) {
            log.debug("LLM unavailable for recommendation justification");
            Map<String, Object> top = ranked.get(0);
            return "Top scenario: " + top.get("scenarioType") + " with score " + top.get("score")
                    + " (LLM justification unavailable).";
        }
    }

    private double mean(Map<String, Object> stats, String key) {
        Object o = stats == null ? null : stats.get(key);
        if (o instanceof Map<?, ?> m) {
            return num(m.get("mean"), 0);
        }
        return 0;
    }

    private double norm(double v) {
        if (!Double.isFinite(v)) {
            return 0;
        }
        return v / (1 + Math.abs(v));
    }

    private double num(Object v, double fallback) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }
}
