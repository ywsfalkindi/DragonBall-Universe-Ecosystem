import PocketBase from "pocketbase";

import { unstable_cache } from "next/cache";

import { getAdminTokenFromCookies } from "@/lib/auth/cookies";
import { PB_URL } from "@/lib/pb/server";

/**
 * Create a PocketBase client (server-side) that is authorized as an admin
 * using the token stored in the httpOnly cookie.
 *
 * IMPORTANT:
 * - Create per request to avoid cross-request auth bleed.
 * - We only set the auth token; admin record data isn't required for list views.
 */
export async function getServerAdminPb() {
  const token = await getAdminTokenFromCookies();
  const pb = new PocketBase(PB_URL);

  if (token) {
    pb.authStore.save(token, null as unknown as never);
  }

  return pb;
}

/**
 * Cache helper for PocketBase list reads (server-side).
 *
 * Why:
 * - Avoid duplicate network calls when multiple Server Components request the same list data.
 *
 * Safety:
 * - Cache key includes the admin token so results don't leak across sessions.
 * - Uses a short revalidate window; adjust per collection if needed.
 */
export async function getAdminListCached<T>(
  collection: string,
  page: number,
  perPage: number,
  options: Record<string, unknown> = {},
  revalidateSeconds: number = 10,
) {
  const token = await getAdminTokenFromCookies();

  // If there's no token, don't cache.
  if (!token) {
    const pb = await getServerAdminPb();
    return pb.collection(collection).getList<T>(page, perPage, options as never);
  }

  const cacheKey = [
    "pb_admin_list",
    collection,
    String(page),
    String(perPage),
    JSON.stringify(options),
    token,
  ];

  const cached = unstable_cache(
    async () => {
      const pb = await getServerAdminPb();
      return pb.collection(collection).getList<T>(page, perPage, options as never);
    },
    cacheKey,
    { revalidate: revalidateSeconds },
  );

  return cached();
}
