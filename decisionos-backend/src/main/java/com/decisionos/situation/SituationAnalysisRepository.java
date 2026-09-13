package com.decisionos.situation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SituationAnalysisRepository extends JpaRepository<SituationAnalysis, UUID> {
    List<SituationAnalysis> findByOrganizationIdOrderByComputedAtDesc(UUID organizationId);
}
