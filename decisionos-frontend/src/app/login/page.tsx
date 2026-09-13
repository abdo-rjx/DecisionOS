"use client";

import { useState } from "react";
import { post } from "@/lib/api-client";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [msg, setMsg] = useState("");

  async function auth(path: string) {
    try {
      const r = await post<{ token: string }>(`/auth/${path}`, { email, password });
      window.localStorage.setItem("decisionos_token", r.token);
      setMsg("Saved token. Go to /");
    } catch (e) {
      setMsg(e instanceof Error ? e.message : "Failed");
    }
  }

  return (
    <main className="space-y-4">
      <h1 className="text-2xl font-bold">Login</h1>
      <input className="border px-2 py-1" placeholder="email" value={email} onChange={(e) => setEmail(e.target.value)} />
      <input className="border px-2 py-1" placeholder="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} />
      <div className="flex gap-2">
        <button className="bg-black px-3 py-1 text-white" onClick={() => auth("register")}>Register</button>
        <button className="bg-black px-3 py-1 text-white" onClick={() => auth("login")}>Login</button>
      </div>
      {msg && <p>{msg}</p>}
    </main>
  );
}
