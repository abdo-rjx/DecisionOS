package com.decisionos.decision;

import com.decisionos.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DecisionService {

    private final DecisionRepository repository;
    private final DecisionParsingService parsingService;

    @Transactional
    public Decision create(UUID organizationId, String title, String description, DecisionType type) {
        DecisionType t = type == null ? DecisionType.CUSTOM : type;
        Decision d = new Decision();
        d.setOrganizationId(organizationId);
        d.setTitle(title);
        d.setDescription(description == null ? "" : description);
        d.setDecisionType(t);
        Map<String, Object> params = parsingService.parse(title, description, t);
        d.setParameters(params);
        d.setCreatedAt(Instant.now());
        return repository.save(d);
    }

    @Transactional(readOnly = true)
    public Decision get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Decision not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Decision> listByOrg(UUID organizationId) {
        return repository.findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }
}
