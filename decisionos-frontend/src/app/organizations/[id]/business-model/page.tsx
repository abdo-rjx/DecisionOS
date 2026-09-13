"use client";

import { useEffect, useState } from "react";
import { get, put } from "@/lib/api-client";
import type { GraphEdge, GraphNode } from "@/lib/types";

export default function BusinessModelPage({ params }: { params: { id: string } }) {
  const [nodes, setNodes] = useState<GraphNode[]>([]);
  const [edges, setEdges] = useState<GraphEdge[]>([]);

  async function load() {
    const g = await get<{ nodes: GraphNode[]; edges: GraphEdge[] }>(`/organizations/${params.id}/business-model`);
    setNodes(g.nodes);
    setEdges(g.edges);
  }

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.id]);

  async function updateNode(id: string, currentValue: number) {
    await put(`/organizations/${params.id}/business-model/nodes/${id}`, { currentValue });
    await load();
  }

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Business Model</h1>
      <table className="w-full border text-sm">
        <thead><tr><th className="border px-2">Node</th><th className="border px-2">Value</th><th className="border px-2">Unit</th><th className="border px-2">Edit</th></tr></thead>
        <tbody>
          {nodes.map((n) => (
            <tr key={n.id}>
              <td className="border px-2">{n.key}</td>
              <td className="border px-2">{n.currentValue}</td>
              <td className="border px-2">{n.unit}</td>
              <td className="border px-2">
                <button className="underline" onClick={() => updateNode(n.id, n.currentValue + 1)}>+1</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      <h2 className="font-bold">Edges (formula)</h2>
      <ul className="text-sm">
        {edges.map((e) => (
          <li key={e.id}>{e.formula} (w={e.weight})</li>
        ))}
      </ul>
    </main>
  );
}

