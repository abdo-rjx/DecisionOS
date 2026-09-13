package com.decisionos.weakness;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations/{id}/weaknesses")
@RequiredArgsConstructor
public class WeaknessController {

    private final WeaknessService service;

    @GetMapping
    public ResponseEntity<List<Weakness>> list(@PathVariable UUID id) {
        return ResponseEntity.ok(service.detect(id));
    }
}
