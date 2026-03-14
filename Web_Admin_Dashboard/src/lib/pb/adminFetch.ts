import PocketBase from "pocketbase";

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
