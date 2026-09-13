package com.decisionos.decision;

import lombok.Data;

@Data
public class DecisionRequest {
    private String title;
    private String description = "";
    private DecisionType decisionType = DecisionType.CUSTOM;
}
