import DataTable, { type Column } from "@/components/DataTable";
import { getServerAdminPb } from "@/lib/pb/adminFetch";

type QuizQuestion = {
  id: string;
  question_text?: string;
  answers?: unknown; // stored as JSON in PocketBase
  correct_answer_index?: number;
  difficulty?: string | number;
  created: string;
};

export const dynamic = "force-dynamic";

function formatAnswers(value: unknown) {
  if (Array.isArray(value)) return value.join(", ");
  if (typeof value === "string") return value;
  if (value && typeof value === "object") return JSON.stringify(value);
  return "—";
}

export default async function QuizzesPage({
  searchParams,
}: {
  searchParams?: { page?: string };
}) {
  const pb = await getServerAdminPb();

  let rows: QuizQuestion[] = [];
  let error: string | null = null;

  const page = Math.max(1, Number(searchParams?.page ?? "1") || 1);

  try {
    const res = await pb.collection("quiz_questions").getList(page, 50, {
      sort: "-created",
    });

    rows = res.items as unknown as QuizQuestion[];
  } catch (err) {
    console.error(err);
    error = "Failed to load quiz_questions from PocketBase.";
  }

  const columns: Column<QuizQuestion>[] = [
    { key: "id", header: "ID", render: (r) => <span>{r.id}</span> },
    {
      key: "question_text",
      header: "Question",
      render: (r) => (
        <span className="text-zinc-100">{r.question_text ?? "—"}</span>
      ),
    },
    {
      key: "answers",
      header: "Answers",
      render: (r) => (
        <span className="text-zinc-300">{formatAnswers(r.answers)}</span>
      ),
    },
    {
      key: "correct_answer_index",
      header: "Correct Index",
      render: (r) => (
        <span className="font-semibold text-[#F0833A]">
          {r.correct_answer_index ?? "—"}
        </span>
      ),
    },
    {
      key: "difficulty",
      header: "Difficulty",
      render: (r) => (
        <span className="rounded-full border border-zinc-700 bg-zinc-900/30 px-2 py-0.5 text-xs text-zinc-200">
          {r.difficulty ?? "—"}
        </span>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="db-card p-5">
        <h1 className="text-xl font-semibold">
          Quiz Management <span className="text-[#1C3D7A]">/ Questions</span>
        </h1>
        <p className="mt-1 text-sm text-zinc-400">
          Listing records from{" "}
          <code className="text-zinc-200">quiz_questions</code>.
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
