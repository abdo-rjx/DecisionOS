# DecisionOS — Project Blueprint

> **Purpose of this document**
> This file is written for an AI coding agent (e.g. Claude Code) to read once and use as the single source of truth to **build the DecisionOS platform from scratch**. It translates the *Cahier des Charges* (business/functional spec, in French/Darija) into a concrete, buildable technical plan: architecture, stack, folder structure, database schema, API contracts, and a strict build order.
>
> **Ground rule for the agent building this: do not invent features that are not in this document.** Every module below maps 1:1 to a module in the original Cahier des Charges. If something is ambiguous, prefer the simplest interpretation that satisfies the functional description, and leave a `// TODO(spec):` comment rather than guessing silently.

---

## 0. Core Principle (read this before writing any code)

DecisionOS is **not a prediction engine**. It is a **decision-support simulator under uncertainty**.

The system never says:
> "I know the future."

The system always says:
> "Based on the available information and assumptions, these are the most plausible outcomes and their associated risks."

Every simulation result, every recommendation, every risk score **must** be traceable back to:
1. The input data used
2. The assumptions made
3. The variables that had the largest influence

This principle (Explainability, Module 14) is not optional polish — it is a first-class requirement that shapes the data model (we must store *why*, not just *what*).

---

## 1. Tech Stack

| Layer | Technology | Notes |
|---|---|---|
| Frontend | **TypeScript + React (Next.js)** | Dashboard, forms, scenario comparison UI, charts |
| Backend | **Spring Boot (Java 21)** | REST API, business logic, orchestration, persistence |
| Database | **PostgreSQL** | Relational — the domain is highly relational (org → decisions → scenarios → simulations → results) |
| AI / Reasoning Layer | **Groq API** (LLM inference) | Used for: qualitative reasoning, weak-point narrative generation, scenario narrative generation, explainability text, recommendation justification. **Not** used for raw numeric simulation math — that stays deterministic in Spring Boot (see §7). |
| Auth | Spring Security + JWT | Simple email/password to start; org-scoped access |
| Build tools | Maven (backend), npm/pnpm (frontend) | |
| Containerization | Docker + docker-compose | Postgres + backend + frontend, one command to boot everything locally |

### Why this split (numeric engine vs. LLM)
The simulation engine (Module 7) must be **deterministic, reproducible, and auditable** — the same inputs must always produce the same numeric outputs. So:
- **Spring Boot owns**: the business model graph, the formulas, the Monte-Carlo-style scenario math, risk scoring, persistence of every simulation run.
- **Groq (LLM) owns**: turning structured numeric results into human-readable explanations, narratives, weak-point descriptions, and recommendation justifications — i.e., everything under Module 14 (Explainability) and the natural-language parts of Modules 3, 6, 10, 13, 18, 19.

This keeps the "why" (Module 22 principle) grounded in real numbers instead of the LLM hallucinating outcomes.

---

## 2. High-Level Architecture

```
┌─────────────────────────────┐
│   Frontend (Next.js/TS)     │
│  - Org profile forms        │
│  - Dashboard                │
│  - Decision builder         │
│  - Scenario comparison UI   │
│  - Simulation history       │
└──────────────┬───────────────┘
               │ REST (JSON)
┌──────────────▼───────────────────────────────────────────┐
│                Spring Boot Backend                        │
│                                                            │
│  ┌────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │ Org Module │  │ Situation      │  │ Weakness         │  │
│  │ (Module 1) │  │ Analysis       │  │ Detection        │  │
│  │            │  │ (Module 2)     │  │ (Module 3)       │  │
│  └────────────┘  └───────────────┘  └──────────────────┘  │
│                                                            │
│  ┌────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │ Business   │  │ Decision       │  │ Scenario         │  │
│  │ Model Graph│  │ Creation       │  │ Generation       │  │
│  │ (Module 4) │  │ (Module 5)     │  │ (Module 6)       │  │
│  └────────────┘  └───────────────┘  └──────────────────┘  │
│                                                            │
│  ┌────────────────────────────────────────────────────┐   │
│  │      Simulation Engine (Module 7) — CORE           │   │
│  │  deterministic math + Monte Carlo sampling          │   │
│  └────────────────────────────────────────────────────┘   │
│                                                            │
│  ┌────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │ What-if    │  │ Decision       │  │ Risk Analysis    │  │
│  │ (Module 8) │  │ Comparison (9) │  │ (Module 10)      │  │
│  └────────────┘  └───────────────┘  └──────────────────┘  │
│                                                            │
│  ┌────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │ External   │  │ Counterfactual │  │ Recommendation   │  │
│  │ Factors(11)│  │ (Module 12)    │  │ (Module 13)       │  │
│  └────────────┘  └───────────────┘  └──────────────────┘  │
│                                                            │
│  ┌────────────┐  ┌───────────────────────────────────┐    │
│  │ Simulation │  │  Explainability Service (14)       │    │
│  │ History(15)│  │  → calls Groq API                  │    │
│  └────────────┘  └───────────────────────────────────┘    │
└──────────────┬─────────────────────────────────┬──────────┘
               │                                 │
      ┌────────▼────────┐               ┌────────▼────────┐
      │   PostgreSQL     │               │   Groq API      │
      │  (all state)     │               │  (LLM reasoning)│
      └──────────────────┘               └─────────────────┘
```

