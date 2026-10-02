package com.example.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Password hashing is the only thing standing between an attacker with a copy of the app's
 * database and every account in it, and the login screen had no real credential to store at all
 * before this existed. These cover the properties that matter: correctness, salting, and not
 * throwing on malformed input.
 */
class PasswordHasherTest {

    @Test
    fun `a correct password verifies`() {
        val stored = PasswordHasher.hash("correct horse battery")

        assertTrue(PasswordHasher.verify("correct horse battery", stored))
    }

    @Test
    fun `a wrong password does not verify`() {
        val stored = PasswordHasher.hash("correct horse battery")

        assertFalse(PasswordHasher.verify("correct horse batterY", stored))
        assertFalse(PasswordHasher.verify("", stored))
        assertFalse(PasswordHasher.verify("a much longer password entirely", stored))
    }

    @Test
    fun `the same password hashes differently each time`() {
        // Without a random per-hash salt, two users with the same password would have
        // identical rows, and a single rainbow table would unlock every one of them.
        val first = PasswordHasher.hash("same-password")
        val second = PasswordHasher.hash("same-password")

        assertNotEquals(first, second)
        // Both must still verify — different salt, same password.
        assertTrue(PasswordHasher.verify("same-password", first))
        assertTrue(PasswordHasher.verify("same-password", second))
    }

    @Test
    fun `the plaintext password is not present in the hash`() {
        val stored = PasswordHasher.hash("hunter2")

        assertFalse(stored.contains("hunter2"))
    }

    @Test
    fun `verification fails safely on empty and malformed stored values`() {
        // A corrupt or legacy row must not crash the login screen, and must never verify.
        assertFalse(PasswordHasher.verify("anything", ""))
        assertFalse(PasswordHasher.verify("anything", "not-a-hash"))
        assertFalse(PasswordHasher.verify("anything", "pbkdf2_sha256"))
        assertFalse(PasswordHasher.verify("anything", "pbkdf2_sha256\$abc\$xx\$yy"))
        assertFalse(PasswordHasher.verify("anything", "pbkdf2_sha256\$1000\$\$\$"))
    }

    @Test
    fun `the stored format is recognisable`() {
        val stored = PasswordHasher.hash("password")

        assertTrue(PasswordHasher.isHashed(stored))
        assertFalse(PasswordHasher.isHashed(""))
        assertFalse(PasswordHasher.isHashed("legacy-plaintext"))
    }

    @Test
    fun `unicode passwords work`() {
        val stored = PasswordHasher.hash("Пароль🔐")

        assertTrue(PasswordHasher.verify("Пароль🔐", stored))
        assertFalse(PasswordHasher.verify("Пароль", stored))
    }
}
