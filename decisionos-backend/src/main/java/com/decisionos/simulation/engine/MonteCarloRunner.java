package com.decisionos.simulation.engine;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class MonteCarloRunner {

    public Map<String, Object> aggregate(List<List<Map<String, Double>>> runs) {
        Map<String, Object> stats = new LinkedHashMap<>();
        if (runs == null || runs.isEmpty()) {
            return stats;
        }
        int months = runs.get(0).size();
        // Aggregate per metric across runs using the final-month value.
        Map<String, List<Double>> finals = new HashMap<>();
        for (List<Map<String, Double>> run : runs) {
            Map<String, Double> last = run.get(run.size() - 1);
            for (Map.Entry<String, Double> e : last.entrySet()) {
                finals.computeIfAbsent(e.getKey(), k -> new ArrayList<>()).add(e.getValue());
            }
        }
        for (Map.Entry<String, List<Double>> e : finals.entrySet()) {
            List<Double> vals = new ArrayList<>(e.getValue());
            Collections.sort(vals);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("mean", mean(vals));
            m.put("p10", percentile(vals, 10));
            m.put("p50", percentile(vals, 50));
            m.put("p90", percentile(vals, 90));
            m.put("min", vals.get(0));
            m.put("max", vals.get(vals.size() - 1));
            stats.put(e.getKey(), m);
        }
        stats.put("months", months);
        stats.put("samples", runs.size());
        return stats;
    }

    private double mean(List<Double> vals) {
        double s = 0;
        for (double v : vals) {
            s += v;
        }
        return s / vals.size();
    }

    private double percentile(List<Double> sorted, double p) {
        if (sorted.isEmpty()) {
            return 0;
        }
        if (sorted.size() == 1) {
            return sorted.get(0);
        }
        double rank = p / 100.0 * (sorted.size() - 1);
        int lo = (int) Math.floor(rank);
        int hi = (int) Math.ceil(rank);
        if (lo == hi) {
            return sorted.get(lo);
        }
        double d = rank - lo;
        return sorted.get(lo) * (1 - d) + sorted.get(hi) * d;
    }
}
