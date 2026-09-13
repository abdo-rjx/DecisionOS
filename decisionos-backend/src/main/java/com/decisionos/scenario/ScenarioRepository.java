package com.decisionos.scenario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ScenarioRepository extends JpaRepository<Scenario, UUID> {
    List<Scenario> findByDecisionId(UUID decisionId);
}
