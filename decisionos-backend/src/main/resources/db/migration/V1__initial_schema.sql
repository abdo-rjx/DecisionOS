CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE app_user (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE organization (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    size VARCHAR(20) NOT NULL DEFAULT 'SMALL',
    employee_count INTEGER NOT NULL DEFAULT 0,
    products JSONB NOT NULL DEFAULT '[]',
    monthly_revenue NUMERIC(19, 2) NOT NULL DEFAULT 0,
    monthly_expenses NUMERIC(19, 2) NOT NULL DEFAULT 0,
    available_resources JSONB NOT NULL DEFAULT '{}',
    target_markets JSONB NOT NULL DEFAULT '[]',
    customer_count INTEGER NOT NULL DEFAULT 0,
    growth_rate DOUBLE PRECISION NOT NULL DEFAULT 0,
    growth_rate_unit VARCHAR(20) NOT NULL DEFAULT 'monthly',
    investments JSONB NOT NULL DEFAULT '{}',
    human_resources JSONB NOT NULL DEFAULT '{}',
    operational_capacity DOUBLE PRECISION NOT NULL DEFAULT 0,
    current_goals JSONB NOT NULL DEFAULT '[]',
    custom_data JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE business_model_node (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    node_key VARCHAR(100) NOT NULL,
    current_value DOUBLE PRECISION NOT NULL DEFAULT 0,
    unit VARCHAR(50) NOT NULL DEFAULT '',
    CONSTRAINT uq_node_org_key UNIQUE (organization_id, node_key)
);
CREATE INDEX idx_node_org ON business_model_node(organization_id);

CREATE TABLE business_model_edge (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    from_node_id UUID NOT NULL REFERENCES business_model_node(id) ON DELETE CASCADE,
    to_node_id UUID NOT NULL REFERENCES business_model_node(id) ON DELETE CASCADE,
    relationship_type VARCHAR(20) NOT NULL DEFAULT 'LINEAR',
    formula TEXT NOT NULL,
    weight DOUBLE PRECISION NOT NULL DEFAULT 1.0
);
CREATE INDEX idx_edge_org ON business_model_edge(organization_id);

CREATE TABLE situation_analysis (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    computed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    financial JSONB NOT NULL DEFAULT '{}',
    operational JSONB NOT NULL DEFAULT '{}',
    market JSONB NOT NULL DEFAULT '{}',
    risk JSONB NOT NULL DEFAULT '{}'
);
CREATE INDEX idx_situation_org ON situation_analysis(organization_id, computed_at DESC);

CREATE TABLE weakness (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    situation_analysis_id UUID REFERENCES situation_analysis(id) ON DELETE SET NULL,
    title VARCHAR(500) NOT NULL,
    reason TEXT NOT NULL DEFAULT '',
    supporting_data JSONB NOT NULL DEFAULT '{}',
    severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    affected_elements JSONB NOT NULL DEFAULT '[]',
    possible_consequences JSONB NOT NULL DEFAULT '[]',
    generated_by_llm BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_weakness_org ON weakness(organization_id);

CREATE TABLE decision (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    decision_type VARCHAR(30) NOT NULL DEFAULT 'CUSTOM',
    parameters JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_decision_org ON decision(organization_id);

CREATE TABLE scenario (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL REFERENCES decision(id) ON DELETE CASCADE,
    type VARCHAR(30) NOT NULL,
    external_condition_modifiers JSONB NOT NULL DEFAULT '{}',
    active_factors JSONB NOT NULL DEFAULT '[]',
    probability_weight DOUBLE PRECISION NOT NULL DEFAULT 1.0,
    narrative TEXT NOT NULL DEFAULT ''
);
CREATE INDEX idx_scenario_decision ON scenario(decision_id);

CREATE TABLE simulation_run (
    id UUID PRIMARY KEY,
    decision_id UUID NOT NULL REFERENCES decision(id) ON DELETE CASCADE,
    scenario_id UUID REFERENCES scenario(id) ON DELETE SET NULL,
    time_horizon_months INTEGER NOT NULL DEFAULT 12,
    input_snapshot JSONB NOT NULL DEFAULT '{}',
    random_seed BIGINT NOT NULL DEFAULT 0,
    assumptions JSONB NOT NULL DEFAULT '[]',
    time_series_result JSONB NOT NULL DEFAULT '[]',
    aggregate_stats JSONB NOT NULL DEFAULT '{}',
    risk_profile JSONB NOT NULL DEFAULT '{}',
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_run_decision ON simulation_run(decision_id);
CREATE INDEX idx_run_scenario ON simulation_run(scenario_id);

CREATE TABLE external_factor (
    id UUID PRIMARY KEY,
    organization_id UUID REFERENCES organization(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(30) NOT NULL DEFAULT 'ECONOMIC',
    impact_modifiers JSONB NOT NULL DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seed global library of 5 predefined external factors (Module 11, V1 hardcoded set)
INSERT INTO external_factor (id, organization_id, name, category, impact_modifiers, is_active) VALUES
    (gen_random_uuid(), NULL, 'Interest rate hike', 'INTEREST_RATE', '{"cash_flow": -0.05, "investment_capacity": -0.10}', TRUE),
    (gen_random_uuid(), NULL, 'New competitor entry', 'COMPETITOR', '{"sales": -0.08, "revenue": -0.08}', TRUE),
    (gen_random_uuid(), NULL, 'Energy price spike', 'ENERGY', '{"cash_flow": -0.06, "production": -0.03}', TRUE),
    (gen_random_uuid(), NULL, 'Favorable regulation change', 'REGULATION', '{"sales": 0.05, "revenue": 0.05}', TRUE),
    (gen_random_uuid(), NULL, 'Currency fluctuation', 'CURRENCY', '{"revenue": -0.04, "cash_flow": -0.04}', TRUE);
