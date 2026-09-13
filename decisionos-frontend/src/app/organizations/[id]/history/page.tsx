"use client";

import { useEffect, useState } from "react";
import { get } from "@/lib/api-client";
import type { SimulationRun } from "@/lib/types";

export default function HistoryPage({ params }: { params: { id: string } }) {
  const [runs, setRuns] = useState<SimulationRun[]>([]);

  useEffect(() => {
    get<SimulationRun[]>(`/organizations/${params.id}/history`).then(setRuns);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id]);

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Simulation History</h1>
      <ul className="text-sm">
        {runs.map((r) => (
          <li key={r.id}>{r.id} — risk {r.riskProfile.riskLevel} — {r.timeHorizonMonths} months</li>
        ))}
      </ul>
    </main>
  );
}

