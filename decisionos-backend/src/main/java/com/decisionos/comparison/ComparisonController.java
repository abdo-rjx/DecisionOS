package com.decisionos.comparison;

import com.decisionos.simulation.SimulationController;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ComparisonController {

    private final ComparisonService service;

    @PostMapping("/api/v1/comparisons")
    public ResponseEntity<Map<String, Object>> compare(@RequestBody SimulationController.CompareRequest req) {
        return ResponseEntity.ok(service.compareRuns(
                req.getRunIds() == null ? java.util.List.of() : req.getRunIds()));
    }
}