---

## 3. Domain Model — mapping Cahier des Charges → Entities

Each entity below corresponds directly to a module in the spec.

### 3.1 `Organization` (Module 1 — Profil de l'organisation)
```
Organization
- id: UUID
- name: string
- size: enum (SOLO, SMALL, MEDIUM, LARGE)
- employeeCount: int
- products: List<Product>
- monthlyRevenue: decimal
- monthlyExpenses: decimal
- availableResources: JSON (cash, equipment, etc. — flexible, see §3.1.1)
- targetMarkets: List<string>
- customerCount: int
- growthRate: decimal (% per month or year — store the unit explicitly)
- investments: JSON
- humanResources: JSON (roles, headcount per role)
- operationalCapacity: decimal (0-100 or absolute unit — define per org)
- currentGoals: List<string>
- createdAt / updatedAt
```

3.1.1 Extensibility requirement (from spec §6):
> "المنصة خاصها تسمح بإضافة معلومات أخرى حسب طبيعة المؤسسة."

→ Add a single JSONB column to the Organization table:

  customData: JSONB   // key-value pairs, e.g.
                       // {"warehouse_count": 3, "has_delivery_fleet": true}

This keeps the MVP simple. If a future need arises for
querying/filtering on custom fields, migrate to a dedicated
table at that time (YAGNI principle).

Frontend: render customData as a dynamic key-value editor
in the org profile form.

### 3.2 `SituationAnalysis` (Module 2)
Generated snapshot, not user-entered. Recomputed whenever org data changes materially.

```
SituationAnalysis
- id: UUID
- organizationId: FK
- computedAt: timestamp
- financial: JSON { revenue, expenses, profitability, cashPosition, growth }
- operational: JSON { capacity, workforce, dependencies, bottlenecks }
- market: JSON { growth, competition, customerConcentration, marketDependency }
- risk: JSON { financialRisk, operationalRisk, marketRisk, dependencyRisk } // each 0-100 or LOW/MED/HIGH/CRITICAL
```

### 3.3 `Weakness` (Module 3 — Weak Point Detection)
```
Weakness
- id: UUID
- organizationId: FK
- situationAnalysisId: FK
- title: string               // e.g. "High dependency on one product"
- reason: string               // human explanation
- supportingData: JSON         // the actual numbers that triggered this
- severity: enum (LOW, MEDIUM, HIGH, CRITICAL)
- affectedElements: List<string>   // e.g. ["revenue", "cash_runway"]
- possibleConsequences: List<string>
- generatedByLLM: boolean       // true if Groq wrote the narrative
```

Detection logic (deterministic rules in Spring Boot, e.g.):
- `singleProductRevenueShare > 70%` → "High dependency on one product" (HIGH)
- `cashRunwayMonths < 3` under current burn → "Critical cash position" (CRITICAL)
- `headcountGrowthRate < revenueGrowthRate` sustained → "Workforce may become insufficient" (MEDIUM/HIGH)

Then Groq is called to turn the rule-trigger + data into natural language explanation (this is where the LLM adds value — phrasing, not detection).

### 3.4 `BusinessModelGraph` (Module 4)
This is the dependency graph between organizational variables — the backbone that the Simulation Engine walks.

