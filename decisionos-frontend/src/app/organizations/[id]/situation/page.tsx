"use client";

import { useEffect, useState } from "react";
import { get, post } from "@/lib/api-client";

export default function SituationPage({ params }: { params: { id: string } }) {
  const [data, setData] = useState<Record<string, unknown> | null>(null);

  async function load() {
    try {
      setData(await get(`/organizations/${params.id}/situation-analysis/latest`));
    } catch {
      setData(await post(`/organizations/${params.id}/situation-analysis`, {}));
    }
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id]);

  if (!data) return <main>Loading...</main>;
  const cards = ["financial", "operational", "market", "risk"];

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Situation Analysis</h1>
      <div className="grid grid-cols-2 gap-4">
        {cards.map((c) => (
          <div key={c} className="border p-4">
            <h2 className="font-bold capitalize">{c}</h2>
            <pre className="text-xs">{JSON.stringify(data[c], null, 2)}</pre>
          </div>
        ))}
      </div>
    </main>
  );
}

