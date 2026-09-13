"use client";

import { useEffect, useState } from "react";
import { get } from "@/lib/api-client";

export default function WeaknessesPage({ params }: { params: { id: string } }) {
  const [items, setItems] = useState<{ title: string; reason: string; severity: string }[]>([]);

  useEffect(() => {
    get<{ title: string; reason: string; severity: string }[]>(`/organizations/${params.id}/weaknesses`).then(setItems);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id]);

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Weaknesses</h1>
      {items.map((w, i) => (
        <div key={i} className="border p-3">
          <p className="font-bold">{w.severity} — {w.title}</p>
          <p>{w.reason}</p>
        </div>
      ))}
    </main>
  );
}

