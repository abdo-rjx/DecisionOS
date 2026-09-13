package com.decisionos.externalfactor;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ExternalFactorController {

    private final ExternalFactorService service;

    @GetMapping("/api/v1/external-factors")
    public ResponseEntity<List<ExternalFactor>> library() {
        return ResponseEntity.ok(service.library());
    }

    @PostMapping("/api/v1/organizations/{id}/external-factors")
    public ResponseEntity<ExternalFactor> attach(@PathVariable UUID id,
                                                 @RequestBody ExternalFactorRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.attach(id, req));
    }
}
