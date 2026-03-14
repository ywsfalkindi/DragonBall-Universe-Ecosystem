import DataTable, { type Column } from "@/components/DataTable";
import { getServerAdminPb } from "@/lib/pb/adminFetch";

type MangaChapter = {
  id: string;
  title?: string;
  chapter_number?: number;
  created: string;
  updated: string;
};

export const dynamic = "force-dynamic";

export default async function MangaPage() {
  const pb = await getServerAdminPb();

  let rows: MangaChapter[] = [];
  let error: string | null = null;

  try {
    const res = await pb.collection("manga_chapters").getList(1, 50, {
      sort: "-created",
    });

    rows = res.items as unknown as MangaChapter[];
  } catch {
    error = "Failed to load manga_chapters from PocketBase.";
  }

  const columns: Column<MangaChapter>[] = [
    { key: "id", header: "ID", render: (r) => <span>{r.id}</span> },
    {
      key: "chapter_number",
      header: "Chapter",
      render: (r) => (
        <span className="font-medium text-zinc-100">
          {r.chapter_number ?? "—"}
        </span>
      ),
    },
    {
      key: "title",
      header: "Title",
      render: (r) => (
        <span className="text-zinc-200">{r.title || "(untitled)"}</span>
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
          Manga Management <span className="text-[#F0833A]">/ Chapters</span>
        </h1>
        <p className="mt-1 text-sm text-zinc-400">
          Listing records from <code className="text-zinc-200">manga_chapters</code>
          .
        </p>
      </div>

      {error ? (
        <div className="rounded-md border border-red-500/40 bg-red-500/10 px-4 py-3 text-sm text-red-200">
          {error}
        </div>
      ) : null}

      <DataTable columns={columns} rows={rows} loading={false} />
    </div>
  );
}
