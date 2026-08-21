package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserLocal::class,
        SymptomCheck::class,
        ChatMessageLocal::class,
        ReminderLocal::class,
        LabResultLocal::class,
        PaymentRequestLocal::class,
        FamilyMemberLocal::class,
        ImmunizationRecordLocal::class,
        NotificationLocal::class,
        AppConfigLocal::class,
        DailyHealthMetricsLocal::class,
        MedicalDocumentLocal::class,
        PrescriptionScanLocal::class,
        AppointmentRequestLocal::class,
        HealthTipLocal::class,
        UserSystem::class,
        UserActivityLog::class,
        ErrorLog::class,
        ApiUsageLog::class,
        PopupBannerConfig::class,
        AnnouncementConfig::class,
        DiseaseEntry::class,
        MedicineEntry::class,
        AppVersionConfig::class,
        FeatureFlagsConfig::class,
        ScheduledNotification::class,
        AdminLog::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "medai_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
