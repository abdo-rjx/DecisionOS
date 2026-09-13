package com.decisionos.counterfactual;

import com.decisionos.comparison.ComparisonService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
class CounterfactualService {

    private final ComparisonService comparisonService;

    @Transactional(readOnly = true)
    public Map<String, Object> diff(UUID runA, UUID runB) {
        // V1 simplification: counterfactual = diff of 2 simulation runs.
        return comparisonService.compareRuns(List.of(runA, runB));
    }
}

@RestController
@RequiredArgsConstructor
class CounterfactualController {

    private final CounterfactualService service;

    @Data
    public static class Request {
        private UUID runA;
        private UUID runB;
    }

    @PostMapping("/api/v1/counterfactual")
    public ResponseEntity<Map<String, Object>> diff(@RequestBody Request req) {
        return ResponseEntity.ok(service.diff(req.getRunA(), req.getRunB()));
    }
}
