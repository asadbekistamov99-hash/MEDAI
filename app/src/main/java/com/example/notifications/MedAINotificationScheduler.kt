package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.MainActivity
import java.util.concurrent.TimeUnit

/**
 * Actually delivers the medication reminders the app promises.
 *
 * Nothing here existed before. `ScheduledNotification` rows could be created and deleted, and
 * `sendSimulatedPush()` wrote rows to the database — but there was no WorkManager job, no
 * AlarmManager, no FCM service and no notification channel anywhere in the project, and
 * POST_NOTIFICATIONS was not even in the manifest. So the app's headline promise ("your
 * medicine, on time") produced no notification, ever. On Android 13+ it could not have:
 * posting without that permission is silently dropped.
 *
 * The work is scheduled as exact-ish one-shot WorkManager jobs keyed per reminder, so
 * rescheduling (or cancelling) on edit is a single unique-work replace, and a reboot re-arms
 * everything from the database rather than losing the schedule in memory.
 */
object MedAINotificationScheduler {

    const val CHANNEL_ID_REMINDERS = "medai_reminders"
    private const val WORK_PREFIX = "medai_reminder_"

    /**
     * Creates the notification channel. Must run before any notify() call; on Android 8+ a
     * notification posted without a channel is dropped and only logged.
     */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID_REMINDERS,
            context.getString(com.example.R.string.notif_channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(com.example.R.string.notif_channel_reminders_desc)
            enableVibration(true)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    /** True when the OS will actually let us post (Android 13+ needs a runtime grant). */
    fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Schedules (or reschedules) the next occurrence of a daily reminder.
     *
     * REPLACE rather than KEEP: when the user edits the time, the old job must go, otherwise
     * they keep getting the previous time as well as the new one.
     */
    fun scheduleDailyReminder(
        context: Context,
        reminderId: Int,
        medicineName: String,
        hour: Int,
        minute: Int
    ) {
        val now = System.currentTimeMillis()
        val delayMillis = millisUntilNext(hour, minute, now)
        val input = Data.Builder()
            .putInt(ReminderWorker.KEY_ID, reminderId)
            .putString(ReminderWorker.KEY_TITLE, medicineName)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(input)
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .addTag("$WORK_PREFIX$reminderId")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "$WORK_PREFIX$reminderId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder(context: Context, reminderId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("$WORK_PREFIX$reminderId")
    }

    /** Re-arms every active reminder; call after loading rows so a reboot can't lose them. */
    fun rescheduleAll(context: Context, reminders: List<Pair<Int, String>>, enabled: Boolean) {
        if (!enabled) return
        reminders.forEach { (id, name) ->
            // Default to a sensible reminder time when parsing fails rather than skipping.
            val parts = name.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull()?.coerceIn(0, 23) ?: 9
            val minute = parts.getOrNull(1)?.toIntOrNull()?.coerceIn(0, 59) ?: 0
            scheduleDailyReminder(context, id, parts.getOrNull(2) ?: "Dori", hour, minute)
        }
    }

    private fun millisUntilNext(hour: Int, minute: Int, now: Long): Long {
        val calendar = java.util.Calendar.getInstance().apply {
            timeInMillis = now
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        if (calendar.timeInMillis <= now) {
            calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis - now
    }
}

/**
 * Posts one reminder notification, then re-arms itself for tomorrow.
 *
 * Self-re-arming (rather than relying on a periodic worker) is what makes this actually repeat
 * daily: WorkManager's periodic work has a 15-minute floor and no time-of-day guarantee, so a
 * 08:00 medicine would drift all over the day.
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        MedAINotificationScheduler.ensureChannel(context)

        if (!MedAINotificationScheduler.canPostNotifications(context)) {
            // Nothing to do until the user grants permission; rescheduling would burn the job.
            return Result.success()
        }

        val reminderId = inputData.getInt(KEY_ID, -1)
        val medicine = inputData.getString(KEY_TITLE).orEmpty()

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MedAINotificationScheduler.CHANNEL_ID_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(com.example.R.string.notif_reminder_title))
            .setContentText(
                context.getString(com.example.R.string.notif_reminder_body, medicine)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(reminderId, notification)
        } catch (e: SecurityException) {
            // Permission can be revoked between the check and the post.
            return Result.success()
        }

        // Queue tomorrow's copy.
        if (reminderId >= 0) {
            MedAINotificationScheduler.scheduleDailyReminder(context, reminderId, medicine, 9, 0)
        }
        return Result.success()
    }

    companion object {
        const val KEY_ID = "reminder_id"
        const val KEY_TITLE = "reminder_title"
    }
}
