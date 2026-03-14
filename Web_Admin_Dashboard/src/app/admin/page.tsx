export default function AdminDashboardPage() {
  return (
    <div className="space-y-6">
      <div className="db-card p-6">
        <h1 className="text-2xl font-semibold">
          Welcome, <span className="text-[#F0833A]">Saiyan Admin</span>
        </h1>
        <p className="mt-2 text-sm text-zinc-400">
          This is your Dragon Ball themed control center. Real stats coming next.
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <div className="db-card p-4">
          <div className="text-xs text-zinc-400">Total Users</div>
          <div className="mt-2 text-2xl font-bold">—</div>
          <div className="mt-2 text-xs text-zinc-500">Coming soon</div>
        </div>

        <div className="db-card p-4">
          <div className="text-xs text-zinc-400">Manga Chapters</div>
          <div className="mt-2 text-2xl font-bold">—</div>
          <div className="mt-2 text-xs text-zinc-500">Coming soon</div>
        </div>

        <div className="db-card p-4">
          <div className="text-xs text-zinc-400">Quiz Questions</div>
          <div className="mt-2 text-2xl font-bold">—</div>
          <div className="mt-2 text-xs text-zinc-500">Coming soon</div>
        </div>

        <div className="db-card p-4">
          <div className="text-xs text-zinc-400">Server Status</div>
          <div className="mt-2 text-2xl font-bold text-[#1C3D7A]">Online</div>
          <div className="mt-2 text-xs text-zinc-500">
            PocketBase @ 192.168.3.23:8090
          </div>
        </div>
      </div>
    </div>
  );
}
