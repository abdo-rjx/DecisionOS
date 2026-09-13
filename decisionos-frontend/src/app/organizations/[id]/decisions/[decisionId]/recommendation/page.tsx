"use client";

import { useState } from "react";
import { post } from "@/lib/api-client";

export default function RecommendationPage({ params }: { params: { decisionId: string } }) {
  const [result, setResult] = useState<Record<string, unknown> | null>(null);

  async function run() {
    setResult(await post(`/decisions/${params.decisionId}/recommendation`, { priorities: { growth: 1, risk: 1, cost: 1 } }));
  }

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Recommendation</h1>
      <button className="bg-black px-3 py-1 text-white" onClick={run}>Get recommendation</button>
      {result && <pre className="text-xs">{JSON.stringify(result, null, 2)}</pre>}
    </main>
  );
}

