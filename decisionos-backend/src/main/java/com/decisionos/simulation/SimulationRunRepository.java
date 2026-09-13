package com.decisionos.simulation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SimulationRunRepository extends JpaRepository<SimulationRun, UUID> {
    List<SimulationRun> findByDecisionIdOrderByCreatedAtDesc(UUID decisionId);

    List<SimulationRun> findByScenarioIdOrderByCreatedAtDesc(UUID scenarioId);
}
