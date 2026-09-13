package com.decisionos.dashboard;

import com.decisionos.decision.Decision;
import com.decisionos.decision.DecisionService;
import com.decisionos.organization.Organization;
import com.decisionos.organization.OrganizationService;
import com.decisionos.simulation.SimulationRun;
import com.decisionos.simulation.SimulationService;
import com.decisionos.situation.SituationAnalysis;
import com.decisionos.situation.SituationAnalysisService;
import com.decisionos.weakness.Weakness;
import com.decisionos.weakness.WeaknessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final OrganizationService organizationService;
    private final SituationAnalysisService situationService;
    private final DecisionService decisionService;
    private final SimulationService simulationService;
    private final WeaknessRepository weaknessRepository;

    @GetMapping("/api/v1/organizations/{id}/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard(@PathVariable UUID id) {
        Organization org = organizationService.get(id);
        SituationAnalysis situation = situationService.latest(id);
        List<Weakness> weaknesses = weaknessRepository.findByOrganizationIdOrderBySeverityDesc(id);
        List<Decision> decisions = decisionService.listByOrg(id);
        List<SimulationRun> history = simulationService.historyByOrg(id);
        SimulationRun latestRun = history.isEmpty() ? null : history.get(0);
        Map<String, Object> out = new HashMap<>();
        out.put("organization", org);
        out.put("situation", situation);
        out.put("weaknesses", weaknesses);
        out.put("decisionCount", decisions.size());
        out.put("latestRun", latestRun);
        out.put("runCount", history.size());
        return ResponseEntity.ok(out);
    }
}
