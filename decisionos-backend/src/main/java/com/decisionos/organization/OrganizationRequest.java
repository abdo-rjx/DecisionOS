package com.decisionos.organization;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class OrganizationRequest {

    @NotBlank
    private String name;

    private OrgSize size = OrgSize.SMALL;

    @Min(0)
    private int employeeCount;

    private List<Map<String, Object>> products = new ArrayList<>();

    private BigDecimal monthlyRevenue = BigDecimal.ZERO;

    private BigDecimal monthlyExpenses = BigDecimal.ZERO;

    private Map<String, Object> availableResources = new HashMap<>();

    private List<String> targetMarkets = new ArrayList<>();

    @Min(0)
    private int customerCount;

    private double growthRate;

    private String growthRateUnit = "monthly";

    private Map<String, Object> investments = new HashMap<>();

    private Map<String, Object> humanResources = new HashMap<>();

    private double operationalCapacity;

    private List<String> currentGoals = new ArrayList<>();

    private Map<String, Object> customData = new HashMap<>();
}
