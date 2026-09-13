package com.decisionos.weakness;

import com.decisionos.common.exceptions.LlmUnavailableException;
import com.decisionos.groq.LlmClient;
import com.decisionos.organization.Organization;
import com.decisionos.organization.OrganizationService;
import com.decisionos.situation.SituationAnalysis;
import com.decisionos.situation.SituationAnalysisService;
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
public class WeaknessService {

    private static final Logger log = LoggerFactory.getLogger(WeaknessService.class);

    private final WeaknessRepository repository;
    private final OrganizationService organizationService;
    private final SituationAnalysisService situationService;
    private final LlmClient llmClient;

    @Transactional
    public List<Weakness> detect(UUID organizationId) {
        Organization o = organizationService.get(organizationId);
        SituationAnalysis s = situationService.latest(organizationId);
        repository.deleteByOrganizationId(organizationId);
        List<Weakness> out = new ArrayList<>();
        double runway = num(s.getFinancial().get("cashRunwayMonths"), 999);
        double margin = num(s.getFinancial().get("margin"), 0);
        double capacity = num(s.getOperational().get("capacity"), 100);
        double concentration = num(s.getMarket().get("customerConcentration"), 0);
        if (runway < 3) {
            out.add(build(organizationId, s.getId(), "Critical cash position",
                    "Cash runway is under 3 months at current burn.",
                    Map.of("cashRunwayMonths", runway),
                    WeaknessSeverity.CRITICAL, List.of("cash_runway", "cash_flow"),
                    List.of("Unable to cover upcoming expenses", "Forced cost cuts")));
        }
        if (margin < 0) {
            out.add(build(organizationId, s.getId(), "Negative profitability",
                    "Monthly expenses exceed monthly revenue.",
                    Map.of("margin", margin),
                    WeaknessSeverity.HIGH, List.of("revenue", "expenses"),
                    List.of("Cash reserves erosion", "Reduced investment capacity")));
        }
        if (concentration > 0.7 || o.getProducts().size() == 1) {
            out.add(build(organizationId, s.getId(), "High dependency on one product",
                    "Revenue is concentrated on a single product.",
                    Map.of("concentration", concentration),
                    WeaknessSeverity.HIGH, List.of("revenue"),
                    List.of("Revenue shock if the product declines")));
        }
        if (capacity < 60) {
            out.add(build(organizationId, s.getId(), "Constrained operational capacity",
                    "Operational capacity is below 60.",
                    Map.of("capacity", capacity),
                    WeaknessSeverity.MEDIUM, List.of("operational_capacity", "production"),
                    List.of("Bottlenecks when demand grows")));
        }
        if (o.getTargetMarkets().size() <= 1) {
            out.add(build(organizationId, s.getId(), "Single-market dependency",
                    "The organization depends on a single target market.",
                    Map.of("markets", o.getTargetMarkets().size()),
                    WeaknessSeverity.MEDIUM, List.of("sales"),
                    List.of("Vulnerability to local demand shocks")));
        }
        List<Weakness> saved = repository.saveAll(out);
        enrichWithLlm(saved);
        return repository.findByOrganizationIdOrderBySeverityDesc(organizationId);
    }

    private Weakness build(UUID orgId, UUID situationId, String title, String fallback,
                           Map<String, Object> data, WeaknessSeverity severity,
                           List<String> affected, List<String> consequences) {
        Weakness w = new Weakness();
        w.setOrganizationId(orgId);
        w.setSituationAnalysisId(situationId);
        w.setTitle(title);
        w.setReason(fallback);
        w.setSupportingData(new HashMap<>(data));
        w.setSeverity(severity);
        w.setAffectedElements(new ArrayList<>(affected));
        w.setPossibleConsequences(new ArrayList<>(consequences));
        w.setGeneratedByLlm(false);
        return w;
    }

    private void enrichWithLlm(List<Weakness> weaknesses) {
        for (Weakness w : weaknesses) {
            try {
                String narrative = llmClient.complete(
                        "You explain pre-computed business weaknesses. Do NOT invent numbers. Only use the numbers given to you in the user message. If information is insufficient, say so explicitly.",
                        "Weakness: " + w.getTitle() + ". Data: " + w.getSupportingData()
                                + ". Write a 1-2 sentence human explanation.");
                w.setReason(narrative);
                w.setGeneratedByLlm(true);
                repository.save(w);
            } catch (LlmUnavailableException e) {
                log.debug("LLM unavailable for weakness narrative, keeping rule text: {}", e.getMessage());
            }
        }
    }

    private double num(Object v, double fallback) {
        if (v instanceof Number n) {
            return n.doubleValue();
        }
        return fallback;
    }
}
