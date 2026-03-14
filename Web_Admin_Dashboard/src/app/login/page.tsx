"use client";

import { useMemo, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";

export default function LoginPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const nextPath = useMemo(
    () => searchParams.get("next") || "/admin",
    [searchParams],
  );

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setLoading(true);

    try {
      const res = await fetch("/api/auth/admin/login", {
        method: "POST",
        headers: { "content-type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      if (!res.ok) {
        const data = (await res.json().catch(() => null)) as
          | { error?: string }
          | null;
        throw new Error(data?.error || "Login failed.");
      }

      router.replace(nextPath);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed.");
      setLoading(false);
    }
  }

  return (
    <main className="flex min-h-dvh items-center justify-center px-4">
      <div className="db-card w-full max-w-md p-6 shadow-lg">
        <div className="mb-6">
          <h1 className="text-2xl font-semibold">Admin Login</h1>
          <p className="mt-1 text-sm text-zinc-400">
            Sign in with PocketBase Admin credentials.
          </p>
        </div>

        <form onSubmit={onSubmit} className="space-y-4">
          <label className="block">
            <span className="text-sm text-zinc-300">Email</span>
            <input
              className="mt-1 w-full rounded-md border border-zinc-700 bg-[#0f0f0f] px-3 py-2 text-zinc-100 outline-none ring-0 placeholder:text-zinc-600 focus:border-[#F0833A]"
              type="email"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="admin@example.com"
              required
              disabled={loading}
            />
          </label>

          <label className="block">
            <span className="text-sm text-zinc-300">Password</span>
            <input
              className="mt-1 w-full rounded-md border border-zinc-700 bg-[#0f0f0f] px-3 py-2 text-zinc-100 outline-none ring-0 placeholder:text-zinc-600 focus:border-[#1C3D7A]"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="••••••••"
              required
              disabled={loading}
            />
          </label>

          {error ? (
            <div className="rounded-md border border-red-500/40 bg-red-500/10 px-3 py-2 text-sm text-red-200">
              {error}
            </div>
          ) : null}

          <button
            type="submit"
            className="db-btn-primary inline-flex w-full items-center justify-center rounded-md px-4 py-2 font-semibold disabled:opacity-60"
            disabled={loading}
          >
            {loading ? (
              <span className="inline-flex items-center gap-2">
                <span className="h-4 w-4 animate-spin rounded-full border-2 border-black/30 border-t-black" />
                Signing in...
              </span>
            ) : (
              "Sign In"
            )}
          </button>
        </form>

        <div className="mt-6 border-t border-zinc-800 pt-4 text-xs text-zinc-500">
          PocketBase server:{" "}
          <span className="text-zinc-300">192.168.3.23:8090</span>
        </div>
      </div>
    </main>
  );
}
