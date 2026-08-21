package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_user")
data class UserLocal(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String, // String representation or date
    val gender: String, // "male" or "female"
    val bloodType: String,
    val height: Double,
    val weight: Double,
    val isPremium: Boolean,
    val premiumExpiry: Long?,
    val language: String, // "uz" | "ru" | "en"
    val fcmToken: String,
    val createdAt: Long,
    val lastActive: Long,
    val healthScore: Int,
    val isAdmin: Boolean,
    val isBanned: Boolean,
    val avatarUrl: String,
    val allergiesJson: String = "[]", // [{name, type, addedAt}]
    val unlockedAchievementsJson: String = "[]" // ["beginner", "week", "month", "water", "regular"]
)

@Entity(tableName = "symptom_checks")
data class SymptomCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val symptomsInput: String,
    val bodyPart: String,
    val resultJson: String, // Stores conditions & analysis as JSON
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val chatType: String, // "doctor" or "general"
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reminders")
data class ReminderLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val medicineName: String,
    val time: String, // e.g., "08:00"
    val frequency: String, // "daily", "weekly", etc.
    val type: String, // "Dori", "Doctor", "Laboratoriya", "Jismoniy", "Suv"
    val isActive: Boolean = true,
    val targetFamilyMember: String? = null, // if set, shows which family member it is for
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lab_results")
data class LabResultLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val imagePath: String, // Local cached uri or path
    val analysisText: String, // Structured analysis result text
    val valuesJson: String, // JSON string of specific parsed table parameters
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "payment_requests")
data class PaymentRequestLocal(
    @PrimaryKey val id: String, // unique request id
    val userId: String,
    val userName: String,
    val userEmail: String,
    val userPhone: String,
    val checkImageUrl: String,
    val status: String, // 'pending' | 'approved' | 'rejected'
    val rejectionReason: String,
    val submittedAt: Long,
    val reviewedAt: Long?,
    val reviewedBy: String?
)

@Entity(tableName = "family_members")
data class FamilyMemberLocal(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val relation: String, // "Father", "Mother", "Spouse", "Child", etc.
    val avatarUrl: String,
    val healthScore: Int,
    val stepsToday: Int,
    val lastActive: Long,
    val activeRemindersCount: Int,
    val sosStatus: Boolean, // True if SOS is currently triggered
    val inviteStatus: String, // "pending" | "accepted" | "rejected"
    val isInvitedByMe: Boolean // True if we invited them, false if they invited us
)

@Entity(tableName = "notifications")
data class NotificationLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "reminder" | "sos" | "premium" | "invite" | "system"
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_config")
data class AppConfigLocal(
    @PrimaryKey val id: String = "global",
    val maintenanceMode: Boolean = false,
    val announcementText: String = "",
    val announcementEnabled: Boolean = false,
    val premiumPriceText: String = "30,000 so'm / oy",
    val supportTelegram: String = "Medai_support",
    val maxFreeChatMessages: Int = 10,
    val symptomsCountFlag: Int = 0,
    val bpc1Flag: Boolean = true
)

@Entity(tableName = "daily_health_metrics")
data class DailyHealthMetricsLocal(
    @PrimaryKey val date: String, // "YYYY-MM-DD"
    val userId: String,
    val waterGlasses: Int = 0,
    val waterGoal: Int = 8,
    val weight: Double = 0.0,
    val bpSystolic: Int = 0,
    val bpDiastolic: Int = 0,
    val heartRate: Int = 0,
    val sleepHours: Double = 0.0,
    val sleepBedtime: String = "",
    val sleepWaketime: String = "",
    val steps: Int = 0,
    val mealsJson: String = "[]", // [{title, calories, time, type}]
    val completedRemindersJson: String = "[]", // list of reminder IDs
    val symptomCheckCompleted: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "medical_documents")
data class MedicalDocumentLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val title: String,
    val docType: String, // "insurance" | "checkup" | "allergy" | "other"
    val imagePath: String, // base64 or file path
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "prescription_scans")
data class PrescriptionScanLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val imagePath: String,
    val rawResult: String, // structured explanation cards
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointment_requests")
data class AppointmentRequestLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val doctorId: String,
    val doctorName: String,
    val patientName: String,
    val patientPhone: String,
    val preferredTime: String,
    val status: String = "pending", // "pending" | "confirmed" | "rejected"
    val rejectionReason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "health_tips")
data class HealthTipLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uz: String,
    val ru: String,
    val en: String,
    val orderIndex: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "system_users")
data class UserSystem(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String,
    val gender: String,
    val bloodType: String,
    val height: Double,
    val weight: Double,
    var isPremium: Boolean,
    var premiumExpiry: Long?,
    val language: String,
    val fcmToken: String,
    val createdAt: Long,
    var lastActive: Long,
    val healthScore: Int,
    val isAdmin: Boolean,
    var isBanned: Boolean,
    val avatarUrl: String,
    var currentScreen: String = "Home",
    val city: String = "Toshkent",
    var lastScreenBeforeUpgrade: String = ""
)

