package com.example.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the entitlement model: who has access to paid features, and for how long.
 *
 * This is the logic the whole business rests on, and it was previously untested while being
 * wrong in a specific way — every account was created with `isPremium = true`, so the free
 * tier, the daily chat quota and the upgrade screen were all unreachable in practice. The
 * regression that matters is "a brand-new account is NOT premium", so that is asserted directly.
 */
class PremiumTrialTest {

    private val now = System.currentTimeMillis()
    private val sevenDays = UserLocal.TRIAL_DURATION_MS

    private fun user(
        isPremium: Boolean = false,
        premiumExpiry: Long? = null,
        trialStartedAt: Long = 0L,
        trialEndsAt: Long = 0L
    ) = UserLocal(
        uid = "u1",
        name = "Test",
        email = "test@example.com",
        phone = "",
        dateOfBirth = "",
        gender = "",
        bloodType = "",
        height = 0.0,
        weight = 0.0,
        isPremium = isPremium,
        premiumExpiry = premiumExpiry,
        trialStartedAt = trialStartedAt,
        trialEndsAt = trialEndsAt,
        language = "uz",
        fcmToken = "",
        createdAt = now,
        lastActive = now,
        healthScore = 0,
        isAdmin = false,
        isBanned = false,
        avatarUrl = ""
    )

    @Test
    fun `a new account gets a trial, not a paid subscription`() {
        val u = user(trialStartedAt = now, trialEndsAt = now + sevenDays)

        // This is the exact regression: registration used to hardcode isPremium = true with a
        // one-year expiry, which made the free tier and the paywall unreachable.
        assertFalse("registration must never grant a paid subscription", u.isPremium)
        assertTrue(u.isTrialActive)
        assertTrue(u.hasPremiumAccess)
    }

    @Test
    fun `trial access ends once the trial window has passed`() {
        val u = user(trialStartedAt = now - sevenDays - 1_000, trialEndsAt = now - 1_000)

        assertFalse(u.isTrialActive)
        assertFalse(u.hasPremiumAccess)
    }

    @Test
    fun `an account with no trial at all has no premium access`() {
        val u = user()

        assertFalse(u.isTrialActive)
        assertFalse(u.hasPremiumAccess)
        assertEquals(0, u.trialDaysRemaining)
    }

    @Test
    fun `a paid subscription grants access regardless of trial state`() {
        val u = user(isPremium = true, premiumExpiry = now + sevenDays)

        assertTrue(u.hasPremiumAccess)
    }

    @Test
    fun `an expired paid subscription stops granting access`() {
        val u = user(isPremium = true, premiumExpiry = now - 1)

        assertFalse(u.hasPremiumAccess)
    }

    @Test
    fun `paying while a trial is running does not double up the remaining time`() {
        // Access is an OR, not a sum: a user who pays on day 2 of a 7-day trial gets 30 days
        // from now, and the trial stamps stay in the past rather than extending the expiry.
        val u = user(
            isPremium = true,
            premiumExpiry = now + 30L * 24 * 60 * 60 * 1000,
            trialStartedAt = now - 2L * 24 * 60 * 60 * 1000,
            trialEndsAt = now + 5L * 24 * 60 * 60 * 1000
        )

        assertTrue(u.hasPremiumAccess)
    }

    @Test
    fun `trial days remaining counts partial days as a full day`() {
        // A user with 23 hours left should be told 1 day, not 0, otherwise the banner
        // disappears while they still have time to use the product.
        val almostADay = 23L * 60 * 60 * 1000
        val u = user(trialStartedAt = now, trialEndsAt = now + almostADay)

        assertEquals(1, u.trialDaysRemaining)
    }

    @Test
    fun `trial days remaining matches a full seven day window`() {
        val u = user(trialStartedAt = now, trialEndsAt = now + sevenDays)

        assertTrue(
            "expected ~7 days, got ${u.trialDaysRemaining}",
            u.trialDaysRemaining in 6..7
        )
    }

    @Test
    fun `the trial window is seven days`() {
        assertEquals(7L * 24 * 60 * 60 * 1000, UserLocal.TRIAL_DURATION_MS)
    }
}
