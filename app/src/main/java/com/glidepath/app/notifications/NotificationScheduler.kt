package com.glidepath.app.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.glidepath.app.data.local.NotifPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Schedules or cancels the periodic reminder worker based on the user's notification prefs. */
@Singleton
class NotificationScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    /**
     * Reconciles the scheduled reminder with [prefs]: keeps a monthly periodic worker while the
     * monthly nudge or keep-it-up toggle is on, cancels it when both are off.
     */
    fun sync(prefs: NotifPrefs) {
        val workManager = WorkManager.getInstance(context)
        if (prefs.monthly || prefs.streak) {
            val request = PeriodicWorkRequestBuilder<MonthlyNudgeWorker>(30, TimeUnit.DAYS)
                .setInitialDelay(30, TimeUnit.DAYS)
                .build()
            workManager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
        } else {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }

    companion object {
        const val WORK_NAME = "monthly_nudge"
    }
}
