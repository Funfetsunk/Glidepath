package com.glidepath.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glidepath.app.data.local.GlidePreferences
import com.glidepath.app.data.local.NotifPrefs
import com.glidepath.app.data.local.ThemePrefs
import com.glidepath.app.data.repository.GoalRepository
import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.GoalProgress
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.domain.model.Payment
import com.glidepath.app.domain.model.goalProgressOf
import com.glidepath.app.domain.model.monthsToClear
import com.glidepath.app.domain.model.recentAveragePayment
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Snapshot of the Home screen, with all derived figures precomputed. */
data class HomeData(
    val goal: Goal,
    val payments: List<Payment>,
    val progress: GoalProgress,
    /** Months to clear at the set monthly target, formatted date-ready (null = no target). */
    val onTargetMonths: Int?,
    /** Months to clear at the recent average pace (null = no payments). */
    val atPaceMonths: Int?,
)

/** Top-level UI state for the single-goal app. */
sealed interface GoalUiState {
    data object Loading : GoalUiState
    data object NeedsOnboarding : GoalUiState
    data class Ready(val data: HomeData) : GoalUiState
}

/** One-off events for overlays / navigation / toasts. */
sealed interface GlideEvent {
    data class MilestoneReached(val percent: Int) : GlideEvent
    data object GoalCompleted : GlideEvent
    data class Toast(val message: String) : GlideEvent
}

/**
 * Central ViewModel for the single active goal. Exposes [uiState] for Home and derived state,
 * [theme] for app-level theming, and [events] for transient overlays/toasts. Milestone crossings
 * are detected on each payment change against the per-goal highest celebrated value in prefs.
 */
@HiltViewModel
class GlidepathViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val prefs: GlidePreferences,
    private val notifier: com.glidepath.app.notifications.GlideNotifier,
    private val scheduler: com.glidepath.app.notifications.NotificationScheduler,
) : ViewModel() {

    init {
        notifier.ensureChannels()
        viewModelScope.launch { scheduler.sync(prefs.notifications.first()) }
    }

    val theme: StateFlow<ThemePrefs> = prefs.theme
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemePrefs())

    val notifications: StateFlow<NotifPrefs> = prefs.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotifPrefs())

    private val eventChannel = Channel<GlideEvent>(Channel.BUFFERED)
    val events: Flow<GlideEvent> = eventChannel.receiveAsFlow()

    @Suppress("OPT_IN_USAGE")
    val uiState: StateFlow<GoalUiState> = repository.observeActiveGoal()
        .flatMapLatest { goal ->
            if (goal == null) {
                flowOf(GoalUiState.NeedsOnboarding)
            } else {
                repository.observePayments(goal.id).map { payments ->
                    val progress = goalProgressOf(goal, payments)
                    GoalUiState.Ready(
                        HomeData(
                            goal = goal,
                            payments = payments,
                            progress = progress,
                            onTargetMonths = if (goal.monthlyTarget > 0) {
                                monthsToClear(progress.remaining, goal.monthlyTarget)
                            } else null,
                            atPaceMonths = recentAveragePayment(payments)?.let {
                                monthsToClear(progress.remaining, it)
                            },
                        ),
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GoalUiState.Loading)

    /** Creates the first goal (fresh onboarding). Wipes any prior payments + milestone tracking. */
    fun createGoal(
        name: String,
        type: GoalType,
        totalPennies: Long,
        currency: String,
        monthlyTargetPennies: Long,
    ) {
        viewModelScope.launch {
            val id = repository.createGoal(
                Goal(
                    name = name.trim(),
                    type = type,
                    total = totalPennies,
                    currency = currency,
                    monthlyTarget = monthlyTargetPennies,
                    createdAt = System.currentTimeMillis(),
                ),
            )
            prefs.setActiveGoalId(id)
            prefs.setHighestMilestone(id, 0)
        }
    }

    /** Edits goal details, keeping existing payments (edit-goal flow). */
    fun updateGoal(goal: Goal) {
        viewModelScope.launch { repository.updateGoal(goal) }
    }

    fun setTheme(palette: com.glidepath.app.ui.theme.GlidePalette, mode: com.glidepath.app.ui.theme.GlideMode) {
        viewModelScope.launch { prefs.setTheme(palette, mode) }
    }

    fun setNotifications(prefs: NotifPrefs) {
        viewModelScope.launch {
            this@GlidepathViewModel.prefs.setNotifications(prefs)
            scheduler.sync(prefs)
        }
    }

    /** Clears the active goal (and its payments via cascade) to start onboarding a new one. */
    fun startNewGoal(currentGoalId: Long) {
        viewModelScope.launch {
            repository.deleteGoal(currentGoalId)
            prefs.setActiveGoalId(null)
        }
    }

    /** Logs a payment, then checks for a newly-crossed milestone / completion. */
    fun addPayment(goalId: Long, amountPennies: Long, date: Long, note: String) {
        viewModelScope.launch {
            repository.addPayment(
                Payment(goalId = goalId, amount = amountPennies, date = date, note = note.trim()),
            )
            checkMilestones(goalId)
        }
    }

    fun deletePayment(payment: Payment) {
        // Deleting recomputes progress but never re-triggers past milestones.
        viewModelScope.launch { repository.deletePayment(payment) }
    }

    /** Edits an existing payment. Recomputes progress but does not re-fire past milestones. */
    fun updatePayment(payment: Payment) {
        viewModelScope.launch { repository.updatePayment(payment) }
    }

    private suspend fun checkMilestones(goalId: Long) {
        val goal = repository.getGoal(goalId) ?: return
        val payments = repository.getPayments(goalId)
        val progress = goalProgressOf(goal, payments)
        val reachedPercent = when {
            progress.progress >= 1f -> 100
            progress.progress >= 0.75f -> 75
            progress.progress >= 0.5f -> 50
            progress.progress >= 0.25f -> 25
            else -> 0
        }
        val highest = prefs.highestMilestoneFirst(goalId)
        if (reachedPercent > highest) {
            prefs.setHighestMilestone(goalId, reachedPercent)
            val notifyProgress = prefs.notifications.first().milestone
            if (reachedPercent == 100) {
                repository.setGoalCompleted(goalId, System.currentTimeMillis())
                eventChannel.send(GlideEvent.GoalCompleted)
                if (notifyProgress) notifier.notifyComplete(goal.name, goal.type == GoalType.DEBT)
            } else {
                eventChannel.send(GlideEvent.MilestoneReached(reachedPercent))
                if (notifyProgress) notifier.notifyMilestone(reachedPercent, goal.name)
            }
        }
    }
}