```
BusinessModelNode
- id: UUID
- organizationId: FK
- key: string          // e.g. "employees", "operational_capacity", "production", "sales", "revenue", "cash_flow", "investment_capacity", "growth"
- currentValue: decimal
- unit: string

BusinessModelEdge
- id: UUID
- organizationId: FK
- fromNodeId: FK
- toNodeId: FK
- relationshipType: enum (LINEAR, PERCENTAGE, THRESHOLD, CUSTOM_FORMULA)
- formula: string        // e.g. "revenue = production * avgUnitPrice * marketDemandFactor"
- formulaSecurity:
  - MUST be validated against a strict whitelist of allowed
    variables (only BusinessModelNode keys in the same org)
  - MUST NOT allow function calls, method invocations, or
    class instantiation
  - MUST NOT allow string literals or concatenation
  - Allowed operators: +, -, *, /, ^, min(), max(), abs()
  - Validation MUST happen BEFORE saving to DB (in the service
    layer, not just frontend)
- weight: decimal        // sensitivity coefficient, used by simulation engine
```

Default graph (from spec example, §9):
```
Employees → Operational Capacity → Production → Sales → Revenue → Cash Flow → Ability to Invest → Growth
```
This default chain must be **seeded** for every new organization, then customizable.

### 3.5 `Decision` (Module 5 — Decision Creation)
```
Decision
- id: UUID
- organizationId: FK
- title: string                // e.g. "Hire 5 employees"
- description: string
- decisionType: enum (HIRING, MARKETING_SPEND, NEW_BRANCH, PRICE_CHANGE, NEW_MARKET, NEW_PRODUCT, SUPPLIER_CHANGE, TECH_INVESTMENT, INFRA_EXPANSION, COST_CUTTING, CUSTOM)
- parameters: JSON              // structured params derived from natural language, e.g. { "hires": 5, "avgSalary": 8000 }
- createdAt: timestamp
```

