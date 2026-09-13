"use client";

import { useEffect, useState } from "react";
import { get } from "@/lib/api-client";

export default function DashboardPage({ params }: { params: { id: string } }) {
  const [data, setData] = useState<Record<string, unknown> | null>(null);

  useEffect(() => {
    get<Record<string, unknown>>(`/organizations/${params.id}/dashboard`).then(setData);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id]);

  if (!data) return <main>Loading...</main>;

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Dashboard</h1>
      <p>Decisions: {String(data.decisionCount)} — Runs: {String(data.runCount)}</p>
      <pre className="text-xs">{JSON.stringify(data, null, 2)}</pre>
    </main>
  );
}

