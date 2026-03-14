import { NextRequest, NextResponse } from "next/server";

import { ADMIN_TOKEN_COOKIE } from "@/lib/auth/cookies";

export function middleware(req: NextRequest) {
  const { pathname } = req.nextUrl;

  // Protect all /admin routes (including nested).
  if (pathname.startsWith("/admin")) {
    const token = req.cookies.get(ADMIN_TOKEN_COOKIE)?.value;

    if (!token) {
      const loginUrl = new URL("/login", req.url);
      loginUrl.searchParams.set("next", pathname);
      return NextResponse.redirect(loginUrl);
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/admin/:path*"],
};
