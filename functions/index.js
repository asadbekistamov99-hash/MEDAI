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

// Bound to this function below via `secrets: [geminiApiKey]`.
const geminiApiKey = defineSecret("GEMINI_API_KEY");

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

/**
 * Server-side Gemini proxy.
 *
 * The app used to call generativelanguage.googleapis.com directly with GEMINI_API_KEY baked
 * into BuildConfig, which means the key ships inside the APK: anyone who unzips the app gets
 * it, and every request (carrying the user's symptoms, lab reports and prescriptions) goes
 * straight to Google from a device the app has no control over. The key was also passed as a
 * URL query parameter, so it ends up in any proxy or network log along the way.
 *
 * Moving the call here keeps the key server-side and gives one place to rate-limit, log and
 * audit. It also matches what metadata.json already advertises
 * (MAJOR_CAPABILITY_SERVER_SIDE_GEMINI_API), which nothing implemented until now.
 *
 * Config: GEMINI_API_KEY is a Firebase *secret* (not a plain env var), so it is encrypted at
 * rest and never shows up in the deployed bundle. Set it with:
 *
 *   firebase functions:secrets:set GEMINI_API_KEY
 *
 * which prompts for the value. Without it the function returns a clear UNAVAILABLE error and
 * the app falls back to its local response rather than silently sending requests with no key.
 * The plain `process.env.GEMINI_API_KEY` fallback is only used by the local emulator, where
 * defineSecret().value() is empty.
 */

// Mirrors the client's model list and fallback order.
const GEMINI_MODELS = ["gemini-2.5-flash", "gemini-1.5-flash", "gemini-2.0-flash", "gemini-1.5-pro"];
const GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

// Per-user daily call ceiling. A compromised client can otherwise burn the key's quota from
// anywhere in the world; this bounds the blast radius to one account.
const FREE_DAILY_CALL_LIMIT = 30;
const callCounts = new Map(); // uid -> { day, count }

function todayKey() {
  return new Date().toISOString().slice(0, 10);
}

function enforceRateLimit(uid) {
  const day = todayKey();
  const entry = callCounts.get(uid);
  if (!entry || entry.day !== day) {
    callCounts.set(uid, { day, count: 1 });
    return;
  }
  entry.count += 1;
  if (entry.count > FREE_DAILY_CALL_LIMIT) {
    throw new HttpsError(
      "resource-exhausted",
      `Daily AI request limit reached (${FREE_DAILY_CALL_LIMIT}). Try again tomorrow.`
    );
  }
  // Opportunistic cleanup so the map cannot grow without bound on a long-lived instance.
  if (callCounts.size > 5_000) {
    for (const [key, value] of callCounts) {
      if (value.day !== day) callCounts.delete(key);
    }
  }
}

exports.generateContent = onCall({ secrets: [geminiApiKey] }, async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in first.");
  }

  const apiKey = geminiApiKey.value() || process.env.GEMINI_API_KEY;
  if (!apiKey) {
    throw new HttpsError(
      "unavailable",
      "AI service is not configured on the server (GEMINI_API_KEY is unset)."
    );
  }

  const prompt = typeof request.data?.prompt === "string" ? request.data.prompt : null;
  if (!prompt) {
    throw new HttpsError("invalid-argument", "prompt is required.");
  }
  if (prompt.length > 20_000) {
    throw new HttpsError("invalid-argument", "prompt is too long.");
  }
  const systemInstruction =
    typeof request.data?.systemInstruction === "string" ? request.data.systemInstruction : null;

  enforceRateLimit(request.auth.uid);

  const body = {
    contents: [{ parts: [{ text: prompt }] }],
    ...(systemInstruction ? { systemInstruction: { parts: [{ text: systemInstruction }] } } : {}),
  };

  let lastError = null;
  for (const model of GEMINI_MODELS) {
    try {
      // The key goes in an Authorization header, not a query string, so it does not end up in
      // URL logs.
      const response = await fetch(`${GEMINI_BASE_URL}/${model}:generateContent`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": apiKey,
        },
        body: JSON.stringify(body),
      });

      if (!response.ok) {
        lastError = new Error(`HTTP ${response.status}`);
        logger.warn(`Gemini model ${model} returned ${response.status}`);
        continue;
      }

      const json = await response.json();
      const text = json?.candidates?.[0]?.content?.parts?.[0]?.text;
      if (text && text.trim()) {
        return { text, model };
      }
      lastError = new Error("empty response");
    } catch (err) {
      lastError = err;
      logger.warn(`Gemini model ${model} failed: ${err.message}`);
    }
  }

  logger.error(`All Gemini models failed: ${lastError?.message}`);
  throw new HttpsError("unavailable", "AI service is temporarily unavailable.");
});
