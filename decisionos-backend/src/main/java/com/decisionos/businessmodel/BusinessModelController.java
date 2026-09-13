package com.decisionos.businessmodel;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{id}/business-model")
@RequiredArgsConstructor
public class BusinessModelController {

    private final BusinessModelService service;

    @GetMapping
    public ResponseEntity<GraphResponse> getGraph(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getGraph(id));
    }

    @PutMapping("/nodes/{nodeId}")
    public ResponseEntity<GraphResponse.NodeDto> updateNode(@PathVariable UUID id,
                                                            @PathVariable UUID nodeId,
                                                            @RequestBody NodeUpdateRequest req) {
        return ResponseEntity.ok(service.updateNode(id, nodeId, req.getCurrentValue()));
    }

    @PostMapping("/edges")
    public ResponseEntity<GraphResponse.EdgeDto> createEdge(@PathVariable UUID id,
                                                            @RequestBody EdgeRequest req) {
        return ResponseEntity.ok(service.createEdge(id, req));
    }

    @PutMapping("/edges/{edgeId}")
    public ResponseEntity<GraphResponse.EdgeDto> updateEdge(@PathVariable UUID id,
                                                            @PathVariable UUID edgeId,
                                                            @RequestBody EdgeRequest req) {
        return ResponseEntity.ok(service.updateEdge(id, edgeId, req));
    }
}
