package com.decisionos.businessmodel;

import lombok.Data;

import java.util.UUID;

@Data
public class EdgeRequest {
    private UUID fromNodeId;
    private UUID toNodeId;
    private RelationshipType relationshipType = RelationshipType.LINEAR;
    private String formula;
    private double weight = 1.0;
}
