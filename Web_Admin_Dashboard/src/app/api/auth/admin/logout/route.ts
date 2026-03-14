import { NextResponse } from "next/server";

import { ADMIN_TOKEN_COOKIE } from "@/lib/auth/cookies";

export async function POST() {
  const res = NextResponse.json({ ok: true });

  res.cookies.set({
    name: ADMIN_TOKEN_COOKIE,
    value: "",
    httpOnly: true,
    sameSite: "lax",
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge: 0,
  });

  return res;
}
