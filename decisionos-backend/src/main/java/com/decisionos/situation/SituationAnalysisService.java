package com.decisionos.situation;

import com.decisionos.businessmodel.BusinessModelService;
import com.decisionos.organization.Organization;
import com.decisionos.organization.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SituationAnalysisService {

    private final SituationAnalysisRepository repository;
    private final OrganizationService organizationService;
    private final BusinessModelService businessModelService;

    @Transactional
    public SituationAnalysis recompute(UUID organizationId) {
        Organization o = organizationService.get(organizationId);
        Map<String, Double> state = businessModelService.currentState(organizationId);
        double revenue = o.getMonthlyRevenue().doubleValue();
        double expenses = o.getMonthlyExpenses().doubleValue();
        double profit = revenue - expenses;
        double margin = revenue > 0 ? profit / revenue : 0;
        double cash = num(o.getAvailableResources().get("cash"), state.getOrDefault("cash_flow", 0.0));
        double burn = Math.max(0, expenses - revenue);
        double runway = burn > 0 ? cash / burn : 999;
        double capacity = o.getOperationalCapacity();
        double concentration = concentration(o.getProducts(), revenue);
        Map<String, Object> financial = new HashMap<>(Map.of(
                "revenue", revenue, "expenses", expenses, "profitability", profit,
                "margin", margin, "cashPosition", cash, "cashRunwayMonths", runway,
                "growth", o.getGrowthRate()));
        Map<String, Object> operational = new HashMap<>(Map.of(
                "capacity", capacity, "workforce", o.getEmployeeCount(),
                "customerCount", o.getCustomerCount(),
                "bottlenecks", capacity < 60 ? List.of("Low operational capacity") : List.of()));
        Map<String, Object> market = new HashMap<>(Map.of(
                "growth", o.getGrowthRate(), "customerCount", o.getCustomerCount(),
                "targetMarkets", o.getTargetMarkets(),
                "customerConcentration", concentration,
                "marketDependency", o.getTargetMarkets().size() <= 1 ? "HIGH" : "LOW"));
        Map<String, Object> risk = new HashMap<>(Map.of(
                "financialRisk", level(runway < 3 || margin < 0),
                "operationalRisk", level(capacity < 60),
                "marketRisk", level(concentration > 0.7),
                "dependencyRisk", level(o.getTargetMarkets().size() <= 1)));
        SituationAnalysis s = new SituationAnalysis();
        s.setOrganizationId(organizationId);
        s.setComputedAt(Instant.now());
        s.setFinancial(financial);
        s.setOperational(operational);
        s.setMarket(market);
        s.setRisk(risk);
        return repository.save(s);
    }

    @Transactional
    public SituationAnalysis latest(UUID organizationId) {
        List<SituationAnalysis> all = repository.findByOrganizationIdOrderByComputedAtDesc(organizationId);
        if (!all.isEmpty()) {
            return all.get(0);
        }
        return recompute(organizationId);
    }

    private String level(boolean bad) {
        return bad ? "HIGH" : "LOW";
    }

    private double concentration(List<?> products, double revenue) {
        if (products == null || products.isEmpty() || revenue <= 0) {
            return 0;
        }
        // TODO(spec): product revenue shares are not modeled in V1; assume even split.
        return 1.0 / products.size();
    }

    private double num(Object v, double fallback) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }
}
