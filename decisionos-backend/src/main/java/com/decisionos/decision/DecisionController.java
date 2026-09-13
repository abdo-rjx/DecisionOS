package com.decisionos.decision;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService service;

    @PostMapping("/api/v1/organizations/{id}/decisions")
    public ResponseEntity<Decision> create(@PathVariable UUID id, @RequestBody DecisionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(id, req.getTitle(), req.getDescription(), req.getDecisionType()));
    }

    @GetMapping("/api/v1/organizations/{id}/decisions")
    public ResponseEntity<List<Decision>> list(@PathVariable UUID id) {
        return ResponseEntity.ok(service.listByOrg(id));
    }

    @GetMapping("/api/v1/decisions/{id}")
    public ResponseEntity<Decision> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }
}
