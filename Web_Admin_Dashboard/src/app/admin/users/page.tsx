import DataTable, { type Column } from "@/components/DataTable";
import { getAdminListCached } from "@/lib/pb/adminFetch";

type UserStats = {
  id: string;
  device_id?: string;
  power_level?: number;
  senzu_beans?: number;
  created: string;
};

export const dynamic = "force-dynamic";

export default async function UsersPage({
  searchParams,
}: {
  searchParams?: { page?: string };
}) {
  let rows: UserStats[] = [];
  let error: string | null = null;

  const page = Math.max(1, Number(searchParams?.page ?? "1") || 1);

  try {
    const res = await getAdminListCached<UserStats>("user_stats", page, 50, {
      sort: "-created",
    });

    rows = res.items as unknown as UserStats[];
  } catch (err) {
    console.error(err);
    error = "Failed to load user_stats from PocketBase.";
  }

  const columns: Column<UserStats>[] = [
    { key: "id", header: "ID", render: (r) => <span>{r.id}</span> },
    {
      key: "device_id",
      header: "Device ID",
      render: (r) => (
        <span className="text-zinc-200">{r.device_id ?? "—"}</span>
      ),
    },
    {
      key: "power_level",
      header: "Power Level",
      render: (r) => (
        <span className="font-semibold text-[#F0833A]">
          {r.power_level ?? "—"}
        </span>
      ),
    },
    {
      key: "senzu_beans",
      header: "Senzu Beans",
      render: (r) => (
        <span className="font-semibold text-[#1C3D7A]">
          {r.senzu_beans ?? "—"}
        </span>
      ),
    },
    {
      key: "created",
      header: "Created",
      render: (r) => (
        <span className="whitespace-nowrap text-zinc-400">
          {new Date(r.created).toLocaleString()}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="db-card p-5">
        <h1 className="text-xl font-semibold">
          User Stats <span className="text-[#F0833A]">/ Tracking</span>
        </h1>
        <p className="mt-1 text-sm text-zinc-400">
          Listing records from <code className="text-zinc-200">user_stats</code>.
        </p>
      </div>

      {error ? (
        <div className="rounded-md border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {error}
        </div>
      ) : null}

      <DataTable
        columns={columns}
        rows={rows}
        loading={false}
        getRowId={(r) => r.id}
      />
    </div>
  );
}
