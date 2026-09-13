package com.decisionos.weakness;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WeaknessRepository extends JpaRepository<Weakness, UUID> {
    List<Weakness> findByOrganizationIdOrderBySeverityDesc(UUID organizationId);

    void deleteByOrganizationId(UUID organizationId);
}
