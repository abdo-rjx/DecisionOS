package com.decisionos.organization;

import com.decisionos.businessmodel.BusinessModelService;
import com.decisionos.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository repository;
    private final BusinessModelService businessModelService;

    @Transactional
    public Organization create(OrganizationRequest req) {
        Organization o = new Organization();
        apply(o, req);
        Organization saved = repository.save(o);
        // Seed default business model graph on org creation (§3.4)
        businessModelService.seedDefaultGraph(saved);
        return saved;
    }

    @Transactional
    public Organization update(UUID id, OrganizationRequest req) {
        Organization o = get(id);
        apply(o, req);
        return repository.save(o);
    }

    @Transactional(readOnly = true)
    public Organization get(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Organization not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Organization> list() {
        return repository.findAll();
    }

    @Transactional
    public void delete(UUID id) {
        Organization o = get(id);
        repository.delete(o);
    }

    @Transactional
    public Organization addCustomField(UUID id, String key, Object value) {
        Organization o = get(id);
        o.getCustomData().put(key, value);
        return repository.save(o);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCustomFields(UUID id) {
        return get(id).getCustomData();
    }

    private void apply(Organization o, OrganizationRequest req) {
        if (req.getName() != null) {
            o.setName(req.getName());
        }
        if (req.getSize() != null) {
            o.setSize(req.getSize());
        }
        o.setEmployeeCount(Math.max(0, req.getEmployeeCount()));
        if (req.getProducts() != null) {
            o.setProducts(req.getProducts());
        }
        if (req.getMonthlyRevenue() != null) {
            o.setMonthlyRevenue(req.getMonthlyRevenue());
        }
        if (req.getMonthlyExpenses() != null) {
            o.setMonthlyExpenses(req.getMonthlyExpenses());
        }
        if (req.getAvailableResources() != null) {
            o.setAvailableResources(req.getAvailableResources());
        }
        if (req.getTargetMarkets() != null) {
            o.setTargetMarkets(req.getTargetMarkets());
        }
        o.setCustomerCount(Math.max(0, req.getCustomerCount()));
        o.setGrowthRate(req.getGrowthRate());
        if (req.getGrowthRateUnit() != null) {
            o.setGrowthRateUnit(req.getGrowthRateUnit());
        }
        if (req.getInvestments() != null) {
            o.setInvestments(req.getInvestments());
        }
        if (req.getHumanResources() != null) {
            o.setHumanResources(req.getHumanResources());
        }
        o.setOperationalCapacity(req.getOperationalCapacity());
        if (req.getCurrentGoals() != null) {
            o.setCurrentGoals(req.getCurrentGoals());
        }
        if (req.getCustomData() != null) {
            o.setCustomData(req.getCustomData());
        }
    }
}
