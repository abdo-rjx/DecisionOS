package com.decisionos.simulation;

import com.decisionos.businessmodel.BusinessModelService;
import com.decisionos.common.exceptions.NotFoundException;
import com.decisionos.decision.Decision;
import com.decisionos.decision.DecisionService;
import com.decisionos.organization.Organization;
import com.decisionos.organization.OrganizationService;
import com.decisionos.risk.RiskScorer;
import com.decisionos.scenario.Scenario;
import com.decisionos.scenario.ScenarioRepository;
import com.decisionos.simulation.engine.GraphPropagator;
import com.decisionos.simulation.engine.MonteCarloRunner;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimulationService {

    private final SimulationRunRepository repository;
    private final ScenarioRepository scenarioRepository;
    private final DecisionService decisionService;
    private final OrganizationService organizationService;
    private final BusinessModelService businessModelService;
    private final GraphPropagator propagator;
    private final MonteCarloRunner monteCarlo;
    private final RiskScorer riskScorer;

    @Value("${simulation.monte-carlo.default-samples:1000}")
    private int defaultSamples;

    @Transactional
    public SimulationRun simulate(UUID scenarioId, int horizonMonths) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new NotFoundException("Scenario not found: " + scenarioId));
        Decision decision = decisionService.get(scenario.getDecisionId());
        Organization org = organizationService.get(decision.getOrganizationId());
        int horizon = horizonMonths >= 1 && horizonMonths <= 24 ? horizonMonths : 12;
        Map<String, Double> baseState = businessModelService.currentState(org.getId());
        Map<String, Double> withDecision = applyDecision(baseState, decision.getParameters());
        Map<String, Double> modifiers = toDoubleMap(scenario.getExternalConditionModifiers());
        long seed = System.nanoTime();
        Random random = new Random(seed);
        List<List<Map<String, Double>>> runs = new ArrayList<>();
        for (int i = 0; i < defaultSamples; i++) {
            runs.add(propagator.propagate(withDecision, org.getId(), sample(modifiers, random), horizon));
        }
        List<Map<String, Double>> baseline = propagator.propagate(withDecision, org.getId(), modifiers, horizon);
        Map<String, Object> stats = monteCarlo.aggregate(runs);
        double cash = num(org.getAvailableResources().get("cash"), withDecision.getOrDefault("cash_flow", 0.0));
        double burn = Math.max(0, org.getMonthlyExpenses().doubleValue() - org.getMonthlyRevenue().doubleValue());
        Map<String, Object> risk = riskScorer.score(stats, Math.max(0, cash), burn);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("orgState", baseState);
        snapshot.put("decisionParams", decision.getParameters());
        snapshot.put("modifiers", modifiers);
        snapshot.put("organizationId", org.getId().toString());
        SimulationRun run = new SimulationRun();
        run.setDecisionId(decision.getId());
        run.setScenarioId(scenario.getId());
        run.setTimeHorizonMonths(horizon);
        run.setInputSnapshot(snapshot);
        run.setRandomSeed(seed);
        run.setAssumptions(List.of("Deterministic graph propagation",
                "Monte Carlo N=" + defaultSamples, "Scenario " + scenario.getType()));
        List<Map<String, Object>> series = new ArrayList<>();
        int month = 1;
        for (Map<String, Double> point : baseline) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("month", month++);
            row.putAll(point);
            series.add(row);
        }
        run.setTimeSeriesResult(series);
        run.setAggregateStats(stats);
        run.setRiskProfile(risk);
        run.setStatus(SimulationStatus.COMPLETED);
        run.setCreatedAt(Instant.now());
        return repository.save(run);
    }

    @Transactional(readOnly = true)
    public SimulationRun get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Run not found: " + id));
    }

    @Transactional(readOnly = true)
    public Map<String, Object> riskProfile(UUID id) {
        return get(id).getRiskProfile();
    }

    @Transactional(readOnly = true)
    public List<SimulationRun> historyByOrg(UUID organizationId) {
        List<SimulationRun> out = new ArrayList<>();
        for (Decision d : decisionService.listByOrg(organizationId)) {
            out.addAll(repository.findByDecisionIdOrderByCreatedAtDesc(d.getId()));
        }
        out.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return out;
    }


    private Map<String, Double> applyDecision(Map<String, Double> state, Map<String, Object> params) {
        Map<String, Double> next = new LinkedHashMap<>(state);
        if (params == null) {
            return next;
        }
        double hires = num(params.get("hires"), 0);
        double fired = num(params.get("fired"), 0);
        double salary = num(params.get("avgSalary"), 8000);
        double spend = num(params.get("spend"), num(params.get("amount"), 0));
        double pricePct = num(params.get("priceDeltaPercent"), 0);
        double capacity = num(params.get("newCapacity"), 0);
        if (hires != 0 || fired != 0) {
            next.put("employees", next.getOrDefault("employees", 0.0) + hires - fired);
            next.put("cash_flow", next.getOrDefault("cash_flow", 0.0) - (hires * salary));
        }
        if (spend != 0) {
            next.put("cash_flow", next.getOrDefault("cash_flow", 0.0) - spend);
            next.put("sales", next.getOrDefault("sales", 0.0) * (1 + Math.min(0.5, spend / 200000.0)));
        }
        if (pricePct != 0) {
            next.put("revenue", next.getOrDefault("revenue", 0.0) * (1 + pricePct / 100.0 * 0.7));
            next.put("sales", next.getOrDefault("sales", 0.0) * (1 - pricePct / 100.0 * 0.3));
        }
        if (capacity != 0) {
            next.put("operational_capacity", Math.min(100, next.getOrDefault("operational_capacity", 0.0) + capacity));
        }
        return next;
    }

    private Map<String, Double> sample(Map<String, Double> mods, Random random) {
        Map<String, Double> out = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : mods.entrySet()) {
            double std = Math.abs(e.getValue()) * 0.25 + 0.01;
            out.put(e.getKey(), e.getValue() + random.nextGaussian() * std);
        }
        return out;
    }

    private Map<String, Double> toDoubleMap(Map<String, Object> in) {
        Map<String, Double> out = new LinkedHashMap<>();
        if (in == null) {
            return out;
        }
        for (Map.Entry<String, Object> e : in.entrySet()) {
            out.put(e.getKey(), num(e.getValue(), 0));
        }
        return out;
    }

    private double num(Object v, double fallback) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }
}