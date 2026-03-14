import { NextResponse } from "next/server";
import admin from "firebase-admin";

function getFirebaseAdmin() {
  if (admin.apps.length) return admin;

  const projectId = process.env.FIREBASE_PROJECT_ID;
  const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
  const privateKeyRaw = process.env.FIREBASE_PRIVATE_KEY;

  if (!projectId || !clientEmail || !privateKeyRaw) {
    throw new Error(
      "Missing Firebase env vars. Required: FIREBASE_PROJECT_ID, FIREBASE_CLIENT_EMAIL, FIREBASE_PRIVATE_KEY",
    );
  }

  // .env.local typically stores the key with literal '\n' sequences.
  // firebase-admin requires real newlines.
  const privateKey = privateKeyRaw.replace(/\\n/g, "\n");

  admin.initializeApp({
    credential: admin.credential.cert({
      projectId,
      clientEmail,
      privateKey,
    }),
  });

  return admin;
}

export async function POST(req: Request) {
  try {
    const { title, body, topic } = (await req.json()) as {
      title?: string;
      body?: string;
      topic?: string;
    };

    if (!title || !body) {
      return NextResponse.json(
        { error: "Missing required fields: title, body" },
        { status: 400 },
      );
    }

    const firebaseAdmin = getFirebaseAdmin();

    const message: admin.messaging.Message = {
      notification: { title, body },
      topic: topic?.trim() || "new_chapters",
      data: { title, body },
    };

    const messageId = await firebaseAdmin.messaging().send(message);

    return NextResponse.json({ ok: true, messageId });
  } catch (err) {
    console.error(err);
    return NextResponse.json(
      { ok: false, error: "Failed to send notification." },
      { status: 500 },
    );
  }
}
