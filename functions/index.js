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
