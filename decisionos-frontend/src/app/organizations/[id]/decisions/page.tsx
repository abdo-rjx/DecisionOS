"use client";

import { useEffect, useState } from "react";
import { get, post } from "@/lib/api-client";

export default function DecisionsPage({ params }: { params: { id: string } }) {
  const [items, setItems] = useState<{ id: string; title: string }[]>([]);
  const [title, setTitle] = useState("Hire 5 people");
  const [type, setType] = useState("HIRING");

  async function load() {
    setItems(await get(`/organizations/${params.id}/decisions`));
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id]);

  async function create() {
    const d = await post<{ id: string }>(`/organizations/${params.id}/decisions`, { title, decisionType: type });
    await post(`/decisions/${d.id}/scenarios`, {});
    await load();
  }

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Decisions</h1>
      <div className="flex gap-2">
        <input className="border px-2 py-1" value={title} onChange={(e) => setTitle(e.target.value)} />
        <select className="border px-2 py-1" value={type} onChange={(e) => setType(e.target.value)}>
          <option>HIRING</option><option>MARKETING_SPEND</option><option>PRICE_CHANGE</option>
          <option>NEW_BRANCH</option><option>COST_CUTTING</option><option>CUSTOM</option>
        </select>
        <button className="bg-black px-3 py-1 text-white" onClick={create}>Create + scenarios</button>
      </div>
      <ul>
        {items.map((d) => (
          <li key={d.id}>
            <a className="text-blue-600 underline" href={`/organizations/${params.id}/decisions/${d.id}/scenarios`}>{d.title}</a>
          </li>
        ))}
      </ul>
    </main>
  );
}

