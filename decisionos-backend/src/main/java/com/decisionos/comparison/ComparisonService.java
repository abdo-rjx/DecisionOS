package com.decisionos.comparison;

import com.decisionos.scenario.Scenario;
import com.decisionos.scenario.ScenarioRepository;
import com.decisionos.simulation.SimulationRun;
import com.decisionos.simulation.SimulationRunRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComparisonService {

    private final SimulationRunRepository runRepository;
    private final ScenarioRepository scenarioRepository;

    @Transactional(readOnly = true)
    public Map<String, Object> compareRuns(List<UUID> runIds) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (UUID id : runIds) {
            SimulationRun run = runRepository.findById(id).orElse(null);
            if (run == null) {
                continue;
            }
            Scenario s = run.getScenarioId() == null ? null
                    : scenarioRepository.findById(run.getScenarioId()).orElse(null);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("runId", run.getId().toString());
            row.put("scenarioType", s == null ? "?" : s.getType().name());
            row.put("aggregateStats", run.getAggregateStats());
            row.put("riskProfile", run.getRiskProfile());
            rows.add(row);
        }
        return Map.of("runs", rows, "count", rows.size());
    }
}
