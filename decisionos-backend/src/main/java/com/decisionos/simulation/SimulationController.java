package com.decisionos.simulation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SimulationController {

    private final SimulationService service;

    @PostMapping("/api/v1/scenarios/{id}/simulate")
    public ResponseEntity<SimulationRun> simulate(@PathVariable UUID id,
                                                  @RequestParam(defaultValue = "12") int horizonMonths) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.simulate(id, horizonMonths));
    }

    @GetMapping("/api/v1/simulation-runs/{id}")
    public ResponseEntity<SimulationRun> get(@PathVariable UUID id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping("/api/v1/simulation-runs/{id}/risk")
    public ResponseEntity<Map<String, Object>> risk(@PathVariable UUID id) {
        return ResponseEntity.ok(service.riskProfile(id));
    }

    @GetMapping("/api/v1/organizations/{id}/history")
    public ResponseEntity<List<SimulationRun>> history(@PathVariable UUID id) {
        return ResponseEntity.ok(service.historyByOrg(id));
    }

    @Data
    public static class CompareRequest {
        private List<UUID> runIds;
    }

    @Data
    public static class WhatIfRequest {
        private List<Map<String, Object>> variants;
        private int horizonMonths = 12;
    }

    @Data
    public static class RecommendRequest {
        private Map<String, Double> priorities;
    }
}
