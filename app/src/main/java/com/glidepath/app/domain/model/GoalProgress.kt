package com.glidepath.app.domain.model

import kotlin.math.ceil

/**
 * Derived progress figures for a goal. These are always computed from the goal and its
 * payments — never persisted — so they can never drift out of sync with the source data.
 *
 * @property paid Sum of all payment amounts (pennies).
 * @property remaining `max(0, total - paid)` (pennies); floors at zero on overpayment.
 * @property progress Fraction `0f..1f`, clamped to 1 on overpayment.
 * @property hero The big headline number: [remaining] for debt, [paid] for savings.
 * @property isComplete True once progress reaches 100%.
 */
data class GoalProgress(
    val paid: Long,
    val remaining: Long,
    val progress: Float,
    val hero: Long,
    val isComplete: Boolean,
)

/** Computes [GoalProgress] for [goal] given its [payments]. Pure and side-effect free. */
fun goalProgressOf(goal: Goal, payments: List<Payment>): GoalProgress {
    val paid = payments.sumOf { it.amount }
    val remaining = (goal.total - paid).coerceAtLeast(0L)
    val progress = if (goal.total > 0L) {
        (paid.toFloat() / goal.total.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    val hero = if (goal.type == GoalType.DEBT) remaining else paid
    return GoalProgress(
        paid = paid,
        remaining = remaining,
        progress = progress,
        hero = hero,
        isComplete = progress >= 1f,
    )
}

/**
 * How many whole months are needed to clear [remaining] at [perMonth] pennies/month.
 * Returns `null` when no meaningful projection exists (nothing left, or no rate).
 */
fun monthsToClear(remaining: Long, perMonth: Long): Int? {
    if (remaining <= 0L || perMonth <= 0L) return null
    return ceil(remaining.toDouble() / perMonth.toDouble()).toInt()
}

/** Mean of the most recent [count] payment amounts (pennies), or `null` if there are none. */
fun recentAveragePayment(payments: List<Payment>, count: Int = 3): Long? {
    if (payments.isEmpty()) return null
    val recent = payments.sortedByDescending { it.date }.take(count)
    return recent.sumOf { it.amount } / recent.size
}
