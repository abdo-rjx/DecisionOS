// Shared backend DTO types (mirrors backend JSON shapes, V1).
export interface Organization {
  id: string;
  name: string;
  size: string;
  employeeCount: number;
  products: Record<string, unknown>[];
  monthlyRevenue: number;
  monthlyExpenses: number;
  availableResources: Record<string, unknown>;
  targetMarkets: string[];
  customerCount: number;
  growthRate: number;
  growthRateUnit: string;
  investments: Record<string, unknown>;
  humanResources: Record<string, unknown>;
  operationalCapacity: number;
  currentGoals: string[];
  customData: Record<string, unknown>;
}

export interface GraphNode {
  id: string;
  key: string;
  currentValue: number;
  unit: string;
}

export interface GraphEdge {
  id: string;
  fromNodeId: string;
  toNodeId: string;
  relationshipType: string;
  formula: string;
  weight: number;
}

export interface Scenario {
  id: string;
  decisionId: string;
  type: string;
  externalConditionModifiers: Record<string, number>;
  activeFactors: string[];
  probabilityWeight: number;
  narrative: string;
}

export interface SimulationRun {
  id: string;
  decisionId: string;
  scenarioId: string | null;
  timeHorizonMonths: number;
  timeSeriesResult: Record<string, number | string>[];
  aggregateStats: Record<string, { mean: number; p10: number; p50: number; p90: number; min: number; max: number }>;
  riskProfile: { riskLevel: string; probabilityOfFailure: number; potentialLoss: number; potentialGain: number };
}

