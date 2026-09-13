package com.decisionos.simulation.engine;

import com.decisionos.businessmodel.BusinessModelEdge;
import com.decisionos.businessmodel.BusinessModelEdgeRepository;
import com.decisionos.businessmodel.BusinessModelNode;
import com.decisionos.businessmodel.BusinessModelNodeRepository;
import com.decisionos.common.ExpressionEvaluator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GraphPropagator {

    private final BusinessModelNodeRepository nodeRepository;
    private final BusinessModelEdgeRepository edgeRepository;
    private final ExpressionEvaluator evaluator;

    public List<Map<String, Double>> propagate(Map<String, Double> start,
                                               UUID organizationId,
                                               Map<String, Double> modifiers,
                                               int months) {
        List<BusinessModelEdge> edges = edgeRepository.findByOrganizationId(organizationId);
        Map<UUID, BusinessModelNode> byId = new HashMap<>();
        for (BusinessModelNode n : nodeRepository.findByOrganizationId(organizationId)) {
            byId.put(n.getId(), n);
        }
        // Topological-ish order: follow the default chain order when possible.
        List<String> chain = List.of("employees", "operational_capacity", "production",
                "sales", "revenue", "cash_flow", "investment_capacity", "growth");
        edges = new ArrayList<>(edges);
        edges.sort(Comparator.comparingInt(e -> {
            BusinessModelNode to = byId.get(e.getToNodeId());
            if (to == null) {
                return 99;
            }
            int i = chain.indexOf(to.getKey());
            return i < 0 ? 50 : i;
        }));
        Map<String, UUID> keyToId = new HashMap<>();
        for (BusinessModelNode n : byId.values()) {
            keyToId.put(n.getKey(), n.getId());
        }
        List<Map<String, Double>> series = new ArrayList<>();
        Map<String, Double> state = new LinkedHashMap<>(start);
        for (int m = 1; m <= months; m++) {
            Map<String, Double> next = new LinkedHashMap<>(state);
            for (BusinessModelEdge e : edges) {
                BusinessModelNode to = byId.get(e.getToNodeId());
                if (to == null) {
                    continue;
                }
                try {
                    Map<String, Double> vars = new HashMap<>(next);
                    double v = evaluator.evaluate(e.getFormula(), vars);
                    // Blend formula output with previous value using weight as sensitivity.
                    double prev = next.getOrDefault(to.getKey(), 0.0);
                    double blended = prev + (v - prev) * clamp(e.getWeight());
                    next.put(to.getKey(), blended);
                } catch (Exception ex) {
                    // Keep previous value on evaluation failure (deterministic fallback).
                }
            }
            // Apply scenario modifiers as relative deltas to sales/revenue/cash_flow.
            for (Map.Entry<String, Double> mod : modifiers.entrySet()) {
                String k = mod.getKey();
                if (next.containsKey(k)) {
                    next.put(k, next.get(k) * (1.0 + mod.getValue()));
                }
            }
            // Natural monthly growth drift from growth node.
            double g = next.getOrDefault("growth", 0.0) / 100.0;
            if (g != 0) {
                next.put("revenue", next.getOrDefault("revenue", 0.0) * (1.0 + g / 12.0));
                next.put("sales", next.getOrDefault("sales", 0.0) * (1.0 + g / 12.0));
            }
            state = next;
            Map<String, Double> point = new LinkedHashMap<>(state);
            series.add(point);
        }
        return series;
    }

    private double clamp(double w) {
        if (Double.isNaN(w) || Double.isInfinite(w)) {
            return 1.0;
        }
        return Math.min(1.0, Math.max(0.0, w));
    }
}
