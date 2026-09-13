package com.decisionos.risk;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RiskScorer {

    public Map<String, Object> score(Map<String, Object> aggregateStats, double cashReserves,
                                     double monthlyBurn) {
        double pFail = probabilityOfFailure(aggregateStats);
        double loss = potentialLoss(aggregateStats);
        double gain = potentialGain(aggregateStats);
        RiskLevel level;
        if (pFail > 0.5 || loss > cashReserves) {
            level = RiskLevel.BLACK;
        } else if (pFail > 0.3 || loss > 0.5 * cashReserves) {
            level = RiskLevel.RED;
        } else if (pFail > 0.15) {
            level = RiskLevel.YELLOW;
        } else {
            level = RiskLevel.GREEN;
        }
        double runway = monthlyBurn > 0 ? Math.max(0, (cashReserves - loss) / monthlyBurn) : 999;
        String recovery = runway > 12 ? "LOW" : runway > 6 ? "MEDIUM" : runway > 3 ? "HIGH" : "CRITICAL";
        List<String> deps = new ArrayList<>();
        deps.add("cash_flow");
        deps.add("revenue");
        Map<String, Object> out = new HashMap<>();
        out.put("probabilityOfFailure", pFail);
        out.put("potentialLoss", loss);
        out.put("potentialGain", gain);
        out.put("criticalDependencies", deps);
        out.put("recoveryDifficulty", recovery);
        out.put("riskLevel", level.name());
        out.put("cashRunwayPostWorstCase", runway);
        return out;
    }

    private double probabilityOfFailure(Map<String, Object> stats) {
        // V1 heuristic: share of downside between p10 and mean for revenue/cash_flow.
        double revDown = downside(stats, "revenue");
        double cashDown = downside(stats, "cash_flow");
        double worst = Math.max(revDown, cashDown);
        if (worst <= 0) {
            return 0.05;
        }
        if (worst < 0.1) {
            return 0.1;
        }
        if (worst < 0.25) {
            return 0.25;
        }
        if (worst < 0.5) {
            return 0.4;
        }
        return 0.6;
    }

    private double downside(Map<String, Object> stats, String key) {
        Map<String, Object> m = metric(stats, key);
        double mean = num(m.get("mean"), 0);
        double p10 = num(m.get("p10"), mean);
        if (mean <= 0) {
            return mean < 0 ? 0.6 : 0.05;
        }
        return Math.max(0, (mean - p10) / Math.abs(mean));
    }

    private double potentialLoss(Map<String, Object> stats) {
        Map<String, Object> m = metric(stats, "cash_flow");
        double mean = num(m.get("mean"), 0);
        double p10 = num(m.get("p10"), mean);
        return Math.max(0, mean - p10);
    }

    private double potentialGain(Map<String, Object> stats) {
        Map<String, Object> m = metric(stats, "revenue");
        double mean = num(m.get("mean"), 0);
        double p90 = num(m.get("p90"), mean);
        return Math.max(0, p90 - mean);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> metric(Map<String, Object> stats, String key) {
        Object o = stats == null ? null : stats.get(key);
        if (o instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private double num(Object v, double fallback) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }
}
