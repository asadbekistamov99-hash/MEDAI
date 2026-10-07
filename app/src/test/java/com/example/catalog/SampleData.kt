package com.example.catalog

import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import com.example.ui.AppViewModel
import kotlinx.coroutines.runBlocking
import org.robolectric.shadows.ShadowLooper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Realistic sample data for screenshot and layout tests (Robolectric only). */
object SampleData {
  const val uid = "demo-uid"

  fun seededVm(lang: String = "uz", admin: Boolean = false): AppViewModel {
    // Seeded reminders make the view model schedule WorkManager jobs; without an initialised
    // WorkManager that throws on the main dispatcher and fails every following test.
    try {
      androidx.work.WorkManager.initialize(
        ApplicationProvider.getApplicationContext(),
        androidx.work.Configuration.Builder().setExecutor(java.util.concurrent.Executors.newSingleThreadExecutor()).build(),
      )
    } catch (e: IllegalStateException) { /* already initialised by an earlier test */ }
    val vm = AppViewModel(ApplicationProvider.getApplicationContext())
    val now = System.currentTimeMillis()
    val day = 86_400_000L
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    runBlocking {
      val dao = vm.daoForTest
      dao.insertUser(UserLocal(uid = uid, name = "Aziz Karimov", email = if (admin) "admin@example.com" else "aziz@example.com", phone = "+998901234567",
        dateOfBirth = "1995-04-12", gender = "male", bloodType = "O+", height = 178.0, weight = 74.0, isPremium = false, premiumExpiry = null,
        trialStartedAt = now, trialEndsAt = now + 5 * day, language = lang, fcmToken = "", createdAt = now, lastActive = now, healthScore = 74,
        isAdmin = admin, isBanned = false, avatarUrl = "",
        allergiesJson = "[{\"name\":\"Penitsillin\",\"type\":\"Dori\",\"addedAt\":0},{\"name\":\"Yeryong'oq\",\"type\":\"Taom\",\"addedAt\":0}]",
        unlockedAchievementsJson = "[\"beginner\",\"water\"]"))
      dao.insertDailyMetrics(DailyHealthMetricsLocal(userId = uid, date = today, waterGlasses = 5, waterGoal = 8, weight = 74.0,
        bpSystolic = 118, bpDiastolic = 76, heartRate = 68, sleepHours = 7.3, steps = 4230))
      listOf("Boshim og'riyapti va holsizlik his qilyapman" to "Bosh", "Tomoqda og'riq, yo'tal 2 kundan beri" to "Tomoq", "Qorin og'rig'i, ko'ngil aynish" to "Qorin").forEachIndexed { i, (t, p) ->
        dao.insertSymptomCheck(SymptomCheck(userId = uid, symptomsInput = t, bodyPart = p,
          resultJson = "Ehtimoliy sabablar: charchoq, suvsizlanish, shamollash alomatlari. Ko'proq suyuqlik iching, dam oling. Holat 48 soatda yaxshilanmasa, shifokorga murojaat qiling.",
          timestamp = now - (i + 1) * day))
      }
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "user", content = "Boshim og'riyapti, nima qilay?", timestamp = now - 4000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "model", content = "Ko'proq suv iching va dam oling. Og'riq davom etsa, shifokorga murojaat qiling.", timestamp = now - 3000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "user", content = "Qancha suv ichishim kerak?", timestamp = now - 2000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "general", role = "model", content = "Kuniga o'rtacha 1,5–2 litr, ya'ni taxminan 8 stakan.", timestamp = now - 1000))
      dao.insertChatMessage(ChatMessageLocal(userId = uid, chatType = "doctor", role = "model", content = "Salom! Men MedAI shifokoriman. Sizni nima bezovta qilyapti?", timestamp = now - 500))
      dao.insertReminder(ReminderLocal(userId = uid, medicineName = "Vitamin D", time = "08:00", frequency = "daily", type = "Dori", notes = "Ovqatdan keyin"))
      dao.insertReminder(ReminderLocal(userId = uid, medicineName = "Suv ichish", time = "12:30", frequency = "daily", type = "Suv"))
      dao.insertReminder(ReminderLocal(userId = uid, medicineName = "Terapevt qabuli", time = "17:00", frequency = "weekly", type = "Doctor"))
      listOf(Triple("m1", "Malika Karimova", "Spouse"), Triple("m2", "Jasur Karimov", "Child"), Triple("m3", "Soliha Karimova", "Mother")).forEachIndexed { i, (id, name, rel) ->
        dao.insertFamilyMember(FamilyMemberLocal(uid = id, ownerUserId = uid, name = name, email = "$id@example.com", phone = "+99890000000$i", relation = rel,
          avatarUrl = "", healthScore = 70 + i * 8, stepsToday = 3000 + i * 1500, lastActive = now - i * 3_600_000L, activeRemindersCount = i,
          sosStatus = false, inviteStatus = if (i == 2) "pending" else "accepted", isInvitedByMe = true))
      }
      dao.insertNotification(NotificationLocal(userId = uid, title = "Dori vaqti", message = "Vitamin D qabul qilish vaqti keldi.", type = "reminder", timestamp = now - 600_000))
      dao.insertNotification(NotificationLocal(userId = uid, title = "7 kunlik bepul Premium sinov", message = "Barcha imkoniyatlar 5 kun davomida ochiq.", type = "premium", isRead = true, timestamp = now - day))
      dao.insertNotification(NotificationLocal(userId = uid, title = "Oila taklifi", message = "Soliha Karimova sizni oilaga qo'shdi.", type = "invite", isRead = true, timestamp = now - 2 * day))
      dao.insertLabResult(LabResultLocal(userId = uid, imagePath = "", analysisText = "Gemoglobin: 138 g/L (norma). Leykotsitlar: 6,2 (norma). Xolesterin: 5,6 mmol/L (biroz yuqori).", valuesJson = "[]", timestamp = now - 3 * day))
    }
    repeat(100) { if (vm.currentUser.value != null) return@repeat; ShadowLooper.idleMainLooper(); Thread.sleep(50) }
    vm.setLanguage(lang)
    repeat(30) { ShadowLooper.idleMainLooper(); Thread.sleep(25) }
    return vm
  }
}
