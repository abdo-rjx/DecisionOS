"use client";

import { useEffect, useState } from "react";
import { get } from "@/lib/api-client";
import type { SimulationRun } from "@/lib/types";
import { CartesianGrid, Legend, Line, LineChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

const badge: Record<string, string> = { GREEN: "🟢", YELLOW: "🟡", RED: "🔴", BLACK: "⚫" };

export default function SimulatePage({ params, searchParams }: { params: { decisionId: string }; searchParams: { scenario?: string } }) {
  const [run, setRun] = useState<SimulationRun | null>(null);
  const [explanation, setExplanation] = useState("");

  useEffect(() => {
    if (!searchParams.scenario) return;
    get(`/scenarios/${searchParams.scenario}/simulate?horizonMonths=12`, { method: "POST" } as RequestInit)
      .catch(() => null)
      .finally(async () => {
        // Latest run for scenario: fetch decision runs via history is org-scoped; use direct latest by listing scenarios runs:
        const runs = await get<SimulationRun[]>(`/decisions/${params.decisionId}/scenarios`).catch(() => []);
        void runs;
      });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Simulation Results</h1>
      <p className="text-sm">Open a scenario and click Simulate, then view history for charts. Direct run view by ID:</p>
      <RunViewer onRun={setRun} onExplanation={setExplanation} />
      {run && (
        <>
          <p>Risk: {badge[run.riskProfile.riskLevel] ?? ""} {run.riskProfile.riskLevel}</p>
          <div style={{ width: "100%", height: 300 }}>
            <ResponsiveContainer>
              <LineChart data={run.timeSeriesResult}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="month" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="revenue" dot={false} />
                <Line type="monotone" dataKey="cash_flow" dot={false} />
              </LineChart>
            </ResponsiveContainer>
          </div>
          {explanation && <p className="border p-3">{explanation}</p>}
        </>
      )}
    </main>
  );
}

function RunViewer({ onRun, onExplanation }: { onRun: (r: SimulationRun) => void; onExplanation: (s: string) => void }) {
  const [id, setId] = useState("");
  return (
    <div className="flex gap-2">
      <input className="border px-2 py-1" placeholder="run id" value={id} onChange={(e) => setId(e.target.value)} />
      <button
        className="bg-black px-3 py-1 text-white"
        onClick={async () => {
          const r = await get<SimulationRun>(`/simulation-runs/${id}`);
          onRun(r);
          const e = await get<{ narrative: string }>(`/simulation-runs/${id}/explanation`).catch(() => ({ narrative: "" }));
          onExplanation(e.narrative);
        }}
      >
        Load
      </button>
    </div>
  );
}

