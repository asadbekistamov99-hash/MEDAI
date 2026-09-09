package com.example.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class GoogleSignInResult(val name: String, val email: String)

/**
 * Real Google Sign-In via the Credential Manager API + Firebase Auth (replaces the previous
 * fake account-picker dialog). Requires two things the app owner must provide, which this
 * class cannot supply on its own:
 *  1. A real google-services.json (from Firebase console) dropped into app/.
 *  2. The OAuth Web Client ID pasted into R.string.google_web_client_id (see strings.xml) —
 *     Firebase console -> Authentication -> Sign-in method -> Google -> Web SDK configuration.
 * Until both are set, signIn() below fails gracefully (returns a Result.failure) rather than
 * crashing, so the rest of the app keeps working without Google Sign-In configured.
 */
object GoogleAuthHelper {

    suspend fun signIn(context: Context): Result<GoogleSignInResult> {
        return try {
            val webClientId = context.getString(R.string.google_web_client_id)
            if (webClientId.isBlank() || webClientId == "REPLACE_WITH_YOUR_FIREBASE_WEB_CLIENT_ID") {
                return Result.failure(
                    IllegalStateException("Google Sign-In hali sozlanmagan: R.string.google_web_client_id qiymatini Firebase konsolidan qo'ying.")
                )
            }

            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .build()
            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                val firebaseUser = signInToFirebase(firebaseCredential)
                    ?: return Result.failure(IllegalStateException("Firebase orqali tizimga kirib bo'lmadi"))

                val name = googleIdTokenCredential.displayName
                    ?: firebaseUser.displayName
                    ?: firebaseUser.email?.substringBefore("@")
                    ?: "Foydalanuvchi"
                val email = firebaseUser.email ?: googleIdTokenCredential.id

                Result.success(GoogleSignInResult(name = name, email = email))
            } else {
                Result.failure(IllegalStateException("Kutilmagan hisob ma'lumoti turi qaytdi"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun signInToFirebase(credential: AuthCredential): FirebaseUser? =
        suspendCancellableCoroutine { continuation ->
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener { result -> continuation.resume(result.user) }
                .addOnFailureListener { e -> continuation.resumeWithException(e) }
        }
}
