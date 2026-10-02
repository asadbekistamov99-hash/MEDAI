package com.example.ui

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserLocal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/**
 * Regression guard for the admin-gating logic that used to be duplicated as a raw email
 * string comparison in ~20 places across the codebase (see AppViewModel.SUPER_ADMIN_EMAIL /
 * isSuperAdmin, and AdminScreen.kt / SettingsAndFamilyScreens.kt which now read
 * viewModel.isSuperAdmin instead of re-deriving it). This exercises the real Room-backed
 * currentUser flow rather than testing the trivial comparison in isolation, so a future change
 * that reintroduces a separate, drifted check would show up here as a real behavior mismatch.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppViewModelAdminTest {

    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        viewModel = AppViewModel(ApplicationProvider.getApplicationContext<Application>())
        // Let AppViewModel's init{} (default-user seeding, Firestore listener setup wrapped in
        // its own try/catch, etc.) finish running on the Robolectric main looper before each test.
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `isSuperAdmin is true only for the configured admin email`() = runBlocking {
        val seedUid = viewModel.dao.getCurrentUser()?.uid ?: "test-uid"

        viewModel.dao.insertUser(testUser(uid = seedUid, email = SUPER_ADMIN_EMAIL))
        awaitEmail(SUPER_ADMIN_EMAIL)
        assertTrue(viewModel.isSuperAdmin)

        viewModel.dao.insertUser(testUser(uid = seedUid, email = "someone.else@gmail.com"))
        awaitEmail("someone.else@gmail.com")
        assertFalse(viewModel.isSuperAdmin)
    }

    @Test
    fun `isSuperAdmin matching ignores case and surrounding whitespace`() = runBlocking {
        val seedUid = viewModel.dao.getCurrentUser()?.uid ?: "test-uid"

        val padded = "  ${SUPER_ADMIN_EMAIL.uppercase()}  "
        viewModel.dao.insertUser(testUser(uid = seedUid, email = padded))
        awaitEmail(padded)
        assertTrue(viewModel.isSuperAdmin)
    }

    @Test
    fun `isSuperAdmin is false when there is no signed-in user`() = runBlocking {
        viewModel.dao.clearCurrentUser()
        awaitEmail(null)
        assertFalse(viewModel.isSuperAdmin)
    }

    // currentUser is a Room-backed StateFlow updated on another dispatcher, so poll briefly.
    private fun awaitEmail(expected: String?) {
        val deadline = System.currentTimeMillis() + 5000
        while (System.currentTimeMillis() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            if (viewModel.currentUser.value?.email == expected) return
            Thread.sleep(25)
        }
    }

    private fun testUser(uid: String, email: String) = UserLocal(
        uid = uid,
        name = "Test User",
        email = email,
        phone = "+998900000000",
        dateOfBirth = "2000-01-01",
        gender = "male",
        bloodType = "O+",
        height = 170.0,
        weight = 70.0,
        isPremium = false,
        premiumExpiry = null,
        language = "uz",
        fcmToken = "test-token",
        createdAt = System.currentTimeMillis(),
        lastActive = System.currentTimeMillis(),
        healthScore = 50,
        isAdmin = false,
        isBanned = false,
        avatarUrl = ""
    )
}
