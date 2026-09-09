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

    // --- Symptom Checks ---
    @Query("SELECT * FROM symptom_checks ORDER BY timestamp DESC")
    fun getAllSymptomChecksFlow(): Flow<List<SymptomCheck>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptomCheck(check: SymptomCheck)

    @Query("DELETE FROM symptom_checks WHERE id = :id")
    suspend fun deleteSymptomCheck(id: Int)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE chatType = :chatType ORDER BY timestamp ASC")
    fun getChatMessagesFlow(chatType: String): Flow<List<ChatMessageLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageLocal)

    @Query("DELETE FROM chat_messages WHERE chatType = :chatType")
    suspend fun clearChatHistory(chatType: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE chatType = 'general' AND role = 'user' AND timestamp >= :todayStart")
    suspend fun getTodayGeneralChatCount(todayStart: Long): Int

    // --- Reminders ---
    @Query("SELECT * FROM reminders ORDER BY timestamp DESC")
    fun getAllRemindersFlow(): Flow<List<ReminderLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderLocal)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Int)

    // --- Lab Results ---
    @Query("SELECT * FROM lab_results ORDER BY timestamp DESC")
    fun getAllLabResultsFlow(): Flow<List<LabResultLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabResult(result: LabResultLocal)

    @Query("DELETE FROM lab_results WHERE id = :id")
    suspend fun deleteLabResult(id: Int)

    // --- Payment Requests ---
    @Query("SELECT * FROM payment_requests ORDER BY submittedAt DESC")
    fun getAllPaymentRequestsFlow(): Flow<List<PaymentRequestLocal>>

    @Query("SELECT * FROM payment_requests WHERE id = :id LIMIT 1")
    suspend fun getPaymentRequestById(id: String): PaymentRequestLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentRequest(request: PaymentRequestLocal)

    @Query("DELETE FROM payment_requests WHERE id = :id")
    suspend fun deletePaymentRequest(id: String)

    // --- Family Members ---
    @Query("SELECT * FROM family_members")
    fun getAllFamilyMembersFlow(): Flow<List<FamilyMemberLocal>>

    @Query("SELECT * FROM family_members WHERE uid = :uid LIMIT 1")
    suspend fun getFamilyMember(uid: String): FamilyMemberLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamilyMember(member: FamilyMemberLocal)

    @Query("DELETE FROM family_members WHERE uid = :uid")
    suspend fun deleteFamilyMember(uid: String)

    // --- Immunization Records ---
    @Query("SELECT * FROM immunization_records ORDER BY timestamp DESC")
    fun getAllImmunizationRecordsFlow(): Flow<List<ImmunizationRecordLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImmunizationRecord(record: ImmunizationRecordLocal)

    @Query("DELETE FROM immunization_records WHERE id = :id")
    suspend fun deleteImmunizationRecord(id: Int)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationLocal>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationLocal)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // --- App Config ---
    @Query("SELECT * FROM app_config WHERE id = 'global' LIMIT 1")
    fun getAppConfigFlow(): Flow<AppConfigLocal?>

    @Query("SELECT * FROM app_config WHERE id = 'global' LIMIT 1")
    suspend fun getAppConfig(): AppConfigLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppConfig(config: AppConfigLocal)

    // --- Daily Health Metrics ---
    @Query("SELECT * FROM daily_health_metrics WHERE date = :date LIMIT 1")
    fun getDailyMetricsFlow(date: String): Flow<DailyHealthMetricsLocal?>

    @Query("SELECT * FROM daily_health_metrics ORDER BY date DESC")
    fun getAllDailyMetricsFlow(): Flow<List<DailyHealthMetricsLocal>>

    @Query("SELECT * FROM daily_health_metrics WHERE date = :date LIMIT 1")
    suspend fun getDailyMetrics(date: String): DailyHealthMetricsLocal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyMetrics(metrics: DailyHealthMetricsLocal)

    // --- Medical Documents ---
    @Query("SELECT * FROM medical_documents WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllMedicalDocumentsFlow(userId: String): Flow<List<MedicalDocumentLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicalDocument(doc: MedicalDocumentLocal)

    @Query("DELETE FROM medical_documents WHERE id = :id")
    suspend fun deleteMedicalDocument(id: Int)

    // --- Prescription Scans ---
    @Query("SELECT * FROM prescription_scans WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllPrescriptionScansFlow(userId: String): Flow<List<PrescriptionScanLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptionScan(scan: PrescriptionScanLocal)

    // --- Appointment Requests ---
    @Query("SELECT * FROM appointment_requests ORDER BY timestamp DESC")
    fun getAllAppointmentRequestsFlow(): Flow<List<AppointmentRequestLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointmentRequest(req: AppointmentRequestLocal)

    @Query("DELETE FROM appointment_requests WHERE id = :id")
    suspend fun deleteAppointmentRequest(id: Int)

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

