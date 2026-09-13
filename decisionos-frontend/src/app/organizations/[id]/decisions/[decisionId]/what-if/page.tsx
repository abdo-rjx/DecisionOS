"use client";

import { useState } from "react";
import { post } from "@/lib/api-client";

export default function WhatIfPage({ params }: { params: { decisionId: string } }) {
  const [scenarioId, setScenarioId] = useState("");
  const [result, setResult] = useState<Record<string, unknown> | null>(null);

  async function run() {
    setResult(await post(`/scenarios/${scenarioId}/what-if`, {
      horizonMonths: 12,
      variants: [{ sales: 0.05 }, { sales: -0.05 }],
    }));
  }

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">What-If Analysis</h1>
      <div className="flex gap-2">
        <input className="border px-2 py-1" placeholder="scenario id" value={scenarioId} onChange={(e) => setScenarioId(e.target.value)} />
        <button className="bg-black px-3 py-1 text-white" onClick={run}>Run variants</button>
      </div>
      {result && <pre className="text-xs">{JSON.stringify(result, null, 2)}</pre>}
    </main>
  );
}

