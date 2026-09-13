package com.decisionos.businessmodel;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class GraphResponse {
    private List<NodeDto> nodes;
    private List<EdgeDto> edges;

    public static GraphResponse from(List<BusinessModelNode> nodes, List<BusinessModelEdge> edges) {
        GraphResponse g = new GraphResponse();
        g.setNodes(nodes.stream().map(NodeDto::from).toList());
        g.setEdges(edges.stream().map(EdgeDto::from).toList());
        return g;
    }

    @Data
    public static class NodeDto {
        private UUID id;
        private String key;
        private double currentValue;
        private String unit;

        public static NodeDto from(BusinessModelNode n) {
            NodeDto d = new NodeDto();
            d.setId(n.getId());
            d.setKey(n.getKey());
            d.setCurrentValue(n.getCurrentValue());
            d.setUnit(n.getUnit());
            return d;
        }
    }

    @Data
    public static class EdgeDto {
        private UUID id;
        private UUID fromNodeId;
        private UUID toNodeId;
        private RelationshipType relationshipType;
        private String formula;
        private double weight;

        public static EdgeDto from(BusinessModelEdge e) {
            EdgeDto d = new EdgeDto();
            d.setId(e.getId());
            d.setFromNodeId(e.getFromNodeId());
            d.setToNodeId(e.getToNodeId());
            d.setRelationshipType(e.getRelationshipType());
            d.setFormula(e.getFormula());
            d.setWeight(e.getWeight());
            return d;
        }
    }
}
