package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Current User ---
    @Query("SELECT * FROM current_user LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserLocal?>

    @Query("SELECT * FROM current_user LIMIT 1")
    suspend fun getCurrentUser(): UserLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserLocal)

    @Query("DELETE FROM current_user")
    suspend fun clearCurrentUser()

    // --- Accounts (credentials, outlive the session) ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountLocal)

    @Query("SELECT * FROM accounts WHERE email = :email COLLATE NOCASE LIMIT 1")
    suspend fun getAccountByEmail(email: String): AccountLocal?

    @Query("SELECT * FROM accounts WHERE uid = :uid LIMIT 1")
    suspend fun getAccountProfile(uid: String): AccountLocal?

    // --- Symptom Checks ---
    @Query("SELECT * FROM symptom_checks WHERE userId = :userId ORDER BY timestamp DESC")
    fun getSymptomChecksFlow(userId: String): Flow<List<SymptomCheck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomCheck(check: SymptomCheck)

    @Query("DELETE FROM symptom_checks WHERE id = :id AND userId = :userId")
    suspend fun deleteSymptomCheck(id: Int, userId: String)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND chatType = :chatType ORDER BY timestamp ASC")
    fun getChatMessagesFlow(userId: String, chatType: String): Flow<List<ChatMessageLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageLocal)

    @Query("DELETE FROM chat_messages WHERE userId = :userId AND chatType = :chatType")
    suspend fun clearChatHistory(userId: String, chatType: String)

    // Counts only THIS user's messages. Without the userId filter a second person on the device
    // inherits the first person's spent quota and gets locked out of the free tier.
    @Query("SELECT COUNT(*) FROM chat_messages WHERE userId = :userId AND chatType = 'general' AND role = 'user' AND timestamp >= :todayStart")
    suspend fun getTodayGeneralChatCount(userId: String, todayStart: Long): Int

    // --- Reminders ---
    @Query("SELECT * FROM reminders WHERE userId = :userId ORDER BY timestamp DESC")
    fun getRemindersFlow(userId: String): Flow<List<ReminderLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderLocal): Long

    @Query("DELETE FROM reminders WHERE id = :id AND userId = :userId")
    suspend fun deleteReminder(id: Int, userId: String)

    // --- Lab Results ---
    // Lab reports are the most sensitive data in the app (scanned blood tests, images), so
    // these must never be visible to another account on the same device.
    @Query("SELECT * FROM lab_results WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLabResultsFlow(userId: String): Flow<List<LabResultLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabResult(result: LabResultLocal)

    @Query("DELETE FROM lab_results WHERE id = :id AND userId = :userId")
    suspend fun deleteLabResult(id: Int, userId: String)

    // --- Payment Requests ---
    @Query("SELECT * FROM payment_requests ORDER BY submittedAt DESC")
    fun getAllPaymentRequestsFlow(): Flow<List<PaymentRequestLocal>>

    @Query("SELECT * FROM payment_requests WHERE id = :id LIMIT 1")
    suspend fun getPaymentRequestById(id: String): PaymentRequestLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentRequest(request: PaymentRequestLocal)

    @Query("DELETE FROM payment_requests WHERE id = :id AND userId = :userId")
    suspend fun deletePaymentRequest(id: String, userId: String)

    // --- Family Members ---
    @Query("SELECT * FROM family_members WHERE ownerUserId = :userId")
    fun getFamilyMembersFlow(userId: String): Flow<List<FamilyMemberLocal>>

    @Query("SELECT * FROM family_members WHERE uid = :uid LIMIT 1")
    suspend fun getFamilyMember(uid: String): FamilyMemberLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberLocal)

    @Query("DELETE FROM family_members WHERE uid = :uid AND ownerUserId = :userId")
    suspend fun deleteFamilyMember(uid: String, userId: String)

    // --- Immunization Records ---
    // Keyed by the family member's uid, so scoping by the signed-in owner keeps one account from
    // reading another account's children's vaccination history.
    @Query("SELECT * FROM immunization_records WHERE ownerUserId = :userId ORDER BY timestamp DESC")
    fun getImmunizationRecordsFlow(userId: String): Flow<List<ImmunizationRecordLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImmunizationRecord(record: ImmunizationRecordLocal)

    @Query("DELETE FROM immunization_records WHERE id = :id AND ownerUserId = :userId")
    suspend fun deleteImmunizationRecord(id: Int, userId: String)

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")
    fun getNotificationsFlow(userId: String): Flow<List<NotificationLocal>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadNotificationsCountFlow(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLocal)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    // --- App Config ---
    @Query("SELECT * FROM app_config WHERE id = 'global' LIMIT 1")
    fun getAppConfigFlow(): Flow<AppConfigLocal?>

    @Query("SELECT * FROM app_config WHERE id = 'global' LIMIT 1")
    suspend fun getAppConfig(): AppConfigLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(config: AppConfigLocal)

    // --- Daily Health Metrics ---
    @Query("SELECT * FROM daily_health_metrics WHERE userId = :userId AND date = :date LIMIT 1")
    fun getDailyMetricsFlow(userId: String, date: String): Flow<DailyHealthMetricsLocal?>

    @Query("SELECT * FROM daily_health_metrics WHERE userId = :userId ORDER BY date DESC")
    fun getDailyMetricsFlow(userId: String): Flow<List<DailyHealthMetricsLocal>>

    @Query("SELECT * FROM daily_health_metrics WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getDailyMetrics(userId: String, date: String): DailyHealthMetricsLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyMetrics(metrics: DailyHealthMetricsLocal)

    // --- Medical Documents ---
    @Query("SELECT * FROM medical_documents WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMedicalDocumentsFlow(userId: String): Flow<List<MedicalDocumentLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalDocument(doc: MedicalDocumentLocal)

    @Query("DELETE FROM medical_documents WHERE id = :id AND userId = :userId")
    suspend fun deleteMedicalDocument(id: Int, userId: String)

    // --- Prescription Scans ---
    @Query("SELECT * FROM prescription_scans WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllPrescriptionScansFlow(userId: String): Flow<List<PrescriptionScanLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptionScan(scan: PrescriptionScanLocal)

    // --- Appointment Requests ---
    // AppointmentRequestLocal has no userId column of its own, so ownership is derived from the
    // patient phone the request was submitted with.
    @Query("SELECT * FROM appointment_requests WHERE patientPhone = :patientPhone ORDER BY timestamp DESC")
    fun getAppointmentRequestsFlow(patientPhone: String): Flow<List<AppointmentRequestLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointmentRequest(req: AppointmentRequestLocal)

    @Query("DELETE FROM appointment_requests WHERE id = :id AND patientPhone = :patientPhone")
    suspend fun deleteAppointmentRequest(id: Int, patientPhone: String)

    // --- Health Tips ---
    @Query("SELECT * FROM health_tips ORDER BY orderIndex ASC, timestamp DESC")
    fun getAllHealthTipsFlow(): Flow<List<HealthTipLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthTip(tip: HealthTipLocal)

    @Query("DELETE FROM health_tips WHERE id = :id")
    suspend fun deleteHealthTip(id: Int)

    // --- System Users ---
    @Query("SELECT * FROM system_users ORDER BY lastActive DESC")
    fun getAllSystemUsersFlow(): Flow<List<UserSystem>>

    @Query("SELECT * FROM system_users")
    suspend fun getAllSystemUsers(): List<UserSystem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemUser(user: UserSystem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSystemUsers(users: List<UserSystem>)

    @Query("UPDATE system_users SET isBanned = :isBanned WHERE uid = :uid")
    suspend fun updateSystemUserBanned(uid: String, isBanned: Boolean)

    @Query("UPDATE system_users SET isPremium = :isPremium, premiumExpiry = :expiry WHERE uid = :uid")
    suspend fun updateSystemUserPremium(uid: String, isPremium: Boolean, expiry: Long?)

    // --- User Activity Logs ---
    @Query("SELECT * FROM user_activities ORDER BY timestamp DESC")
    fun getAllUserActivitiesFlow(): Flow<List<UserActivityLog>>

    @Query("SELECT * FROM user_activities WHERE userId = :userId ORDER BY timestamp DESC")
    fun getUserActivitiesFlow(userId: String): Flow<List<UserActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserActivity(activity: UserActivityLog)

    // --- Error Logs ---
    @Query("SELECT * FROM error_logs ORDER BY timestamp DESC")
    fun getAllErrorLogsFlow(): Flow<List<ErrorLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertErrorLog(log: ErrorLog)

    @Query("UPDATE error_logs SET isResolved = :isResolved WHERE id = :id")
    suspend fun updateErrorLogResolved(id: Int, isResolved: Boolean)

    // --- API Usage Logs ---
    @Query("SELECT * FROM api_usage_logs ORDER BY timestamp DESC")
    fun getAllApiUsageLogsFlow(): Flow<List<ApiUsageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiUsageLog(log: ApiUsageLog)

    // --- Popup Banner ---
    @Query("SELECT * FROM popup_banner_config WHERE id = 'global_banner' LIMIT 1")
    fun getPopupBannerConfigFlow(): Flow<PopupBannerConfig?>

    @Query("SELECT * FROM popup_banner_config WHERE id = 'global_banner' LIMIT 1")
    suspend fun getPopupBannerConfig(): PopupBannerConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPopupBannerConfig(config: PopupBannerConfig)

    // --- Announcement ---
    @Query("SELECT * FROM announcement_config WHERE id = 'global_announcement' LIMIT 1")
    fun getAnnouncementConfigFlow(): Flow<AnnouncementConfig?>

    @Query("SELECT * FROM announcement_config WHERE id = 'global_announcement' LIMIT 1")
    suspend fun getAnnouncementConfig(): AnnouncementConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncementConfig(config: AnnouncementConfig)

    // --- Disease DB ---
    @Query("SELECT * FROM diseases_database ORDER BY nameUz ASC")
    fun getAllDiseasesFlow(): Flow<List<DiseaseEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDisease(disease: DiseaseEntry)

    @Query("DELETE FROM diseases_database WHERE id = :id")
    suspend fun deleteDisease(id: Int)

    // --- Medicine DB ---
    @Query("SELECT * FROM medicines_database ORDER BY name ASC")
    fun getAllMedicinesFlow(): Flow<List<MedicineEntry>>

    @Query("SELECT * FROM medicines_database WHERE name LIKE :query LIMIT 1")
    suspend fun findMedicine(query: String): MedicineEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(med: MedicineEntry)

    @Query("DELETE FROM medicines_database WHERE id = :id")
    suspend fun deleteMedicine(id: Int)

    // --- App Version Config ---
    @Query("SELECT * FROM app_version_config WHERE id = 'global_version' LIMIT 1")
    fun getAppVersionConfigFlow(): Flow<AppVersionConfig?>

    @Query("SELECT * FROM app_version_config WHERE id = 'global_version' LIMIT 1")
    suspend fun getAppVersionConfig(): AppVersionConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppVersionConfig(config: AppVersionConfig)

    // --- Feature Flags ---
    @Query("SELECT * FROM feature_flags_config WHERE id = 'global_flags' LIMIT 1")
    fun getFeatureFlagsConfigFlow(): Flow<FeatureFlagsConfig?>

    @Query("SELECT * FROM feature_flags_config WHERE id = 'global_flags' LIMIT 1")
    suspend fun getFeatureFlagsConfig(): FeatureFlagsConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeatureFlagsConfig(config: FeatureFlagsConfig)

    // --- Scheduled Notifications ---
    @Query("SELECT * FROM scheduled_notifications ORDER BY scheduledTime DESC")
    fun getAllScheduledNotificationsFlow(): Flow<List<ScheduledNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduledNotification(notif: ScheduledNotification)

    @Query("DELETE FROM scheduled_notifications WHERE id = :id")
    suspend fun deleteScheduledNotification(id: Int)

    // --- Admin Logs ---
    @Query("SELECT * FROM admin_logs ORDER BY timestamp DESC")
    fun getAllAdminLogsFlow(): Flow<List<AdminLog>>

    @Query("SELECT * FROM admin_logs WHERE adminEmail = :email AND action = :action AND timestamp = :timestamp LIMIT 1")
    suspend fun getAdminLogByDetails(email: String, action: String, timestamp: Long): AdminLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdminLog(log: AdminLog)
}

