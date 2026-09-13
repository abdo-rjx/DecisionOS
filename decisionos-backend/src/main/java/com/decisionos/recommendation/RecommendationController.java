package com.decisionos.recommendation;

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
public class RecommendationController {

    private final RecommendationService service;

    @PostMapping("/api/v1/decisions/{id}/recommendation")
    public ResponseEntity<Map<String, Object>> recommend(@PathVariable UUID id,
                                                         @RequestBody(required = false) SimulationController.RecommendRequest req) {
        Map<String, Double> priorities = req == null || req.getPriorities() == null
                ? Map.of("growth", 1.0, "risk", 1.0, "cost", 1.0)
                : req.getPriorities();
        return ResponseEntity.ok(service.recommend(id, priorities));
    }
}
