package com.glidepath.app.domain.model

/** Whether a goal counts down a debt to zero or climbs savings up to a target. */
enum class GoalType { DEBT, SAVINGS }

/**
 * A single money goal. All amounts are in integer minor units (pennies) to avoid
 * floating-point drift; they are formatted for display only at the UI edge.
 *
 * @property total For [GoalType.DEBT] the original amount owed; for [GoalType.SAVINGS] the target.
 * @property monthlyTarget Optional planned monthly amount in pennies, `0` when not set.
 */
data class Goal(
    val id: Long = 0,
    val name: String,
    val type: GoalType,
    val total: Long,
    val currency: String,
    val monthlyTarget: Long = 0,
    val createdAt: Long,
    val completedAt: Long? = null,
)

/**
 * A logged payment (debt) or contribution (savings). [amount] is always positive pennies.
 */
data class Payment(
    val id: Long = 0,
    val goalId: Long,
    val amount: Long,
    val date: Long,
    val note: String = "",
)
