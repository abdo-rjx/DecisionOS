package com.decisionos.weakness;

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
@Table(name = "weakness")
@Getter
@Setter
public class Weakness extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "situation_analysis_id")
    private UUID situationAnalysisId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason = "";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "supporting_data", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> supportingData = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeaknessSeverity severity = WeaknessSeverity.MEDIUM;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "affected_elements", columnDefinition = "jsonb", nullable = false)
    private List<String> affectedElements = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "possible_consequences", columnDefinition = "jsonb", nullable = false)
    private List<String> possibleConsequences = new ArrayList<>();

    @Column(name = "generated_by_llm", nullable = false)
    private boolean generatedByLlm;
}
