package com.example.catalog

import com.example.data.*
import com.example.ui.AppViewModel
import com.example.ui.SUPER_ADMIN_EMAIL
import kotlinx.coroutines.runBlocking
import org.robolectric.shadows.ShadowLooper

/** Seeded view model whose signed-in user is the super admin, with populated Admin lists. Test-only. */
object AdminTestData {
  fun adminVm(lang: String): AppViewModel {
    val vm = SampleData.seededVm(lang, admin = true)
    val now = System.currentTimeMillis()
    val day = 86_400_000L
    runBlocking {
      val dao = vm.daoForTest
      dao.getCurrentUser()?.let { dao.insertUser(it.copy(email = SUPER_ADMIN_EMAIL)) }
      listOf(
        UserSystem("u1", "Aziz Karimov", "aziz@example.com", "+998901234567", "1995-04-12", "male", "O+", 178.0, 74.0, true, now + 20 * day, 0, 0, lang, "", now - 90 * day, now - 2 * 3_600_000L, 74, false, false, "", "Home"),
        UserSystem("u2", "Malika Rustamova", "malika@example.com", "+998909876543", "1992-01-30", "female", "A+", 165.0, 58.0, false, null, 0, 0, lang, "", now - 60 * day, now - 40 * day, 68, false, false, "", "Analytics"),
        UserSystem("u3", "Jasur Toshmatov", "jasur@example.com", "+998933332211", "1988-07-02", "male", "B+", 181.0, 90.0, false, null, 0, 0, lang, "", now - 30 * day, now - 5 * day, 55, false, true, "", "Profile"),
      ).forEach { dao.insertSystemUser(it) }
      dao.insertPaymentRequest(PaymentRequestLocal("p1", "u2", "Malika Rustamova", "malika@example.com", "+998909876543", "", "pending", "", now - 3_600_000L, null, null))
      dao.insertPaymentRequest(PaymentRequestLocal("p2", "u1", "Aziz Karimov", "aziz@example.com", "+998901234567", "", "approved", "", now - 3 * day, now - 2 * day, SUPER_ADMIN_EMAIL))
      dao.insertPaymentRequest(PaymentRequestLocal("p3", "u3", "Jasur Toshmatov", "jasur@example.com", "+998933332211", "", "rejected", "Chek tushunarsiz", now - 5 * day, now - 4 * day, SUPER_ADMIN_EMAIL))
      dao.insertDisease(DiseaseEntry(nameUz = "Gripp", nameRu = "Gripp", nameEn = "Flu", symptomsUz = "Isitma, yo'tal, holsizlik", descriptionUz = "", descriptionRu = "", descriptionEn = "", specialistType = "Terapevt", severity = "moderate"))
      dao.insertDisease(DiseaseEntry(nameUz = "Gipertoniya", nameRu = "", nameEn = "", symptomsUz = "Bosh og'rig'i, bosim ortishi", descriptionUz = "", descriptionRu = "", descriptionEn = "", specialistType = "Kardiolog", severity = "severe"))
      dao.insertMedicine(MedicineEntry(name = "Paratsetamol", description = "Isitma va og'riq qoldiruvchi vosita", dosage = "500 mg", sideEffects = "", category = "Og'riq qoldiruvchi"))
      dao.insertMedicine(MedicineEntry(name = "Ibuprofen", description = "Yallig'lanishga qarshi", dosage = "200 mg", sideEffects = "", category = "NSAID"))
      dao.insertHealthTip(HealthTipLocal(uz = "Kuniga 8 stakan suv iching.", ru = "Пейте 8 стаканов воды в день.", en = "Drink 8 glasses of water a day.", orderIndex = 0))
      dao.insertAdminLog(AdminLog(adminEmail = SUPER_ADMIN_EMAIL, action = "Premium Extended", targetUser = "Aziz Karimov", details = "30 kunga premium uzaytirildi.", timestamp = now - 3_600_000L))
      dao.insertAdminLog(AdminLog(adminEmail = SUPER_ADMIN_EMAIL, action = "User Blocked", targetUser = "Jasur Toshmatov", details = "Sabab: Qoidalarni buzganligi sababli", timestamp = now - day))
      dao.insertErrorLog(ErrorLog(userId = "u1", screen = "Analytics", errorMessage = "NullPointerException: vitals list is null", appVersion = "1.4.2", deviceInfo = "Pixel 7 / Android 15"))
      dao.insertErrorLog(ErrorLog(userId = "u2", screen = "Lab", errorMessage = "Timeout while uploading image", appVersion = "1.4.1", deviceInfo = "Samsung A54 / Android 14", isResolved = true))
    }
    repeat(30) { ShadowLooper.idleMainLooper(); Thread.sleep(25) }
    return vm
  }
}
