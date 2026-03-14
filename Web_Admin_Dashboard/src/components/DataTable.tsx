import type { ReactNode } from "react";

export type Column<Row> = {
  key: string;
  header: ReactNode;
  className?: string;
  render: (row: Row) => ReactNode;
};

export default function DataTable<Row>({
  columns,
  rows,
  loading,
  emptyText = "No records found.",
}: {
  columns: Column<Row>[];
  rows: Row[];
  loading?: boolean;
  emptyText?: string;
}) {
  return (
    <div className="db-card overflow-hidden">
      <div className="w-full overflow-x-auto">
        <table className="min-w-[720px] w-full border-collapse text-left text-sm">
          <thead className="bg-zinc-900/40 text-xs uppercase tracking-wider text-zinc-400">
            <tr>
              {columns.map((c) => (
                <th
                  key={c.key}
                  className={`whitespace-nowrap px-4 py-3 ${c.className ?? ""}`}
                >
                  {c.header}
                </th>
              ))}
            </tr>
          </thead>

          <tbody>
            {loading ? (
              [...Array(6)].map((_, i) => (
                <tr key={i} className="border-t border-zinc-800">
                  {columns.map((c) => (
                    <td key={c.key} className="px-4 py-3">
                      <div className="h-4 w-full animate-pulse rounded bg-zinc-800/60" />
                    </td>
                  ))}
                </tr>
              ))
            ) : rows.length === 0 ? (
              <tr className="border-t border-zinc-800">
                <td
                  colSpan={columns.length}
                  className="px-4 py-10 text-center text-zinc-500"
                >
                  {emptyText}
                </td>
              </tr>
            ) : (
              rows.map((row, idx) => (
                <tr
                  key={(row as unknown as { id?: string }).id ?? idx}
                  className="border-t border-zinc-800 hover:bg-zinc-900/20"
                >
                  {columns.map((c) => (
                    <td key={c.key} className="px-4 py-3 align-top">
                      {c.render(row)}
                    </td>
                  ))}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
