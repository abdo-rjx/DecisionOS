package com.decisionos.externalfactor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExternalFactorRepository extends JpaRepository<ExternalFactor, UUID> {
    List<ExternalFactor> findByOrganizationIdIsNull();

    List<ExternalFactor> findByOrganizationId(UUID organizationId);
}
