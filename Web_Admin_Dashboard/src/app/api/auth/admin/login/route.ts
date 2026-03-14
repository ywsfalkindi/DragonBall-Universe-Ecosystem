import { NextResponse } from "next/server";

import { ADMIN_TOKEN_COOKIE } from "@/lib/auth/cookies";
import { getServerPb } from "@/lib/pb/server";

export async function POST(req: Request) {
  const { email, password } = (await req.json()) as {
    email?: string;
    password?: string;
  };

  if (!email || !password) {
    return NextResponse.json(
      { error: "Email and password are required." },
      { status: 400 },
    );
  }

  const pb = getServerPb();

  try {
    const authData = await pb.admins.authWithPassword(email, password);

    // Store admin token in an httpOnly cookie so middleware can protect /admin routes.
    const res = NextResponse.json({ ok: true });

    res.cookies.set({
      name: ADMIN_TOKEN_COOKIE,
      value: authData.token,
      httpOnly: true,
      sameSite: "lax",
      secure: process.env.NODE_ENV === "production",
      path: "/",
      maxAge: 60 * 60 * 24, // 1 day
    });

    return res;
  } catch {
    return NextResponse.json(
      { error: "Invalid admin credentials." },
      { status: 401 },
    );
  }
}
