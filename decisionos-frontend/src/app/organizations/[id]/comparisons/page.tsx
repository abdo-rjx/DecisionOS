"use client";

import { useState } from "react";
import { post } from "@/lib/api-client";

export default function ComparisonsPage() {
  const [ids, setIds] = useState("");
  const [result, setResult] = useState<Record<string, unknown> | null>(null);

  async function run() {
    setResult(await post("/comparisons", { runIds: ids.split(",").map((s) => s.trim()).filter(Boolean) }));
  }

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Comparisons</h1>
      <div className="flex gap-2">
        <input className="border px-2 py-1" placeholder="runId1,runId2" value={ids} onChange={(e) => setIds(e.target.value)} />
        <button className="bg-black px-3 py-1 text-white" onClick={run}>Compare</button>
      </div>
      {result && <pre className="text-xs">{JSON.stringify(result, null, 2)}</pre>}
    </main>
  );
}

