"use client";

import { useEffect, useState } from "react";
import { get, post } from "@/lib/api-client";
import type { Scenario } from "@/lib/types";

export default function ScenariosPage({ params }: { params: { id: string; decisionId: string } }) {
  const [items, setItems] = useState<Scenario[]>([]);

  async function load() {
    setItems(await get(`/decisions/${params.decisionId}/scenarios`));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.decisionId]);

  async function simulate(id: string) {
    await post(`/scenarios/${id}/simulate?horizonMonths=12`, {});
    window.location.href = `/organizations/${params.id}/decisions/${params.decisionId}/simulate?scenario=${id}`;
  }

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Scenarios</h1>
      <button className="bg-black px-3 py-1 text-white" onClick={() => post(`/decisions/${params.decisionId}/scenarios`, {}).then(load)}>Regenerate</button>
      {items.map((s) => (
        <div key={s.id} className="border p-3">
          <p className="font-bold">{s.type}</p>
          <p className="text-sm">{s.narrative}</p>
          <button className="underline" onClick={() => simulate(s.id)}>Simulate</button>
        </div>
      ))}
    </main>
  );
}

