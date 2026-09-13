package com.decisionos.organization;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class OrganizationResponse {

    private UUID id;
    private String name;
    private OrgSize size;
    private int employeeCount;
    private List<Map<String, Object>> products;
    private BigDecimal monthlyRevenue;
    private BigDecimal monthlyExpenses;
    private Map<String, Object> availableResources;
    private List<String> targetMarkets;
    private int customerCount;
    private double growthRate;
    private String growthRateUnit;
    private Map<String, Object> investments;
    private Map<String, Object> humanResources;
    private double operationalCapacity;
    private List<String> currentGoals;
    private Map<String, Object> customData;
    private Instant createdAt;
    private Instant updatedAt;

    public static OrganizationResponse from(Organization o) {
        OrganizationResponse r = new OrganizationResponse();
        r.setId(o.getId());
        r.setName(o.getName());
        r.setSize(o.getSize());
        r.setEmployeeCount(o.getEmployeeCount());
        r.setProducts(o.getProducts());
        r.setMonthlyRevenue(o.getMonthlyRevenue());
        r.setMonthlyExpenses(o.getMonthlyExpenses());
        r.setAvailableResources(o.getAvailableResources());
        r.setTargetMarkets(o.getTargetMarkets());
        r.setCustomerCount(o.getCustomerCount());
        r.setGrowthRate(o.getGrowthRate());
        r.setGrowthRateUnit(o.getGrowthRateUnit());
        r.setInvestments(o.getInvestments());
        r.setHumanResources(o.getHumanResources());
        r.setOperationalCapacity(o.getOperationalCapacity());
        r.setCurrentGoals(o.getCurrentGoals());
        r.setCustomData(o.getCustomData());
        r.setCreatedAt(o.getCreatedAt());
        r.setUpdatedAt(o.getUpdatedAt());
        return r;
    }
}
