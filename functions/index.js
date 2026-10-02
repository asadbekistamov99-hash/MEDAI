/**
 * Server-side admin verification for MedAI.
 *
 * The app's Firestore rules (see ../firestore.rules) already gate every admin write on
 * `request.auth.token.admin == true` — a Firebase custom claim baked into the user's ID
 * token. That claim can only be set from a trusted server context (this file), never by the
 * client, so it's the actual security boundary. Before this function existed, nothing ever
 * set that claim, so admin status only ever lived in client-side checks (a hardcoded email
 * string) that a rooted device or a decompiled build could bypass; the rules quietly did
 * nothing because the claim was always absent.
 *
 * Deploy with: `firebase deploy --only functions` (requires `firebase login` and a Firebase
 * project already linked via `firebase use --add` — see the repo root README/FIRESTORE_SCHEMA.md
 * for project setup). Requires Node 20 and `npm install` inside this functions/ directory first.
 */

const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { beforeUserCreated } = require("firebase-functions/v2/identity");
const { defineSecret } = require("firebase-functions/params");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const logger = require("firebase-functions/logger");

initializeApp();

// Keep this in sync with SUPER_ADMIN_EMAIL in
// app/src/main/java/com/example/ui/AppViewModel.kt. There is intentionally exactly one
// admin account for now; turning this into a Firestore-managed allowlist is a reasonable
// next step once there's more than one admin.
const SUPER_ADMIN_EMAIL = "asadbekistamov99@gmail.com";

function isSuperAdminEmail(email) {
  return typeof email === "string" && email.trim().toLowerCase() === SUPER_ADMIN_EMAIL.toLowerCase();
}

/**
 * Runs before a new Firebase Auth account is created (Google Sign-In included). If the
 * email matches the super-admin account, the resulting ID token already carries
 * `admin: true` from the very first sign-in — no manual step needed for brand-new accounts.
 */
exports.setAdminClaimOnCreate = beforeUserCreated((event) => {
  const user = event.data;
  if (isSuperAdminEmail(user.email)) {
    logger.info(`Granting admin claim to new user ${user.uid} (${user.email})`);
    return {
      customClaims: { admin: true },
    };
  }
  return {};
});

/**
 * Self-service callable for an account that already existed before setAdminClaimOnCreate was
 * deployed. Any signed-in caller can invoke this, but it only ever grants the claim to the
 * caller's OWN uid, and only if the caller's own verified email matches SUPER_ADMIN_EMAIL —
 * it can never be used to grant admin to a different account. Call it once from the app (or
 * via the Firebase console's function tester) after signing in with the admin account; every
 * sign-in after that already carries the claim without calling this again.
 */
exports.claimAdminIfEligible = onCall(async (request) => {
  const auth = request.auth;
  if (!auth) {
    throw new HttpsError("unauthenticated", "Sign in first.");
  }
  if (!isSuperAdminEmail(auth.token.email)) {
    throw new HttpsError("permission-denied", "This account is not the configured admin account.");
  }

  await getAuth().setCustomUserClaims(auth.uid, { admin: true });
  logger.info(`Granted admin claim to existing user ${auth.uid} (${auth.token.email}) via claimAdminIfEligible`);
  return { admin: true };
});

// ---------------------------------------------------------------------------------------------
// Gemini proxy. The Gemini API key lives ONLY here (Firebase secret), never in the APK.
// Set it once with:  firebase functions:secrets:set GEMINI_API_KEY
// ---------------------------------------------------------------------------------------------
const GEMINI_API_KEY = defineSecret("GEMINI_API_KEY");
const GEMINI_MODELS = ["gemini-2.5-flash", "gemini-2.0-flash"];
const MAX_PROMPT_CHARS = 30000;
const MAX_IMAGE_BASE64_CHARS = 7 * 1024 * 1024; // ~5 MB of image data
const RATE_LIMIT_PER_MINUTE = 20;
const rateBuckets = new Map(); // uid -> [timestamps]; best-effort, per function instance

function checkRateLimit(uid) {
  const now = Date.now();
  const recent = (rateBuckets.get(uid) || []).filter((t) => now - t < 60000);
  if (recent.length >= RATE_LIMIT_PER_MINUTE) {
    throw new HttpsError("resource-exhausted", "Too many AI requests, please wait a minute.");
  }
  recent.push(now);
  rateBuckets.set(uid, recent);
}

exports.askGemini = onCall({ secrets: [GEMINI_API_KEY], timeoutSeconds: 60, memory: "512MiB" }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in first.");
  }
  checkRateLimit(request.auth.uid);

  const { prompt, systemInstruction, imageBase64, mimeType } = request.data || {};
  if (typeof prompt !== "string" || !prompt.trim() || prompt.length > MAX_PROMPT_CHARS) {
    throw new HttpsError("invalid-argument", "Invalid prompt.");
  }
  const parts = [{ text: prompt }];
  if (imageBase64) {
    if (typeof imageBase64 !== "string" || imageBase64.length > MAX_IMAGE_BASE64_CHARS ||
        !["image/jpeg", "image/png", "image/webp"].includes(mimeType)) {
      throw new HttpsError("invalid-argument", "Invalid image.");
    }
    parts.push({ inlineData: { mimeType, data: imageBase64 } });
  }
  const body = { contents: [{ parts }] };
  if (typeof systemInstruction === "string" && systemInstruction.trim()) {
    body.systemInstruction = { parts: [{ text: systemInstruction.slice(0, 4000) }] };
  }

  for (const model of GEMINI_MODELS) {
    try {
      const res = await fetch(
        `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json", "x-goog-api-key": GEMINI_API_KEY.value() },
          body: JSON.stringify(body),
        }
      );
      if (!res.ok) {
        logger.warn(`Gemini ${model} responded ${res.status}`);
        continue;
      }
      const json = await res.json();
      const text = json?.candidates?.[0]?.content?.parts?.[0]?.text;
      if (text && text.trim()) {
        const usage = json.usageMetadata || {};
        return { text, model, totalTokens: usage.totalTokenCount || 0 };
      }
    } catch (e) {
      logger.warn(`Gemini ${model} failed: ${e.message}`);
    }
  }
  throw new HttpsError("unavailable", "AI service is temporarily unavailable.");
});
