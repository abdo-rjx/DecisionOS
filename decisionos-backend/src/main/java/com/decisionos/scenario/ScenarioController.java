package com.decisionos.scenario;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ScenarioController {

    private final ScenarioService service;

    @PostMapping("/api/v1/decisions/{id}/scenarios")
    public ResponseEntity<List<Scenario>> generate(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.generate(id));
    }

    @GetMapping("/api/v1/decisions/{id}/scenarios")
    public ResponseEntity<List<Scenario>> list(@PathVariable UUID id) {
        return ResponseEntity.ok(service.list(id));
    }

    @PostMapping("/api/v1/scenarios/{id}/external-factors/{factorId}")
    public ResponseEntity<Scenario> toggle(@PathVariable UUID id, @PathVariable UUID factorId) {
        return ResponseEntity.ok(service.toggleFactor(id, factorId));
    }
}