**Decision → Parameters conversion (spec §10):** each `decisionType` has a parameter schema. When a user enters a decision in natural language ("Hire 5 employees"), Groq is used to **parse** it into structured `parameters` JSON matching the schema for that `decisionType`. This is an LLM extraction task (well-suited to Groq's speed), not a simulation task.

### 3.6 `Scenario` (Module 6 — Scenario Generation)
```
Scenario
- id: UUID
- decisionId: FK
- type: enum (OPTIMISTIC, EXPECTED, PESSIMISTIC, EXTREME, PROBABILISTIC_SAMPLE)
- externalConditionModifiers: JSON   // e.g. { "marketGrowthDelta": +0.05, "competitionIntensity": "low" }
- probabilityWeight: decimal          // used when aggregating probabilistic samples
- narrative: string                   // Groq-generated description of "what this scenario assumes"
```

For `PROBABILISTIC_SAMPLE`, the system generates **N** scenarios (e.g. N=200–1000) via Monte Carlo sampling over uncertain variables (see §7), not one row per sample in the DB — only summary statistics are persisted (see `SimulationRun.aggregateStats`), individual samples can be transient/in-memory.

### 3.7 `SimulationRun` (Module 7 — Simulation Engine — CORE)
```
SimulationRun
- id: UUID
- decisionId: FK
- scenarioId: FK (nullable if this run aggregates multiple scenarios)
- timeHorizonMonths: int         // 1, 3, 6, 12, 24
- inputSnapshot: JSON             // full copy of org state + business graph + decision params AT TIME OF RUN (immutable, for auditability)
- randomSeed: long    // the Java Random seed used for
                      // Monte Carlo sampling, stored for
                      // exact reproducibility
- assumptions: JSON               // explicit list of assumptions used
- timeSeriesResult: JSON           // per-month projected values for each BusinessModelNode
- aggregateStats: JSON             // { mean, p10, p50, p90, min, max } per key output metric
- riskProfile: JSON                // see RiskProfile below
- status: enum (PENDING, RUNNING, COMPLETED, FAILED)
- createdAt: timestamp
```

**Why `inputSnapshot` is mandatory:** Module 22's core principle demands every result be explainable and reproducible. If org data changes later, old simulation runs must still show exactly what they were based on.

### 3.8 `RiskProfile` (Module 10 — Risk Analysis)
Embedded in `SimulationRun`, or its own table if you want to query/filter risk profiles independently:
```
RiskProfile
- id: UUID
- simulationRunId: FK
- probabilityOfFailure: decimal        // 0-1
- potentialLoss: decimal
- potentialGain: decimal
- criticalDependencies: List<string>
- worstCaseScenarioId: FK
- recoveryDifficulty: enum (LOW, MEDIUM, HIGH, CRITICAL)
- riskLevel: enum (GREEN, YELLOW, RED, BLACK)   // 🟢🟡🔴⚫ from spec §15
```

### 3.9 `ExternalFactor` (Module 11)
```
ExternalFactor
- id: UUID
- organizationId: FK (nullable = global/library factor)
- name: string                  // e.g. "Interest rate hike", "New competitor entry"
- category: enum (ECONOMIC, MARKET, COMPETITOR, REGULATION, INTEREST_RATE, CURRENCY, SUPPLY, ENERGY, MAJOR_EVENT)
- impactModifiers: JSON          // which BusinessModelNodes it affects and by how much
- isActive: boolean              // whether it's toggled on for current simulations
```
These are optional modifiers a user can attach to a `Scenario` to stress-test external shocks.

### 3.10 `CounterfactualAnalysis` (Module 12)
```
CounterfactualAnalysis
- id: UUID
- organizationId: FK
- actualDecisionId: FK             // what actually happened / was chosen
- alternativeDecisionId: FK        // the "what if we had chosen X instead"
- actualSimulationRunId: FK
- alternativeSimulationRunId: FK
- comparisonSummary: JSON          // deltas per key metric
- narrative: string                 // Groq-generated: gains missed, risks avoided, etc.
```

### 3.11 `Recommendation` (Module 13)
```
Recommendation
- id: UUID
- decisionId: FK
- recommendedScenarioId: FK
- justification: string          // Groq-generated, MUST reference actual data (see §4 prompt contract)
- rankedAlternatives: JSON        // ordered list of other scenarios with short reasons
- createdAt: timestamp
```

### 3.12 `ExplainabilityRecord` (Module 14)
Attachable to any result (SimulationRun, Weakness, Recommendation, CounterfactualAnalysis).
```
ExplainabilityRecord
- id: UUID
- targetType: enum (SIMULATION_RUN, WEAKNESS, RECOMMENDATION, COUNTERFACTUAL)
- targetId: UUID
- mainFactors: List<string>
- assumptionsUsed: List<string>
- highestImpactVariables: JSON     // variable → impact score
- uncertaintiesConsidered: List<string>
- narrative: string                 // Groq output, grounded in the fields above
```

### 3.13 `SimulationHistoryEntry` (Module 15)
This can simply be a **query view** over `SimulationRun` (join with `Decision`), not necessarily a new table:
```sql
SELECT sr.id, d.title AS decision_title, sr.createdAt, sr.status
FROM simulation_run sr
JOIN decision d ON sr.decision_id = d.id
WHERE d.organization_id = :orgId
ORDER BY sr.createdAt DESC;
```

---

## 4. Groq Integration Contract (LLM Layer)

**Rule: Groq is called only for narrative/explanation/extraction tasks — never to invent numbers.**

### 4.1 Where Groq is used
| Use case | Module | Quality Tier | Input given to Groq | Output expected |
|---|---|---|---|---|
| Parse natural-language decision into structured params | 5 | LOW (Groq OK) | decision text + `decisionType` param schema | JSON matching schema |
| Weakness narrative | 3 | LOW (Groq OK) | rule-trigger name + supporting data | 1-2 sentence human explanation |
| Scenario narrative | 6 | LOW (Groq OK) | scenario type + external condition modifiers | short description of assumed conditions |
| Recommendation justification | 13 | HIGH (prefer GPT-4o) | ranked scenarios + their aggregateStats + riskProfile | justification text referencing the actual numbers |
| Explainability narrative | 14 | HIGH (prefer GPT-4o) | mainFactors + assumptions + impact scores (all pre-computed) | plain-language explanation |
| Counterfactual narrative | 12 | MEDIUM (Groq OK for MVP) | delta between actual vs alternative simulation stats | "what could have been gained/avoided" text |

### 4.2 Groq call pattern (Spring Boot side)

```java
public interface LlmClient {
    String complete(String systemPrompt, String userPrompt,
                     QualityTier tier);
}

@Service
public class GroqClientImpl implements LlmClient {
    private final WebClient webClient; // base-url: https://api.groq.com/openai/v1
    // model: use a fast Groq-hosted model (e.g. llama-3.x served on Groq) — confirm exact model id at build time
    // POST /chat/completions with { model, messages: [system, user], temperature: 0.2 }
}
```

The implementation should route HIGH-tier requests to the
secondary provider and LOW/MEDIUM to the primary, unless
LLM_TIER_OVERRIDE is set.

**Critical constraint for every prompt template:** the system prompt must always include an instruction equivalent to:
> "You are explaining pre-computed results. Do NOT invent numbers. Only use the numbers given to you in the user message. If information is insufficient, say so explicitly."

This enforces Module 22's principle at the LLM layer, not just in the UI copy.

### 4.3 Config
```
# Primary LLM (fast, cheap — used for LOW/MEDIUM tier)
LLM_PRIMARY_PROVIDER=groq
LLM_PRIMARY_API_KEY=<env var>
LLM_PRIMARY_MODEL=llama-3.3-70b-versatile

# Secondary LLM (higher quality — used for HIGH tier)
LLM_SECONDARY_PROVIDER=openai
LLM_SECONDARY_API_KEY=<env var>
LLM_SECONDARY_MODEL=gpt-4o

# MVP override: set to "primary" to use Groq for everything
LLM_TIER_OVERRIDE=primary
```

---

## 5. Simulation Engine — detailed logic (Module 7, the core module)

This is the most important part of the backend. Build it as its own isolated package: `com.decisionos.simulation`.

### 5.1 Inputs to a simulation run
1. `Organization` current state (all `BusinessModelNode` current values)
2. `BusinessModelGraph` (edges + formulas)
3. `Decision.parameters` (the intervention being tested)
4. `Scenario` (external condition modifiers + type)
5. `timeHorizonMonths`
6. Active `ExternalFactor`s (optional)

### 5.2 Algorithm (deterministic core + Monte Carlo wrapper)

```
function runSimulation(org, graph, decision, scenario, horizonMonths):
    state = org.currentBusinessModelState()   // map: nodeKey -> value
    state = applyDecisionParameters(state, decision)   // e.g. employees += 5

    timeSeries = []
    for month in 1..horizonMonths:
        state = propagateGraph(state, graph, scenario.externalConditionModifiers)
        timeSeries.append(snapshot(state, month))

    return timeSeries
```

`propagateGraph`: walk `BusinessModelEdge`s in topological order (Employees → Capacity → Production → Sales → Revenue → Cash Flow → Investment Capacity → Growth), applying each edge's formula. Formulas are simple expressions evaluated with a lightweight expression evaluator (e.g. `exp4j` or similar Java library) — do NOT hardcode Java code per formula; read `formula` string from the DB so users/analysts can tune the model without redeploying.

SECURITY: The expression evaluator (e.g. exp4j) MUST be
configured with a custom FunctionRegistry that only exposes
the whitelisted operators above. Do NOT use the default
function set. Wrap evaluation in a try-catch and treat any
parse error as a validation failure, not a runtime error.

### 5.3 Handling uncertainty — Monte Carlo layer
For `PROBABILISTIC_SAMPLE` scenarios (and to produce `aggregateStats` p10/p50/p90 for any scenario):

```
function runProbabilisticSimulation(org, graph, decision, baseScenario, horizonMonths, N=1000):
    results = []
    for i in 1..N:
        sampledModifiers = sampleUncertainVariables(baseScenario.externalConditionModifiers)
        // e.g. draw marketGrowthDelta ~ Normal(mean, stddev) per variable's defined uncertainty range
        timeSeries = runSimulation(org, graph, decision, sampledModifiers, horizonMonths)
        results.append(timeSeries)

    return aggregate(results)   // per month, per metric: mean, p10, p50, p90, min, max
```

Note: N=1000 provides reliable p10/p90 estimates while
completing in <1 second per simulation on modern hardware.
For EXTREME scenarios where tail risk matters (p5/p95),
consider N=2000. The N value should be configurable via
application.yml (key: simulation.monte-carlo.default-samples)
so it can be tuned without code changes.

Each `ExternalFactor`/scenario modifier that carries uncertainty should define a distribution (`mean`, `stddev`, or `min`/`max` for uniform) in its `impactModifiers` JSON, not just a point value.

### 5.4 Output
Persist to `SimulationRun.timeSeriesResult` and `.aggregateStats`. Then:
- Feed `aggregateStats` + `riskProfile` into the **Risk Analysis** (Module 10) scorer (deterministic thresholds → GREEN/YELLOW/RED/BLACK).
- Feed the same into the **Explainability Service** → Groq call for narrative.

### 5.5 Determinism & reproducibility requirement

Every SimulationRun MUST store:
1. inputSnapshot: full org state + graph + decision params
   (already defined above)
2. randomSeed: the exact seed passed to java.util.Random
   before Monte Carlo sampling begins

To reproduce a run:
- Load inputSnapshot
- Create new Random(simulationRun.randomSeed)
- Re-run the Monte Carlo loop
- Output MUST be byte-identical to the original

The seed is generated ONCE at the start of
runProbabilisticSimulation() via System.nanoTime() and
persisted immediately, before any sampling occurs.

---

## 6. What-if, Comparison, Counterfactual (Modules 8, 9, 12)

These three modules share the same underlying mechanism: **run 2+ simulations and diff them.**

- **What-if (8):** user runs the same decision type with different parameters ("hire 5" vs "hire 10" vs "no hiring") → table comparing `aggregateStats` across runs (as in spec's example table: Growth / Cost / Risk / Cash).
- **Comparison (9):** user explicitly selects N `Decision`s (can be different types entirely) and the system builds a comparison table: expected outcome, best-case, worst-case, risk, cost, potential gain, resource requirements, long-term consequences, and a **trade-off narrative** (Groq call, grounded in the numbers).
- **Counterfactual (12):** same diff mechanism, but framed backward-looking — "decision A was actually taken, what if B had been chosen instead" — see `CounterfactualAnalysis` entity in §3.10.

Build a shared backend service: `ComparisonService.compare(List<SimulationRun>)` → returns a normalized comparison DTO used by all three modules' endpoints/UI.

---

## 7. Risk Scoring (Module 10) — deterministic rules

Define explicit thresholds (tune later, but must exist and be documented, not left to the LLM):

```
riskLevel = 
  BLACK  if probabilityOfFailure > 0.5 OR potentialLoss > cashReserves
  RED    if probabilityOfFailure > 0.3 OR potentialLoss > 0.5 * cashReserves
  YELLOW if probabilityOfFailure > 0.15
  GREEN  otherwise
```

`recoveryDifficulty` derived from: cash runway post-worst-case ÷ average monthly burn.

---

## 8. REST API Contract (Spring Boot)

Base path: `/api/v1`

```
# Organization (Module 1)
POST   /organizations
GET    /organizations/{id}
PUT    /organizations/{id}
POST   /organizations/{id}/custom-fields
GET    /organizations/{id}/custom-fields

# Situation Analysis (Module 2)
POST   /organizations/{id}/situation-analysis      # triggers recompute
GET    /organizations/{id}/situation-analysis/latest

# Weaknesses (Module 3)
GET    /organizations/{id}/weaknesses

# Business Model Graph (Module 4)
GET    /organizations/{id}/business-model
PUT    /organizations/{id}/business-model/nodes/{nodeId}
POST   /organizations/{id}/business-model/edges
PUT    /organizations/{id}/business-model/edges/{edgeId}

# Decisions (Module 5)
POST   /organizations/{id}/decisions          # body: { title, description } -> parsed via Groq into parameters
GET    /organizations/{id}/decisions
GET    /decisions/{id}

# Scenarios (Module 6)
POST   /decisions/{id}/scenarios               # generate OPTIMISTIC/EXPECTED/PESSIMISTIC/EXTREME
GET    /decisions/{id}/scenarios

# Simulation (Module 7)
POST   /scenarios/{id}/simulate                # body: { timeHorizonMonths }
GET    /simulation-runs/{id}

# What-if (Module 8)
POST   /decisions/{id}/what-if                 # body: list of parameter variants
GET    /what-if/{batchId}

# Comparison (Module 9)
POST   /comparisons                            # body: { decisionIds: [...] }
GET    /comparisons/{id}

# Risk (Module 10) — embedded in simulation-run response, no separate endpoint needed
# unless standalone risk queries are wanted:
GET    /simulation-runs/{id}/risk-profile

# External Factors (Module 11)
GET    /external-factors                        # library
POST   /organizations/{id}/external-factors      # attach custom one
POST   /scenarios/{id}/external-factors/{factorId}   # toggle on for a scenario

# Counterfactual (Module 12)
POST   /organizations/{id}/counterfactual        # body: { actualDecisionId, alternativeDecisionId }
GET    /counterfactual/{id}

# Recommendation (Module 13)
GET    /decisions/{id}/recommendation

# Explainability (Module 14)
GET    /explainability/{targetType}/{targetId}

# Simulation History (Module 15)
GET    /organizations/{id}/simulation-history

# Dashboard (Module 21)
GET    /organizations/{id}/dashboard             # aggregated view, see §9
```

---

## 9. Dashboard Payload (Module 21)

Single aggregated endpoint response shape (`GET /organizations/{id}/dashboard`):

```json
{
  "currentState": {
    "financialHealth": "...",
    "growth": "...",
    "operationalCapacity": "...",
    "mainRisks": ["..."],
    "mainWeaknesses": ["..."]
  },
  "activeSimulation": {
    "decisionTitle": "...",
    "numberOfScenarios": 4,
    "expectedOutcome": "...",
    "riskLevel": "YELLOW"
  },
  "recommendations": {
    "bestScenario": "...",
    "alternativeScenario": "...",
    "mainRisks": ["..."]
  }
}
```

This endpoint composes data already computed by other services — it should not run new simulations itself, only read the latest persisted state.

---

## 10. Folder Structure

### Backend (Spring Boot, Maven, package `com.decisionos`)
```
decisionos-backend/
├── pom.xml
├── src/main/java/com/decisionos/
│   ├── DecisionOsApplication.java
│   ├── organization/          # Module 1 — entities, repo, service, controller
│   ├── situation/             # Module 2
│   ├── weakness/              # Module 3
│   ├── businessmodel/         # Module 4 — graph entities + expression evaluator
│   ├── decision/              # Module 5
│   ├── scenario/              # Module 6
│   ├── simulation/            # Module 7 — CORE engine
│   │   ├── engine/            # propagation + Monte Carlo
│   │   ├── SimulationRun.java
│   │   └── SimulationService.java
│   ├── whatif/                # Module 8
│   ├── comparison/            # Module 9
│   ├── risk/                  # Module 10
│   ├── externalfactor/        # Module 11
│   ├── counterfactual/        # Module 12
│   ├── recommendation/        # Module 13
│   ├── explainability/        # Module 14 — Groq prompt templates live here
│   ├── history/               # Module 15
│   ├── dashboard/             # Module 21
│   ├── groq/                  # GroqClient + config
│   ├── common/                # shared DTOs, exceptions, expression evaluator wrapper
│   └── security/              # JWT auth
├── src/main/resources/
│   ├── application.yml
│   └── db/migration/          # Flyway SQL migrations, one per entity group
└── src/test/java/com/decisionos/...
```

### Frontend (Next.js + TypeScript)
```
decisionos-frontend/
├── package.json
├── tsconfig.json
├── src/
│   ├── app/
│   │   ├── organizations/[id]/
│   │   │   ├── page.tsx                 # Module 1 profile
│   │   │   ├── situation/page.tsx        # Module 2
│   │   │   ├── weaknesses/page.tsx       # Module 3
│   │   │   ├── business-model/page.tsx   # Module 4 (graph editor)
│   │   │   ├── decisions/
│   │   │   │   ├── page.tsx              # list + create (Module 5)
│   │   │   │   └── [decisionId]/
│   │   │   │       ├── scenarios/page.tsx     # Module 6
│   │   │   │       ├── simulate/page.tsx      # Module 7 results view
│   │   │   │       ├── what-if/page.tsx       # Module 8
│   │   │   │       └── recommendation/page.tsx # Module 13
│   │   │   ├── comparisons/page.tsx      # Module 9
│   │   │   ├── counterfactual/page.tsx   # Module 12
│   │   │   ├── history/page.tsx          # Module 15
│   │   │   └── dashboard/page.tsx        # Module 21
│   │   └── layout.tsx
│   ├── components/
│   │   ├── charts/            # time-series, comparison tables, risk badges (🟢🟡🔴⚫)
│   │   ├── forms/
│   │   └── ui/
│   ├── lib/
│   │   ├── api-client.ts      # typed fetch wrapper for the Spring Boot API
│   │   └── types.ts           # TS types mirroring backend DTOs
│   └── styles/
└── public/
```

---

## 11. Build Order (strict sequence for the agent)

Build in this order — each phase depends on the previous one being functional (even if minimal).

**Phase 0 — Scaffolding**
1. Init Spring Boot project (Web, JPA, PostgreSQL driver, Flyway, Validation, Security).
2. Init Next.js + TypeScript project.
3. `docker-compose.yml` with Postgres + backend + frontend.
4. Confirm Groq API connectivity with a trivial "hello world" completion call, wired through `GroqClient`.

**Phase 1 — Organization core (Module 1)**
5. `Organization` entity + migration + CRUD API + minimal frontend form.

**Phase 2 — Business Model Graph (Module 4) — build early, everything depends on it**
6. `BusinessModelNode` / `BusinessModelEdge` entities + seed default chain (Employees→...→Growth) on org creation.
7. Expression evaluator wrapper (pick a library, wire `propagateGraph`).

**Phase 3 — Situation Analysis & Weaknesses (Modules 2, 3)**
8. `SituationAnalysis` computation service (pure deterministic aggregation from org + graph state).
9. `Weakness` rule engine (deterministic triggers) + Groq narrative enrichment.

**Phase 4 — Decisions & Scenarios (Modules 5, 6)**
10. `Decision` entity + Groq-based NL→parameters parsing.
11. `Scenario` generation (4 fixed types + probabilistic).

**Phase 5 — Simulation Engine (Module 7) — THE CORE, build carefully**
12. Deterministic single-run propagation.
13. Monte Carlo wrapper + aggregateStats.
14. Persist `SimulationRun` with full `inputSnapshot` (reproducibility).

**Phase 6 — Risk Analysis (Module 10)**
15. Deterministic risk scorer consuming `SimulationRun` output.

**Phase 7 — What-if, Comparison, Counterfactual (Modules 8, 9, 12)**
16. Shared `ComparisonService`.
17. Three thin controllers/UIs on top of it.

**Phase 8 — Recommendation & Explainability (Modules 13, 14)**
18. Recommendation ranking logic (deterministic scoring across scenarios: e.g. weighted sum of normalized growth/risk/cost per user-selected priorities).
19. Groq prompt templates for justification + explainability narratives (§4).

**Phase 9 — External Factors (Module 11)**
20. Library of factors + attach/toggle mechanism feeding into scenario modifiers.

**Phase 10 — History & Dashboard (Modules 15, 21)**
21. History query view.
22. Dashboard aggregation endpoint + frontend.

**Phase 11 — Polish**
23. Auth (JWT), multi-org support per user, error handling, input validation, loading states in UI, empty states, risk color badges (🟢🟡🔴⚫) in UI.

---

## 12. Non-Goals (explicitly out of scope for v1 — do not build these unless asked)

- Multi-tenant billing/subscriptions
- Real external data feeds (interest rates, market data APIs) — `ExternalFactor` values are user-entered/manual in v1, not auto-fetched
- Mobile app
- Real-time collaboration (multiple users editing same org simultaneously)
- Fine-tuning or hosting a custom model — Groq's hosted API only

This section exists so the AI agent doesn't scope-creep. The long-term vision (Module 24 — general-purpose Decision Intelligence Platform beyond business) is **noted for context only** and is not part of this build.

---

## 13. Summary for the Agent

Build order = **Org → Business Model Graph → Situation/Weaknesses → Decisions/Scenarios → Simulation Engine → Risk → What-if/Comparison/Counterfactual → Recommendation/Explainability → External Factors → History/Dashboard → Polish.**

Stack = **TypeScript/Next.js frontend, Spring Boot backend, PostgreSQL, Groq API for all narrative/explanation/NL-parsing tasks only — never for core numeric simulation.**

Every module in this file maps to a numbered module in the original Cahier des Charges (referenced inline). Do not add modules that aren't there.
