package com.decisionos.businessmodel;

import com.decisionos.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "business_model_edge")
@Getter
@Setter
public class BusinessModelEdge extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "from_node_id", nullable = false)
    private UUID fromNodeId;

    @Column(name = "to_node_id", nullable = false)
    private UUID toNodeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship_type", nullable = false)
    private RelationshipType relationshipType = RelationshipType.LINEAR;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String formula;

    @Column(nullable = false)
    private double weight = 1.0;
}
