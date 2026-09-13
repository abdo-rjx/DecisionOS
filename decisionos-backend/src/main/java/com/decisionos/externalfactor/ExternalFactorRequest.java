package com.decisionos.externalfactor;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class ExternalFactorRequest {
    private String name;
    private String category = "ECONOMIC";
    private Map<String, Object> impactModifiers = new HashMap<>();
    private boolean active = true;
}
