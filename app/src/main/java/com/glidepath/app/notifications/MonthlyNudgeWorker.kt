package com.glidepath.app.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.glidepath.app.data.local.GlidePreferences
import com.glidepath.app.data.repository.GoalRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Periodic reminder. Warm and non-nagging (§10): if the monthly nudge is on and nothing has been
 * logged recently it suggests logging; if the keep-it-up toggle is on and the user is keeping a
 * steady rhythm it sends encouragement instead. At most one notification per run — never both,
 * never shame.
 */
@HiltWorker
class MonthlyNudgeWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val repository: GoalRepository,
    private val prefs: GlidePreferences,
    private val notifier: GlideNotifier,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val goal = repository.observeActiveGoal().first() ?: return Result.success()
        if (goal.completedAt != null) return Result.success()

        val notif = prefs.notifications.first()
        val payments = repository.getPayments(goal.id)
        val lastPaymentAt = payments.maxOfOrNull { it.date } ?: 0L
        val quietFor = System.currentTimeMillis() - lastPaymentAt
        val recentlyLogged = quietFor < TimeUnit.DAYS.toMillis(RECENT_DAYS)

        notifier.ensureChannels()
        when {
            notif.monthly && !recentlyLogged ->
                notifier.notifyReminder(
                    "A gentle nudge",
                    "No payments toward ${goal.name} lately — even a small amount keeps the glide going.",
                )
            notif.streak && recentlyLogged && payments.isNotEmpty() ->
                notifier.notifyReminder(
                    "Keep it up",
                    "You're keeping a steady rhythm on ${goal.name}. Nice flying.",
                )
        }
        return Result.success()
    }

    companion object {
        const val RECENT_DAYS = 20L
    }
}
