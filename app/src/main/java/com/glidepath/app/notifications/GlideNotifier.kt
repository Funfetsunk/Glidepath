package com.glidepath.app.notifications

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
import com.glidepath.app.MainActivity
import com.glidepath.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds and posts Glidepath's local notifications. All copy is warm and non-nagging (§10).
 * Posting silently no-ops when the runtime notification permission is not granted.
 */
@Singleton
class GlideNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    companion object {
        const val CHANNEL_PROGRESS = "progress"   // milestone + goal complete
        const val CHANNEL_REMINDERS = "reminders"  // monthly nudge + streak

        private const val ID_MILESTONE = 1001
        private const val ID_COMPLETE = 1002
        private const val ID_REMINDER = 1003
    }

    /** Creates the notification channels. Safe to call repeatedly. */
    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_PROGRESS, "Progress", NotificationManager.IMPORTANCE_DEFAULT)
                .apply { description = "Milestones and finished goals" },
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDERS, "Reminders", NotificationManager.IMPORTANCE_LOW)
                .apply { description = "Gentle nudges to log your progress" },
        )
    }

    fun notifyMilestone(percent: Int, goalName: String) {
        val (title, body) = when (percent) {
            25 -> "A quarter of the way" to "$goalName — the hardest part is behind you."
            50 -> "Halfway there" to "$goalName is halfway. Nice going."
            75 -> "Three quarters down" to "$goalName — you can see the runway from here."
            else -> "Milestone reached" to "$goalName is moving."
        }
        post(CHANNEL_PROGRESS, ID_MILESTONE, title, body)
    }

    fun notifyComplete(goalName: String, isDebt: Boolean) {
        val title = if (isDebt) "Paid off" else "Fully funded"
        post(CHANNEL_PROGRESS, ID_COMPLETE, title, "$goalName — you reached the goal. Touchdown.")
    }

    fun notifyReminder(title: String, body: String) {
        post(CHANNEL_REMINDERS, ID_REMINDER, title, body)
    }

    private fun post(channel: String, id: Int, title: String, body: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notification)
    }
}
