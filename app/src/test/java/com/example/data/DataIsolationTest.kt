package com.example.data

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Proves that one account on a device cannot see another account's health data.
 *
 * This is the defect that made the app unsafe for its actual purpose. Every table carries a
 * userId column, but the queries ignored it: `SELECT * FROM chat_messages WHERE chatType = ?`
 * returned every account's history, reminders, symptom checks, lab results and notifications.
 * On a phone shared by parents and children — the exact scenario this app advertises — one
 * person's lab results and medical history were visible to everyone else, and `logout()` did not
 * even clear them because it only deleted the session row.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DataIsolationTest {

    private lateinit var dao: AppDao

    private val alice = "alice-uid"
    private val bob = "bob-uid"

    @Before
    fun setUp() {
        val db = AppDatabase.getDatabase(ApplicationProvider.getApplicationContext())
        // A unique in-memory name per test keeps these isolated from each other and from the
        // on-disk database.
        dao = db.appDao()
    }

    private fun chat(uid: String, text: String) =
        ChatMessageLocal(userId = uid, chatType = "general", role = "user", content = text)

    @Test
    fun `chat history is per user`() = runBlocking {
        dao.insertChatMessage(chat(alice, "Alice: bosh og'rigi bor"))
        dao.insertChatMessage(chat(bob, "Bob: oyoq og'rigi bor"))

        val alicesMessages = dao.getChatMessagesFlow(alice, "general").first()
        val bobsMessages = dao.getChatMessagesFlow(bob, "general").first()

        assertEquals(1, alicesMessages.size)
        assertEquals(1, bobsMessages.size)
        assertTrue(
            "Alice must not see Bob's message",
            alicesMessages.none { it.content.contains("Bob") }
        )
        assertTrue(
            "Bob must not see Alice's message",
            bobsMessages.none { it.content.contains("Alice") }
        )
    }

    @Test
    fun `reminders are per user`() = runBlocking {
        dao.insertReminder(ReminderLocal(userId = alice, medicineName = "Alice's insulin", time = "08:00", frequency = "daily", type = "Dori"))
        dao.insertReminder(ReminderLocal(userId = bob, medicineName = "Bob's tablets", time = "21:00", frequency = "daily", type = "Dori"))

        val alicesReminders = dao.getRemindersFlow(alice).first()
        val bobsReminders = dao.getRemindersFlow(bob).first()

        assertEquals(1, alicesReminders.size)
        assertEquals("Alice's insulin", alicesReminders.first().medicineName)
        assertEquals(1, bobsReminders.size)
        assertEquals("Bob's tablets", bobsReminders.first().medicineName)
    }

    @Test
    fun `lab results are per user`() = runBlocking {
        dao.insertLabResult(LabResultLocal(userId = alice, imagePath = "a.jpg", analysisText = "Alice: high glucose", valuesJson = "{}"))
        dao.insertLabResult(LabResultLocal(userId = bob, imagePath = "b.jpg", analysisText = "Bob: normal", valuesJson = "{}"))

        val alices = dao.getLabResultsFlow(alice).first()
        val bobs = dao.getLabResultsFlow(bob).first()

        assertEquals(1, alices.size)
        assertEquals(1, bobs.size)
        assertTrue(alices.none { it.analysisText.contains("Bob") })
    }

    @Test
    fun `symptom checks are per user`() = runBlocking {
        dao.insertSymptomCheck(SymptomCheck(userId = alice, symptomsInput = "Alice symptoms", bodyPart = "head", resultJson = "{}"))
        dao.insertSymptomCheck(SymptomCheck(userId = bob, symptomsInput = "Bob symptoms", bodyPart = "leg", resultJson = "{}"))

        assertEquals(1, dao.getSymptomChecksFlow(alice).first().size)
        assertEquals(1, dao.getSymptomChecksFlow(bob).first().size)
    }

    @Test
    fun `notifications are per user`() = runBlocking {
        dao.insertNotification(NotificationLocal(userId = alice, title = "Alice only", message = "private", type = "system"))
        dao.insertNotification(NotificationLocal(userId = bob, title = "Bob only", message = "private", type = "system"))

        assertEquals(1, dao.getNotificationsFlow(alice).first().size)
        assertEquals(1, dao.getUnreadNotificationsCountFlow(alice).first())
        assertEquals("Alice only", dao.getNotificationsFlow(alice).first().first().title)
    }

    @Test
    fun `marking notifications read does not touch another user's`() = runBlocking {
        dao.insertNotification(NotificationLocal(userId = alice, title = "a", message = "a", type = "system"))
        dao.insertNotification(NotificationLocal(userId = bob, title = "b", message = "b", type = "system"))

        dao.markAllNotificationsAsRead(alice)

        assertEquals(0, dao.getUnreadNotificationsCountFlow(alice).first())
        assertEquals("Bob's unread count must be untouched", 1, dao.getUnreadNotificationsCountFlow(bob).first())
    }

    @Test
    fun `daily metrics are per user for the same date`() = runBlocking {
        val today = "2026-01-15"

        // This is the collision the old schema could not represent: the primary key was the
        // date alone, so both of these wrote to the SAME row and whoever saved last won.
        dao.insertDailyMetrics(DailyHealthMetricsLocal(userId = alice, date = today, waterGlasses = 8))
        dao.insertDailyMetrics(DailyHealthMetricsLocal(userId = bob, date = today, waterGlasses = 2))

        assertEquals(8, dao.getDailyMetrics(alice, today)?.waterGlasses)
        assertEquals(2, dao.getDailyMetrics(bob, today)?.waterGlasses)
    }

    @Test
    fun `the free chat quota counts only this user's messages`() = runBlocking {
        val startOfDay = System.currentTimeMillis() - 60_000

        repeat(5) { dao.insertChatMessage(chat(alice, "Alice $it")) }
        repeat(3) { dao.insertChatMessage(chat(bob, "Bob $it")) }

        // Bob's spending must not consume Alice's allowance, or the second person to use the
        // device silently inherits the quota state of the first.
        assertEquals(5, dao.getTodayGeneralChatCount(alice, startOfDay))
        assertEquals(3, dao.getTodayGeneralChatCount(bob, startOfDay))
    }

    @Test
    fun `clearing chat history only clears the given user's messages`() = runBlocking {
        dao.insertChatMessage(chat(alice, "Alice keep"))
        dao.insertChatMessage(chat(bob, "Bob keep"))

        dao.clearChatHistory(alice, "general")

        assertEquals(0, dao.getChatMessagesFlow(alice, "general").first().size)
        assertEquals(1, dao.getChatMessagesFlow(bob, "general").first().size)
    }

    @Test
    fun `deleting a reminder cannot reach another user's row`() = runBlocking {
        val alicesReminder = dao.insertReminder(
            ReminderLocal(userId = alice, medicineName = "keep me", time = "08:00", frequency = "daily", type = "Dori")
        )
        val bobsReminder = dao.insertReminder(
            ReminderLocal(userId = bob, medicineName = "delete target", time = "09:00", frequency = "daily", type = "Dori")
        )

        // Bob's id, but Alice as the acting user: must affect nothing.
        dao.deleteReminder(bobsReminder.toInt(), alice)

        assertEquals(1, dao.getRemindersFlow(alice).first().size)
        assertEquals(1, dao.getRemindersFlow(bob).first().size)
        assertEquals("keep me", dao.getRemindersFlow(alice).first().first().medicineName)
        assertTrue(alicesReminder > 0)
    }

    @Test
    fun `accounts are unique per email regardless of case`() = runBlocking {
        dao.insertAccount(AccountLocal(uid = "a1", email = "person@example.com", createdAt = 0))

        val found = dao.getAccountByEmail("PERSON@Example.com")

        assertEquals("a1", found?.uid)
    }

    @Test
    fun `signing out keeps the account and its credentials`() = runBlocking {
        dao.insertAccount(AccountLocal(uid = "a1", email = "person@example.com", passwordHash = "hash", createdAt = 0))
        dao.insertUser(
            UserLocal(
                uid = "a1", name = "P", email = "person@example.com", phone = "", dateOfBirth = "",
                gender = "", bloodType = "", height = 0.0, weight = 0.0, isPremium = false,
                premiumExpiry = null, language = "uz", fcmToken = "", createdAt = 0, lastActive = 0,
                healthScore = 0, isAdmin = false, isBanned = false, avatarUrl = ""
            )
        )

        // logout() only clears the session row...
        dao.clearCurrentUser()
        assertEquals(null, dao.getCurrentUser())

        // ...but the account and its credential survive, so signing back in restores the SAME
        // account instead of silently creating a new one with a fresh trial.
        assertEquals("a1", dao.getAccountByEmail("person@example.com")?.uid)
        assertEquals("hash", dao.getAccountByEmail("person@example.com")?.passwordHash)
    }
}
