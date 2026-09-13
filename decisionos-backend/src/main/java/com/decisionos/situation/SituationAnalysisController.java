package com.decisionos.situation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{id}/situation-analysis")
@RequiredArgsConstructor
public class SituationAnalysisController {

    private final SituationAnalysisService service;

    @PostMapping
    public ResponseEntity<SituationAnalysis> recompute(@PathVariable UUID id) {
        return ResponseEntity.ok(service.recompute(id));
    }

    @GetMapping("/latest")
    public ResponseEntity<SituationAnalysis> latest(@PathVariable UUID id) {
        return ResponseEntity.ok(service.latest(id));
    }
}
