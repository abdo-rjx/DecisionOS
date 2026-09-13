package com.decisionos.externalfactor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import com.decisionos.common.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "external_factor")
@Getter
@Setter
public class ExternalFactor extends BaseEntity {

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category = "ECONOMIC";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "impact_modifiers", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> impactModifiers = new HashMap<>();

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
