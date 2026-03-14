"use client";

import { useMemo, useState } from "react";
import PocketBase from "pocketbase";

type Props = {
  open: boolean;
  onClose: () => void;
  onCreated?: () => void;
};

type Arc = "classic" | "z" | "super";

function getClientPb() {
  const baseUrl =
    process.env.NEXT_PUBLIC_POCKETBASE_URL ||
    process.env.NEXT_PUBLIC_PB_URL ||
    "http://127.0.0.1:8090";

  return new PocketBase(baseUrl);
}

export default function UploadChapterModal({ open, onClose, onCreated }: Props) {
  const pb = useMemo(() => getClientPb(), []);

  const [arc, setArc] = useState<Arc>("classic");
  const [chapterNumber, setChapterNumber] = useState<number>(1);
  const [title, setTitle] = useState<string>("");
  const [files, setFiles] = useState<File[]>([]);

  const [submitting, setSubmitting] = useState(false);
  const [toast, setToast] = useState<{ type: "success" | "error"; message: string } | null>(
    null,
  );

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setToast(null);

    if (!files.length) {
      setToast({ type: "error", message: "Please select at least 1 page image." });
      return;
    }

    setSubmitting(true);
    try {
      const formData = new FormData();
      formData.append("arc", arc);
      formData.append("chapter_number", String(chapterNumber));
      formData.append("title", title);

      // PocketBase multi-file field: append same key multiple times.
      files.forEach((f) => formData.append("pages", f));

      await pb.collection("manga_chapters").create(formData);

      // Fire-and-forget push notification via server route (does not block the success UX).
      fetch("/api/notifications/send", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: "New Manga Chapter",
          body: `Arc: ${arc} • Chapter ${chapterNumber}${title ? ` — ${title}` : ""}`,
          topic: "new_chapters",
        }),
      }).catch((e) => console.warn("Notification request failed", e));

      setToast({ type: "success", message: "Chapter uploaded successfully." });
      setFiles([]);
      setTitle("");
      onCreated?.();

      // close shortly after success (keeps toast visible briefly)
      setTimeout(() => {
        onClose();
        setToast(null);
      }, 700);
    } catch (err) {
      console.error(err);
      setToast({ type: "error", message: "Upload failed. Check console / PocketBase logs." });
    } finally {
      setSubmitting(false);
    }
  }

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div
        className="absolute inset-0 bg-black/70"
        onClick={() => (submitting ? null : onClose())}
      />
      <div className="relative w-full max-w-xl db-card p-5">
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-lg font-semibold text-zinc-100">Upload Manga Chapter</h2>
            <p className="mt-1 text-sm text-zinc-400">
              Creates a record in{" "}
              <code className="text-zinc-200">manga_chapters</code> and uploads page images.
            </p>
          </div>

          <button
            type="button"
            className="db-btn-secondary px-3 py-2 text-sm"
            onClick={onClose}
            disabled={submitting}
          >
            Close
          </button>
        </div>

        {toast ? (
          <div
            className={[
              "mt-4 rounded-md border px-4 py-3 text-sm",
              toast.type === "success"
                ? "border-emerald-500/40 bg-emerald-500/10 text-emerald-100"
                : "border-red-500/40 bg-red-500/10 text-red-200",
            ].join(" ")}
          >
            {toast.message}
          </div>
        ) : null}

        <form className="mt-4 space-y-4" onSubmit={handleSubmit}>
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className="space-y-1">
              <div className="text-sm text-zinc-300">Arc</div>
              <select
                className="w-full rounded-md border border-zinc-700 bg-zinc-950 px-3 py-2 text-zinc-100"
                value={arc}
                onChange={(e) => setArc(e.target.value as Arc)}
                disabled={submitting}
              >
                <option value="classic">classic</option>
                <option value="z">z</option>
                <option value="super">super</option>
              </select>
            </label>

            <label className="space-y-1">
              <div className="text-sm text-zinc-300">Chapter Number</div>
              <input
                type="number"
                className="w-full rounded-md border border-zinc-700 bg-zinc-950 px-3 py-2 text-zinc-100"
                value={chapterNumber}
                min={1}
                onChange={(e) => setChapterNumber(Number(e.target.value))}
                disabled={submitting}
              />
            </label>
          </div>

          <label className="space-y-1 block">
            <div className="text-sm text-zinc-300">Title</div>
            <input
              type="text"
              className="w-full rounded-md border border-zinc-700 bg-zinc-950 px-3 py-2 text-zinc-100"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              disabled={submitting}
              placeholder="e.g., The Legendary Super Saiyan"
            />
          </label>

          <label className="space-y-1 block">
            <div className="text-sm text-zinc-300">Pages (images)</div>
            <input
              type="file"
              accept="image/*"
              multiple
              className="block w-full text-sm text-zinc-200 file:mr-4 file:rounded-md file:border-0 file:bg-zinc-800 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-zinc-100 hover:file:bg-zinc-700"
              onChange={(e) => setFiles(Array.from(e.target.files ?? []))}
              disabled={submitting}
            />
            <div className="text-xs text-zinc-500">
              Selected: <span className="text-zinc-300">{files.length}</span>
            </div>
          </label>

          <div className="flex items-center justify-end gap-3 pt-2">
            <button
              type="button"
              className="db-btn-secondary px-4 py-2"
              onClick={onClose}
              disabled={submitting}
            >
              Cancel
            </button>
            <button type="submit" className="db-btn-primary px-4 py-2" disabled={submitting}>
              {submitting ? "Uploading..." : "Upload Chapter"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
