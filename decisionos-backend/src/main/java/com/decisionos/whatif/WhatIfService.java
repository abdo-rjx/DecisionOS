package com.decisionos.whatif;

import com.decisionos.common.exceptions.NotFoundException;
import com.decisionos.scenario.Scenario;
import com.decisionos.scenario.ScenarioRepository;
import com.decisionos.simulation.SimulationRun;
import com.decisionos.simulation.SimulationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WhatIfService {

    private final ScenarioRepository scenarioRepository;
    private final SimulationService simulationService;

    @Transactional
    public Map<String, Object> runVariants(UUID scenarioId, List<Map<String, Object>> variants, int horizon) {
        Scenario base = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new NotFoundException("Scenario not found: " + scenarioId));
        List<Map<String, Object>> results = new ArrayList<>();
        Map<String, Object> baseMods = base.getExternalConditionModifiers();
        // Variant 0 = baseline scenario as-is.
        SimulationRun baseline = simulationService.simulate(scenarioId, horizon);
        results.add(Map.of("variant", "baseline", "run", baseline));
        int i = 1;
        for (Map<String, Object> variant : variants.subList(0, Math.min(2, variants.size()))) {
            Map<String, Object> merged = new HashMap<>(baseMods);
            merged.putAll(variant);
            Scenario tmp = new Scenario();
            tmp.setDecisionId(base.getDecisionId());
            tmp.setType(base.getType());
            tmp.setProbabilityWeight(base.getProbabilityWeight());
            tmp.setExternalConditionModifiers(merged);
            tmp.setActiveFactors(new ArrayList<>(base.getActiveFactors()));
            tmp.setNarrative("What-if variant " + i);
            Scenario saved = scenarioRepository.save(tmp);
            SimulationRun run = simulationService.simulate(saved.getId(), horizon);
            results.add(Map.of("variant", "variant-" + i, "overrides", variant, "run", run));
            i++;
        }
        return Map.of("results", results, "count", results.size());
    }
}
