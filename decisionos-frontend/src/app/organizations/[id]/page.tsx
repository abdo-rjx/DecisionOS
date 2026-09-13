"use client";

import { useEffect, useState } from "react";
import { get, put } from "@/lib/api-client";
import type { Organization } from "@/lib/types";

export default function OrganizationProfilePage({ params }: { params: { id: string } }) {
  const [org, setOrg] = useState<Organization | null>(null);
  const [form, setForm] = useState({ name: "", employeeCount: 0, monthlyRevenue: 0, monthlyExpenses: 0 });

  async function load() {
    const o = await get<Organization>(`/organizations/${params.id}`);
    setOrg(o);
    setForm({ name: o.name, employeeCount: o.employeeCount, monthlyRevenue: o.monthlyRevenue, monthlyExpenses: o.monthlyExpenses });
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id]);

  async function save() {
    await put(`/organizations/${params.id}`, form);
    await load();
  }

  if (!org) return <main>Loading...</main>;

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Organization Profile</h1>
      <div className="grid gap-2">
        <input className="border px-2 py-1" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
        <input className="border px-2 py-1" type="number" value={form.employeeCount} onChange={(e) => setForm({ ...form, employeeCount: Number(e.target.value) })} />
        <input className="border px-2 py-1" type="number" value={form.monthlyRevenue} onChange={(e) => setForm({ ...form, monthlyRevenue: Number(e.target.value) })} />
        <input className="border px-2 py-1" type="number" value={form.monthlyExpenses} onChange={(e) => setForm({ ...form, monthlyExpenses: Number(e.target.value) })} />
        <button className="bg-black px-3 py-1 text-white" onClick={save}>Save</button>
      </div>
      <nav className="space-x-3 text-blue-600 underline">
        <a href={`/organizations/${params.id}/dashboard`}>Dashboard</a>
        <a href={`/organizations/${params.id}/business-model`}>Business Model</a>
        <a href={`/organizations/${params.id}/decisions`}>Decisions</a>
      </nav>
    </main>
  );
}

