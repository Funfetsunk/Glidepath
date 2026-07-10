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

/** Which reminder (if any) a scheduled run should send. */
enum class ReminderKind { NUDGE, ENCOURAGE }

/**
 * Pure decision for the periodic reminder — at most one, never both, never shame (§10):
 * nudge only when the monthly toggle is on and nothing was logged recently; encourage only when
 * the streak toggle is on and a steady rhythm is being kept.
 */
fun reminderDecision(
    monthlyOn: Boolean,
    streakOn: Boolean,
    recentlyLogged: Boolean,
    hasPayments: Boolean,
): ReminderKind? = when {
    monthlyOn && !recentlyLogged -> ReminderKind.NUDGE
    streakOn && recentlyLogged && hasPayments -> ReminderKind.ENCOURAGE
    else -> null
}

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
        when (reminderDecision(notif.monthly, notif.streak, recentlyLogged, payments.isNotEmpty())) {
            ReminderKind.NUDGE -> notifier.notifyReminder(
                "A gentle nudge",
                "No payments toward ${goal.name} lately — even a small amount keeps the glide going.",
            )
            ReminderKind.ENCOURAGE -> notifier.notifyReminder(
                "Keep it up",
                "You're keeping a steady rhythm on ${goal.name}. Nice flying.",
            )
            null -> Unit
        }
        return Result.success()
    }

    companion object {
        const val RECENT_DAYS = 20L
    }
}
