"use client";

import { useRouter } from "next/navigation";

export default function LogoutButton() {
  const router = useRouter();

  async function logout() {
    await fetch("/api/auth/admin/logout", { method: "POST" });
    router.replace("/login");
  }

  return (
    <button
      onClick={logout}
      className="db-btn-secondary inline-flex w-full items-center justify-center rounded-md px-3 py-2 text-sm font-semibold"
      type="button"
    >
      Logout
    </button>
  );
}
