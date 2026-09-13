package com.decisionos.scenario;

import com.decisionos.common.exceptions.LlmUnavailableException;
import com.decisionos.decision.Decision;
import com.decisionos.decision.DecisionService;
import com.decisionos.externalfactor.ExternalFactor;
import com.decisionos.externalfactor.ExternalFactorService;
import com.decisionos.groq.LlmClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScenarioService {

    private static final Logger log = LoggerFactory.getLogger(ScenarioService.class);

    private final ScenarioRepository repository;
    private final DecisionService decisionService;
    private final ExternalFactorService factorService;
    private final LlmClient llmClient;

    @Transactional
    public List<Scenario> generate(UUID decisionId) {
        Decision d = decisionService.get(decisionId);
        repository.findByDecisionId(decisionId).forEach(s -> repository.delete(s));
        List<Scenario> out = new ArrayList<>();
        out.add(make(d, ScenarioType.OPTIMISTIC, 1.0,
                Map.of("sales", 0.15, "revenue", 0.15, "marketGrowthDelta", 0.05), new ArrayList<>()));
        out.add(make(d, ScenarioType.EXPECTED, 1.0,
                Map.of("sales", 0.0, "revenue", 0.0, "marketGrowthDelta", 0.0), new ArrayList<>()));
        out.add(make(d, ScenarioType.PESSIMISTIC, 1.0,
                Map.of("sales", -0.10, "revenue", -0.10, "marketGrowthDelta", -0.05), new ArrayList<>()));
        out.add(make(d, ScenarioType.EXTREME, 0.5,
                Map.of("sales", -0.25, "revenue", -0.25, "marketGrowthDelta", -0.12), new ArrayList<>()));
        List<Scenario> saved = repository.saveAll(out);
        enrichNarratives(saved, d.getTitle());
        return repository.findByDecisionId(decisionId);
    }

    @Transactional(readOnly = true)
    public List<Scenario> list(UUID decisionId) {
        return repository.findByDecisionId(decisionId);
    }

    @Transactional
    public Scenario toggleFactor(UUID scenarioId, UUID factorId) {
        Scenario s = repository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("Scenario not found: " + scenarioId));
        ExternalFactor f = factorService.get(factorId);
        List<String> active = new ArrayList<>(s.getActiveFactors());
        String key = f.getId().toString();
        Map<String, Object> mods = new HashMap<>(s.getExternalConditionModifiers());
        if (active.contains(key)) {
            active.remove(key);
            for (String k : f.getImpactModifiers().keySet()) {
                mods.remove(k);
            }
        } else {
            active.add(key);
            for (Map.Entry<String, Object> e : f.getImpactModifiers().entrySet()) {
                mods.merge(e.getKey(), e.getValue(), (a, b) -> num(a) + num(b));
            }
        }
        s.setActiveFactors(active);
        s.setExternalConditionModifiers(mods);
        return repository.save(s);
    }

    private Scenario make(Decision d, ScenarioType type, double weight,
                          Map<String, Object> mods, List<String> factors) {
        Scenario s = new Scenario();
        s.setDecisionId(d.getId());
        s.setType(type);
        s.setProbabilityWeight(weight);
        s.setExternalConditionModifiers(new HashMap<>(mods));
        s.setActiveFactors(new ArrayList<>(factors));
        s.setNarrative(fallbackNarrative(type, mods));
        return s;
    }

    private String fallbackNarrative(ScenarioType type, Map<String, Object> mods) {
        return switch (type) {
            case OPTIMISTIC -> "Favorable conditions with modifiers " + mods;
            case EXPECTED -> "Baseline conditions with modifiers " + mods;
            case PESSIMISTIC -> "Adverse conditions with modifiers " + mods;
            case EXTREME -> "Severe stress conditions with modifiers " + mods;
            default -> "Sampled conditions with modifiers " + mods;
        };
    }

    private void enrichNarratives(List<Scenario> scenarios, String decisionTitle) {
        for (Scenario s : scenarios) {
            try {
                String narrative = llmClient.complete(
                        "You describe pre-computed business scenarios. Do NOT invent numbers. Only use the numbers given to you in the user message. If information is insufficient, say so explicitly.",
                        "Decision: \"" + decisionTitle + "\". Scenario: " + s.getType()
                                + ". Modifiers: " + s.getExternalConditionModifiers()
                                + ". Write one short sentence describing the assumed conditions.");
                s.setNarrative(narrative);
                repository.save(s);
            } catch (LlmUnavailableException e) {
                log.debug("LLM unavailable for scenario narrative");
            }
        }
    }

    private double num(Object v) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return 0;
    }
}
