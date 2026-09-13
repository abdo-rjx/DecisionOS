package com.decisionos.externalfactor;

import com.decisionos.common.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExternalFactorService {

    private final ExternalFactorRepository repository;

    @Transactional(readOnly = true)
    public List<ExternalFactor> library() {
        return repository.findByOrganizationIdIsNull();
    }

    @Transactional(readOnly = true)
    public List<ExternalFactor> listForOrg(UUID organizationId) {
        List<ExternalFactor> all = new ArrayList<>(library());
        all.addAll(repository.findByOrganizationId(organizationId));
        return all;
    }

    @Transactional
    public ExternalFactor attach(UUID organizationId, ExternalFactorRequest req) {
        ExternalFactor f = new ExternalFactor();
        f.setOrganizationId(organizationId);
        f.setName(req.getName());
        f.setCategory(req.getCategory() == null ? "ECONOMIC" : req.getCategory());
        if (req.getImpactModifiers() != null) {
            f.setImpactModifiers(req.getImpactModifiers());
        }
        f.setActive(req.isActive());
        return repository.save(f);
    }

    @Transactional(readOnly = true)
    public ExternalFactor get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Factor not found: " + id));
    }
}
