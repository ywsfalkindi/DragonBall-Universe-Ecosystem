"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import UploadChapterModal from "./UploadChapterModal";

export default function UploadChapterClient() {
  const router = useRouter();
  const [open, setOpen] = useState(false);

  return (
    <>
      <button className="db-btn-primary px-4 py-2" onClick={() => setOpen(true)}>
        Upload Chapter
      </button>

      <UploadChapterModal
        open={open}
        onClose={() => setOpen(false)}
        onCreated={() => {
          // Refresh server component data table
          router.refresh();
        }}
      />
    </>
  );
}
