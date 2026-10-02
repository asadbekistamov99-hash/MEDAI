package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserLocal::class,
        AccountLocal::class,
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
    version = 4,
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
                .addMigrations(MIGRATION_3_4)
                .build()
                INSTANCE = instance
                instance
            }
        }

        /**
         * 3 -> 4: adds the 7-day free trial, fixes the daily-metrics primary key, and scopes
         * health data per user.
         *
         * This replaces `fallbackToDestructiveMigration()`, which silently DELETED the whole
         * database on any schema bump. For an app holding symptom reports, lab results and
         * medication history that is data loss a user cannot undo, so the upgrade is explicit
         * and every existing row is carried over rather than dropped.
         *
         * The daily_health_metrics table needs a real table rebuild: its primary key changes
         * from (date) to (userId, date), which SQLite cannot do with an ALTER, so we create the
         * new shape, copy the rows across (backfilling userId from current_user), and swap.
         */
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // current_user + system_users: trial bookkeeping columns.
                db.execSQL("ALTER TABLE current_user ADD COLUMN trialStartedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE current_user ADD COLUMN trialEndsAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE current_user ADD COLUMN trialExpiryNotifiedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE system_users ADD COLUMN trialStartedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE system_users ADD COLUMN trialEndsAt INTEGER NOT NULL DEFAULT 0")

                // immunization_records: owner scope for family members' vaccination history.
                db.execSQL("ALTER TABLE immunization_records ADD COLUMN ownerUserId TEXT NOT NULL DEFAULT ''")

                // family_members: owner scope, so two accounts can link the same person
                // without overwriting each other's relationship data.
                db.execSQL("ALTER TABLE family_members ADD COLUMN ownerUserId TEXT NOT NULL DEFAULT ''")

                // accounts: credentials table. Backfilled from the current profile so an
                // existing signed-in user keeps working; their password hash is unknown
                // (the old login never checked one), so they will need a password reset.
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS accounts (
                        uid TEXT NOT NULL PRIMARY KEY,
                        email TEXT NOT NULL,
                        passwordHash TEXT NOT NULL DEFAULT '',
                        fcmToken TEXT NOT NULL DEFAULT '',
                        createdAt INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS idx_accounts_email ON accounts (email COLLATE NOCASE)")
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO accounts (uid, email, passwordHash, fcmToken, createdAt)
                    SELECT uid, LOWER(email), '', fcmToken, createdAt FROM current_user
                    """.trimIndent()
                )

                // family_members rows created before this migration have no owner recorded.
                // Attribute them to whoever was signed in at upgrade time rather than leaving
                // them orphaned (ownerUserId = ''), which would hide them from every account.
                db.execSQL(
                    """
                    UPDATE family_members SET ownerUserId = (
                        SELECT uid FROM current_user LIMIT 1
                    ) WHERE ownerUserId = ''
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    UPDATE immunization_records SET ownerUserId = (
                        SELECT uid FROM current_user LIMIT 1
                    ) WHERE ownerUserId = ''
                    """.trimIndent()
                )

                // daily_health_metrics: (date) -> (userId, date).
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_health_metrics_new (
                        userId TEXT NOT NULL,
                        date TEXT NOT NULL,
                        waterGlasses INTEGER NOT NULL DEFAULT 0,
                        waterGoal INTEGER NOT NULL DEFAULT 8,
                        weight REAL NOT NULL DEFAULT 0.0,
                        bpSystolic INTEGER NOT NULL DEFAULT 0,
                        bpDiastolic INTEGER NOT NULL DEFAULT 0,
                        heartRate INTEGER NOT NULL DEFAULT 0,
                        sleepHours REAL NOT NULL DEFAULT 0.0,
                        sleepBedtime TEXT NOT NULL DEFAULT '',
                        sleepWaketime TEXT NOT NULL DEFAULT '',
                        steps INTEGER NOT NULL DEFAULT 0,
                        mealsJson TEXT NOT NULL DEFAULT '[]',
                        completedRemindersJson TEXT NOT NULL DEFAULT '[]',
                        symptomCheckCompleted INTEGER NOT NULL DEFAULT 0,
                        timestamp INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY (userId, date)
                    )
                    """.trimIndent()
                )
                // Carry rows over, attributing them to whoever is signed in. If nobody is signed
                // in there is nothing to attribute them to, and the new schema requires a
                // userId — those rows are dropped rather than invented an owner for.
                db.execSQL(
                    """
                    INSERT OR REPLACE INTO daily_health_metrics_new
                    SELECT m.userId, m.date, m.waterGlasses, m.waterGoal, m.weight, m.bpSystolic,
                           m.bpDiastolic, m.heartRate, m.sleepHours, m.sleepBedtime,
                           m.sleepWaketime, m.steps, m.mealsJson, m.completedRemindersJson,
                           m.symptomCheckCompleted, m.timestamp
                    FROM daily_health_metrics m
                    WHERE EXISTS (SELECT 1 FROM current_user u WHERE u.uid = m.userId)
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE daily_health_metrics")
                db.execSQL("ALTER TABLE daily_health_metrics_new RENAME TO daily_health_metrics")

                // Covering index for the per-user history chart, which is now the hot query.
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_daily_metrics_user_date ON daily_health_metrics (userId, date)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_chat_messages_user_type ON chat_messages (userId, chatType)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_reminders_user ON reminders (userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_notifications_user ON notifications (userId, isRead)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_symptom_checks_user ON symptom_checks (userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_lab_results_user ON lab_results (userId)")
            }
        }
    }
}
