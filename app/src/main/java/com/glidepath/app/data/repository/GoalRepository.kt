package com.glidepath.app.data.repository

import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.Payment
import kotlinx.coroutines.flow.Flow

/**
 * Single point of access to goal + payment data. The app currently works with one active
 * goal at a time (tracked by `activeGoalId` in preferences), but the API is goal-scoped so
 * multiple goals can be added later without reshaping callers.
 */
interface GoalRepository {

    /** Emits the active goal (most recent), or `null` before onboarding. */
    fun observeActiveGoal(): Flow<Goal?>

    /** Emits payments for [goalId], newest first. */
    fun observePayments(goalId: Long): Flow<List<Payment>>

    suspend fun getGoal(goalId: Long): Goal?

    /** Creates a goal and returns its new id. */
    suspend fun createGoal(goal: Goal): Long

    suspend fun updateGoal(goal: Goal)

    /** Sets or clears the completion timestamp for a goal. */
    suspend fun setGoalCompleted(goalId: Long, completedAt: Long?)

    /** Deletes a goal and all its payments. */
    suspend fun deleteGoal(goalId: Long)

    suspend fun addPayment(payment: Payment): Long

    suspend fun updatePayment(payment: Payment)

    suspend fun deletePayment(payment: Payment)

    suspend fun getPayments(goalId: Long): List<Payment>
}
