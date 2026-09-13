"use client";

import { useEffect, useState } from "react";
import { get, post } from "@/lib/api-client";
import type { Organization } from "@/lib/types";

export default function HomePage() {
  const [orgs, setOrgs] = useState<Organization[]>([]);
  const [name, setName] = useState("");
  const [error, setError] = useState("");

  async function load() {
    try {
      setOrgs(await get<Organization[]>("/organizations"));
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to load");
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function create() {
    try {
      await post("/organizations", { name, employeeCount: 0 });
      setName("");
      await load();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Create failed");
    }
  }

  return (
    <main className="space-y-6">
      <h1 className="text-3xl font-bold">DecisionOS</h1>
      {error && <p className="text-red-600">{error}</p>}
      <div className="flex gap-2">
        <input
          className="border px-2 py-1"
          placeholder="New organization name"
          value={name}
          onChange={(e) => setName(e.target.value)}
        />
        <button className="bg-black px-3 py-1 text-white" onClick={create}>
          Create
        </button>
      </div>
      <ul className="space-y-2">
        {orgs.map((o) => (
          <li key={o.id}>
            <a className="text-blue-600 underline" href={`/organizations/${o.id}`}>
              {o.name}
            </a>
          </li>
        ))}
      </ul>
    </main>
  );
}

