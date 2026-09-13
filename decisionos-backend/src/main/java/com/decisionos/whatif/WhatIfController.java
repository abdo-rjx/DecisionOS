package com.decisionos.whatif;

import com.decisionos.simulation.SimulationController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class WhatIfController {

    private final WhatIfService service;

    @PostMapping("/api/v1/scenarios/{id}/what-if")
    public ResponseEntity<Map<String, Object>> whatIf(@PathVariable UUID id,
                                                      @RequestBody SimulationController.WhatIfRequest req) {
        return ResponseEntity.ok(service.runVariants(id,
                req.getVariants() == null ? java.util.List.of() : req.getVariants(),
                req.getHorizonMonths()));
    }
}
