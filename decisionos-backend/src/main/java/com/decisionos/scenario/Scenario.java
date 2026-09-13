package com.decisionos.scenario;

import com.decisionos.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "scenario")
@Getter
@Setter
public class Scenario extends BaseEntity {

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScenarioType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "external_condition_modifiers", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> externalConditionModifiers = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "active_factors", columnDefinition = "jsonb", nullable = false)
    private List<String> activeFactors = new ArrayList<>();

    @Column(name = "probability_weight", nullable = false)
    private double probabilityWeight = 1.0;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narrative = "";
}
