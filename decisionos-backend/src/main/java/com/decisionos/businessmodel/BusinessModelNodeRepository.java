package com.decisionos.businessmodel;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BusinessModelNodeRepository extends JpaRepository<BusinessModelNode, UUID> {

    List<BusinessModelNode> findByOrganizationId(UUID organizationId);

    Optional<BusinessModelNode> findByOrganizationIdAndKey(UUID organizationId, String key);
}
