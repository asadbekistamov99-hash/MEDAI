package com.example.ui

import android.app.Application
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

/** New accounts must be free-tier, non-admin and use the supplied (Firebase) uid. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppViewModelAuthTest {

    private lateinit var viewModel: AppViewModel

    @Before
    fun setup() {
        viewModel = AppViewModel(ApplicationProvider.getApplicationContext<Application>())
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `no demo user or demo premium is created on first launch`() = runBlocking {
        viewModel.dao.clearCurrentUser()
        shadowOf(Looper.getMainLooper()).idle()
        assertNull(viewModel.dao.getCurrentUser())
    }

    @Test
    fun `new regular account is free tier and not admin`() = runBlocking {
        viewModel.dao.clearCurrentUser()
        viewModel.registerUserSuspend(
            name = "Test", email = "regular@example.com", phone = "", dob = "", gender = "male",
            bloodType = "", height = 0.0, weight = 0.0, uid = "firebase-uid-1"
        )
        val user = viewModel.dao.getCurrentUser()!!
        assertEquals("firebase-uid-1", user.uid)
        assertFalse(user.isPremium)
        assertNull(user.premiumExpiry)
        assertFalse(user.isAdmin)
    }

    @Test
    fun `admin email gets the admin hint but still no premium`() = runBlocking {
        viewModel.dao.clearCurrentUser()
        viewModel.registerUserSuspend(
            name = "Admin", email = SUPER_ADMIN_EMAIL, phone = "", dob = "", gender = "male",
            bloodType = "", height = 0.0, weight = 0.0
        )
        val user = viewModel.dao.getCurrentUser()!!
        assertTrue(user.isAdmin)
        assertFalse(user.isPremium)
    }
}
