package com.decisionos.organization;

import com.decisionos.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "organization")
@Getter
@Setter
public class Organization extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrgSize size = OrgSize.SMALL;

    @Column(name = "employee_count", nullable = false)
    private int employeeCount;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> products = new ArrayList<>();

    @Column(name = "monthly_revenue", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;

    @Column(name = "monthly_expenses", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyExpenses = BigDecimal.ZERO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "available_resources", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> availableResources = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_markets", columnDefinition = "jsonb", nullable = false)
    private List<String> targetMarkets = new ArrayList<>();

    @Column(name = "customer_count", nullable = false)
    private int customerCount;

    @Column(name = "growth_rate", nullable = false)
    private double growthRate;

    @Column(name = "growth_rate_unit", nullable = false)
    private String growthRateUnit = "monthly";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> investments = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "human_resources", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> humanResources = new HashMap<>();

    @Column(name = "operational_capacity", nullable = false)
    private double operationalCapacity;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "current_goals", columnDefinition = "jsonb", nullable = false)
    private List<String> currentGoals = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "custom_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> customData = new HashMap<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        super.prePersist();
        Instant t = Instant.now();
        createdAt = t;
        updatedAt = t;
    }

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }
}
