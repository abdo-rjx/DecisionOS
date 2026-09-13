package com.decisionos.simulation;

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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "simulation_run")
@Getter
@Setter
public class SimulationRun extends BaseEntity {

    @Column(name = "decision_id", nullable = false)
    private UUID decisionId;

    @Column(name = "scenario_id")
    private UUID scenarioId;

    @Column(name = "time_horizon_months", nullable = false)
    private int timeHorizonMonths = 12;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "input_snapshot", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> inputSnapshot = new HashMap<>();

    @Column(name = "random_seed", nullable = false)
    private long randomSeed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private List<String> assumptions = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "time_series_result", columnDefinition = "jsonb", nullable = false)
    private List<Map<String, Object>> timeSeriesResult = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "aggregate_stats", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> aggregateStats = new HashMap<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_profile", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> riskProfile = new HashMap<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SimulationStatus status = SimulationStatus.COMPLETED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
