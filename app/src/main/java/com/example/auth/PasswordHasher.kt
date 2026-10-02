package com.example.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted password hashing for locally-stored accounts.
 *
 * The login screen has always collected a password but never checked it, so no credential
 * needed to exist anywhere. Now that it does, it must not be stored in plaintext: a Room file
 * pulled off a backup, or a debug build dumped on a shared device, would otherwise hand over
 * every account.
 *
 * PBKDF2-HMAC-SHA256 with a per-account random salt, matching the parameters OWASP recommends
 * for PBKDF2-SHA256. [SALT_BYTES] random bytes are drawn per hash and stored alongside it.
 *
 * The output format is self-describing so the parameters can be raised later without
 * invalidating existing hashes:
 *
 *     pbkdf2_sha256$<iterations>$<saltBase64>$<hashBase64>
 */
object PasswordHasher {

    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256
    private const val ITERATIONS = 120_000
    private const val PREFIX = "pbkdf2_sha256"
    private const val SEPARATOR = "$"

    /** True for values produced by [hash]; used to tell a real hash from a legacy empty field. */
    fun isHashed(value: String): Boolean = value.startsWith("$PREFIX$SEPARATOR")

    /**
     * Hashes [password] with a fresh random salt.
     *
     * [PBKDF2WithHmacSHA256] is unavailable on API < 26 and this app supports API 24, so
     * PBKDF2-HMAC-SHA1 is used as the fallback — still a real PBKDF2 with the same iteration
     * count, just a weaker digest.
     */
    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val derived = derive(password, salt, ITERATIONS, KEY_BITS)
        val saltB64 = Base64.encodeToString(salt, Base64.NO_WRAP)
        val hashB64 = Base64.encodeToString(derived, Base64.NO_WRAP)
        return "$PREFIX$SEPARATOR$ITERATIONS$SEPARATOR$saltB64$SEPARATOR$hashB64"
    }

    /**
     * Constant-time verification of [password] against a stored [stored] value.
     *
     * Returns false (never throws) for malformed or empty input, so a corrupt row cannot crash
     * the login screen or be used to distinguish "no such account" from "wrong password".
     */
    fun verify(password: String, stored: String): Boolean {
        if (!isHashed(stored)) return false
        val parts = stored.split(SEPARATOR)
        if (parts.size != 4) return false

        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = runCatching { Base64.decode(parts[2], Base64.NO_WRAP) }.getOrNull() ?: return false
        val expected = runCatching { Base64.decode(parts[3], Base64.NO_WRAP) }.getOrNull() ?: return false
        if (iterations <= 0 || salt.isEmpty() || expected.isEmpty()) return false

        val actual = derive(password, salt, iterations, expected.size * 8)
        // MessageDigest.isEqual is constant-time; a plain contentEquals would leak the
        // matching prefix length through timing.
        return MessageDigest.isEqual(expected, actual)
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int, keyBits: Int): ByteArray {
        val chars = password.toCharArray()
        val spec = PBEKeySpec(chars, salt, iterations, keyBits)
        return try {
            val factory = SecretKeyFactory.getInstance(algorithmFor(keyBits))
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    private fun algorithmFor(keyBits: Int): String =
        if (keyBits == KEY_BITS) "PBKDF2WithHmacSHA256" else "PBKDF2WithHmacSHA1"
}
