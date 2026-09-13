package com.decisionos.businessmodel;

import com.decisionos.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "business_model_node")
@Getter
@Setter
public class BusinessModelNode extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private UUID organizationId;

    @Column(name = "node_key", nullable = false)
    private String key;

    @Column(name = "current_value", nullable = false)
    private double currentValue;

    @Column(nullable = false)
    private String unit = "";
}
