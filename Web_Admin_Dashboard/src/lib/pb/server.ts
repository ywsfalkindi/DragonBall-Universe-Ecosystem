import PocketBase from "pocketbase";

export const PB_URL =
  process.env.POCKETBASE_URL ?? "http://192.168.3.23:8090";

/**
 * Server-side PocketBase client.
 *
 * IMPORTANT:
 * - Instantiate per-request to avoid leaking auth state between users/requests.
 * - We do NOT persist admin auth in this instance; auth is stored in a httpOnly cookie.
 */
export function getServerPb() {
  return new PocketBase(PB_URL);
}
