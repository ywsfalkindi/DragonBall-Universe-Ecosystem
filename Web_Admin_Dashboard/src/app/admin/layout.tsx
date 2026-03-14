import type { ReactNode } from "react";
import Link from "next/link";

import LogoutButton from "@/app/admin/logout-button";

const navItems = [
  { href: "/admin", label: "Dashboard" },
  { href: "/admin/manga", label: "Manga" },
  { href: "/admin/quizzes", label: "Quizzes" },
  { href: "/admin/users", label: "Users" },
];

export default function AdminLayout({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-dvh">
      <div className="mx-auto flex min-h-dvh max-w-7xl">
        <aside className="hidden w-64 shrink-0 border-r border-zinc-800 bg-[#0f0f0f] p-4 md:block">
          <div className="mb-6">
            <div className="text-lg font-semibold tracking-wide">
              Dragon Ball <span className="text-[#F0833A]">Admin</span>
            </div>
            <div className="text-xs text-zinc-500">PocketBase Control Room</div>
          </div>

          <nav className="space-y-1">
            {navItems.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="block rounded-md border border-transparent px-3 py-2 text-sm text-zinc-200 hover:border-zinc-800 hover:bg-zinc-900/40"
              >
                {item.label}
              </Link>
            ))}
          </nav>

          <div className="mt-6 border-t border-zinc-800 pt-4">
            <LogoutButton />
          </div>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="sticky top-0 z-10 border-b border-zinc-800 bg-[#121212]/80 px-4 py-3 backdrop-blur md:px-6">
            <div className="flex items-center justify-between">
              <div className="text-sm text-zinc-400">
                <span className="text-zinc-200">Admin</span> /{" "}
                <span className="text-[#1C3D7A]">Dashboard</span>
              </div>

              <div className="md:hidden">
                <LogoutButton />
              </div>
            </div>

            <div className="mt-3 flex gap-2 overflow-x-auto md:hidden">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="whitespace-nowrap rounded-full border border-zinc-700 bg-zinc-900/30 px-3 py-1 text-xs text-zinc-200 hover:border-[#F0833A]"
                >
                  {item.label}
                </Link>
              ))}
            </div>
          </header>

          <main className="min-w-0 flex-1 px-4 py-6 md:px-6">{children}</main>
        </div>
      </div>
    </div>
  );
}