@Entity(tableName = "user_activities")
data class UserActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val screenName: String,
    val actionType: String, // "view_screen", "symptom_check", "medicine_search", "reminder_complete"
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "error_logs")
data class ErrorLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val screen: String,
    val errorMessage: String,
    val timestamp: Long = System.currentTimeMillis(),
    val appVersion: String,
    val deviceInfo: String,
    var isResolved: Boolean = false
)

@Entity(tableName = "api_usage_logs")
data class ApiUsageLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val feature: String, // "Symptom", "Doctor", "Medicine", "Lab"
    val tokensUsed: Int,
    val costEstimateUsd: Double,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "popup_banner_config")
data class PopupBannerConfig(
    @PrimaryKey val id: String = "global_banner",
    val active: Boolean = false,
    val imageUrl: String = "",
    val titleUz: String = "",
    val titleRu: String = "",
    val titleEn: String = "",
    val subtitleUz: String = "",
    val subtitleRu: String = "",
    val subtitleEn: String = "",
    val buttonTextUz: String = "Ochish",
    val buttonTextRu: String = "Открыть",
    val buttonTextEn: String = "Open",
    val actionType: String = "dismiss", // "open_screen" | "open_url" | "dismiss"
    val actionValue: String = "",
    val startDate: Long = 0,
    val endDate: Long = 0
)

@Entity(tableName = "announcement_config")
data class AnnouncementConfig(
    @PrimaryKey val id: String = "global_announcement",
    val active: Boolean = false,
    val textUz: String = "",
    val textRu: String = "",
    val textEn: String = "",
    val color: String = "yellow" // "yellow" | "red" | "green" | "blue"
)

@Entity(tableName = "diseases_database")
data class DiseaseEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameUz: String,
    val nameRu: String,
    val nameEn: String,
    val symptomsUz: String,
    val descriptionUz: String,
    val descriptionRu: String,
    val descriptionEn: String,
    val specialistType: String,
    val severity: String // "mild" | "moderate" | "severe"
)

@Entity(tableName = "medicines_database")
data class MedicineEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val dosage: String,
    val sideEffects: String,
    val category: String
)

@Entity(tableName = "app_version_config")
data class AppVersionConfig(
    @PrimaryKey val id: String = "global_version",
    val forceUpdate: Boolean = false,
    val minVersion: String = "1.0.0",
    val messageUz: String = "Yangi versiya mavjud! Iltimos, ilovani yangilang.",
    val messageRu: String = "Доступна новая версия! Пожалуйста, обновите приложение.",
    val messageEn: String = "A new version is available! Please update the app.",
    val storeUrl: String = "https://play.google.com/store"
)

@Entity(tableName = "feature_flags_config")
data class FeatureFlagsConfig(
    @PrimaryKey val id: String = "global_flags",
    val symptomChecker: Boolean = true,
    val aiDoctor: Boolean = true,
    val labAnalysis: Boolean = true,
    val family: Boolean = true,
    val sos: Boolean = true,
    val analytics: Boolean = true
)

@Entity(tableName = "scheduled_notifications")
data class ScheduledNotification(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val scheduledTime: Long,
    val repeatType: String, // "once" | "daily" | "weekly"
    val targetType: String, // "all" | "premium" | "free" | "city" | "segment"
    val targetValue: String,
    val isSent: Boolean = false,
    val sentCount: Int = 0,
    val openedCount: Int = 0
)

@Entity(tableName = "admin_logs")
data class AdminLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val adminEmail: String,
    val action: String,
    val targetUser: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class FirestoreMedicationReminder(
    val id: String = "",
    val userId: String = "",
    val medicineName: String = "",
    val dosage: String = "",
    val time: String = "",
    val frequency: String = "",
    val notificationsEnabled: Boolean = true,
    val notificationFrequency: String = "Exact time",
    val isActive: Boolean = true,
    val targetFamilyMember: String? = null,
    val notes: String? = null,
    val completedDates: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class FirestoreVitalReading(
    val id: String = "",
    val userId: String = "",
    val date: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val heartRate: Int = 0,
    val bpSystolic: Int = 0,
    val bpDiastolic: Int = 0,
    val weight: Double = 0.0,
    val note: String = ""
)

@Entity(tableName = "immunization_records")
data class ImmunizationRecordLocal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val familyMemberUid: String,
    val vaccineName: String,
    val targetDisease: String,
    val scheduledAge: String,
    val dueDate: String,
    val status: String, // "Pending" | "Completed" | "Overdue"
    val completedDate: String? = null,
    val administeredBy: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)




