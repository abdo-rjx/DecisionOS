package com.decisionos.organization;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService service;

    @PostMapping
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(OrganizationResponse.from(service.create(req)));
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> list() {
        return ResponseEntity.ok(service.list().stream().map(OrganizationResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> get(@PathVariable UUID id) {
        return ResponseEntity.ok(OrganizationResponse.from(service.get(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody OrganizationRequest req) {
        return ResponseEntity.ok(OrganizationResponse.from(service.update(id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/custom-fields")
    public ResponseEntity<OrganizationResponse> addCustomField(@PathVariable UUID id,
                                                               @RequestBody Map<String, Object> body) {
        String key = String.valueOf(body.get("key"));
        Object value = body.get("value");
        return ResponseEntity.ok(OrganizationResponse.from(service.addCustomField(id, key, value)));
    }

    @GetMapping("/{id}/custom-fields")
    public ResponseEntity<Map<String, Object>> getCustomFields(@PathVariable UUID id) {
        return ResponseEntity.ok(service.getCustomFields(id));
    }
}
