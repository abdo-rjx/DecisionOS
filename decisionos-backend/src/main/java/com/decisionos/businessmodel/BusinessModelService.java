package com.decisionos.businessmodel;

import com.decisionos.common.ExpressionEvaluator;
import com.decisionos.common.exceptions.BadRequestException;
import com.decisionos.common.exceptions.NotFoundException;
import com.decisionos.organization.Organization;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusinessModelService {

    private final BusinessModelNodeRepository nodeRepository;
    private final BusinessModelEdgeRepository edgeRepository;
    private final ExpressionEvaluator evaluator;

    @Transactional
    public void seedDefaultGraph(Organization org) {
        if (!nodeRepository.findByOrganizationId(org.getId()).isEmpty()) {
            return;
        }
        List<BusinessModelNode> seeds = new ArrayList<>();
        seeds.add(node(org.getId(), "employees", org.getEmployeeCount(), "people"));
        seeds.add(node(org.getId(), "operational_capacity", org.getOperationalCapacity(), "percent"));
        double production = org.getEmployeeCount() * Math.max(0.5, org.getOperationalCapacity() / 100.0);
        seeds.add(node(org.getId(), "production", production, "units"));
        seeds.add(node(org.getId(), "sales", production * 0.9, "units"));
        seeds.add(node(org.getId(), "revenue", org.getMonthlyRevenue().doubleValue(), "MAD"));
        double cash = org.getMonthlyRevenue().subtract(org.getMonthlyExpenses()).doubleValue();
        seeds.add(node(org.getId(), "cash_flow", cash, "MAD"));
        seeds.add(node(org.getId(), "investment_capacity", Math.max(0, cash * 0.2), "MAD"));
        seeds.add(node(org.getId(), "growth", org.getGrowthRate(), "percent"));
        List<BusinessModelNode> saved = nodeRepository.saveAll(seeds);
        Map<String, BusinessModelNode> byKey = saved.stream()
                .collect(Collectors.toMap(BusinessModelNode::getKey, n -> n));
        seedEdge(byKey, "employees", "operational_capacity", RelationshipType.LINEAR, "min(employees * 2, 100)");
        seedEdge(byKey, "operational_capacity", "production", RelationshipType.LINEAR, "employees * operational_capacity / 100");
        seedEdge(byKey, "production", "sales", RelationshipType.PERCENTAGE, "production * 0.9");
        seedEdge(byKey, "sales", "revenue", RelationshipType.LINEAR, "revenue");
        seedEdge(byKey, "revenue", "cash_flow", RelationshipType.LINEAR, "cash_flow");
        seedEdge(byKey, "cash_flow", "investment_capacity", RelationshipType.PERCENTAGE, "max(cash_flow, 0) * 0.2");
        seedEdge(byKey, "investment_capacity", "growth", RelationshipType.LINEAR, "growth");
    }
    @Transactional(readOnly = true)
    public GraphResponse getGraph(UUID organizationId) {
        List<BusinessModelNode> nodes = nodeRepository.findByOrganizationId(organizationId);
        List<BusinessModelEdge> edges = edgeRepository.findByOrganizationId(organizationId);
        return GraphResponse.from(nodes, edges);
    }

    @Transactional
    public GraphResponse.NodeDto updateNode(UUID organizationId, UUID nodeId, double currentValue) {
        BusinessModelNode node = nodeRepository.findById(nodeId)
                .filter(n -> n.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new NotFoundException("Node not found: " + nodeId));
        node.setCurrentValue(currentValue);
        return GraphResponse.NodeDto.from(nodeRepository.save(node));
    }

    @Transactional
    public GraphResponse.EdgeDto createEdge(UUID organizationId, EdgeRequest req) {
        BusinessModelNode from = nodeRepository.findById(req.getFromNodeId())
                .filter(n -> n.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new NotFoundException("From-node not found"));
        BusinessModelNode to = nodeRepository.findById(req.getToNodeId())
                .filter(n -> n.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new NotFoundException("To-node not found"));
        evaluator.validate(req.getFormula(), allowedKeys(organizationId));
        BusinessModelEdge edge = new BusinessModelEdge();
        edge.setOrganizationId(organizationId);
        edge.setFromNodeId(from.getId());
        edge.setToNodeId(to.getId());
        edge.setRelationshipType(req.getRelationshipType() == null ? RelationshipType.LINEAR : req.getRelationshipType());
        edge.setFormula(req.getFormula());
        edge.setWeight(req.getWeight() <= 0 ? 1.0 : req.getWeight());
        return GraphResponse.EdgeDto.from(edgeRepository.save(edge));
    }

    @Transactional
    public GraphResponse.EdgeDto updateEdge(UUID organizationId, UUID edgeId, EdgeRequest req) {
        BusinessModelEdge edge = edgeRepository.findById(edgeId)
                .filter(e -> e.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new NotFoundException("Edge not found: " + edgeId));
        if (req.getFormula() != null) {
            evaluator.validate(req.getFormula(), allowedKeys(organizationId));
            edge.setFormula(req.getFormula());
        }
        if (req.getRelationshipType() != null) {
            edge.setRelationshipType(req.getRelationshipType());
        }
        if (req.getWeight() > 0) {
            edge.setWeight(req.getWeight());
        }
        return GraphResponse.EdgeDto.from(edgeRepository.save(edge));
    }

    @Transactional(readOnly = true)
    public Map<String, Double> currentState(UUID organizationId) {
        Map<String, Double> state = new LinkedHashMap<>();
        for (BusinessModelNode n : nodeRepository.findByOrganizationId(organizationId)) {
            state.put(n.getKey(), n.getCurrentValue());
        }
        return state;
    }

    private Set<String> allowedKeys(UUID organizationId) {
        return nodeRepository.findByOrganizationId(organizationId).stream()
                .map(BusinessModelNode::getKey).collect(Collectors.toSet());
    }

    private BusinessModelNode node(UUID orgId, String key, double value, String unit) {
        BusinessModelNode n = new BusinessModelNode();
        n.setOrganizationId(orgId);
        n.setKey(key);
        n.setCurrentValue(Double.isFinite(value) ? value : 0.0);
        n.setUnit(unit);
        return n;
    }

    private void seedEdge(Map<String, BusinessModelNode> byKey, String from, String to,
                          RelationshipType type, String formula) {
        BusinessModelEdge e = new BusinessModelEdge();
        e.setOrganizationId(byKey.get(from).getOrganizationId());
        e.setFromNodeId(byKey.get(from).getId());
        e.setToNodeId(byKey.get(to).getId());
        e.setRelationshipType(type);
        e.setWeight(1.0);
        try {
            evaluator.validate(formula, byKey.keySet());
            e.setFormula(formula);
        } catch (BadRequestException ex) {
            e.setFormula(to);
        }
        edgeRepository.save(e);
    }
}
