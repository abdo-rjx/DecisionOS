package com.decisionos.decision;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {
    List<Decision> findByOrganizationIdOrderByCreatedAtDesc(UUID organizationId);
}
