"use client";

import { useState } from "react";
import { post } from "@/lib/api-client";

export default function CounterfactualPage() {
  const [runA, setRunA] = useState("");
  const [runB, setRunB] = useState("");
  const [result, setResult] = useState<Record<string, unknown> | null>(null);

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Counterfactual</h1>
      <div className="flex gap-2">
        <input className="border px-2 py-1" placeholder="run A" value={runA} onChange={(e) => setRunA(e.target.value)} />
        <input className="border px-2 py-1" placeholder="run B" value={runB} onChange={(e) => setRunB(e.target.value)} />
        <button
          className="bg-black px-3 py-1 text-white"
          onClick={() =>
            post<Record<string, unknown>>("/counterfactual", { runA, runB }).then(setResult)
          }
        >
          Diff
        </button>
      </div>
      {result && <pre className="text-xs">{JSON.stringify(result, null, 2)}</pre>}
    </main>
  );
}

