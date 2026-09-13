package com.decisionos.businessmodel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BusinessModelEdgeRepository extends JpaRepository<BusinessModelEdge, UUID> {

    List<BusinessModelEdge> findByOrganizationId(UUID organizationId);
}
