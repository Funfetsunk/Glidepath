package com.glidepath.app.data.repository

import com.glidepath.app.data.local.GoalDao
import com.glidepath.app.data.local.PaymentDao
import com.glidepath.app.data.local.toDomain
import com.glidepath.app.data.local.toEntity
import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.Payment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Room-backed [GoalRepository]. Maps between Room entities and domain models. */
@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val paymentDao: PaymentDao,
) : GoalRepository {

    override fun observeActiveGoal(): Flow<Goal?> =
        goalDao.observeMostRecentGoal().map { it?.toDomain() }

    override fun observePayments(goalId: Long): Flow<List<Payment>> =
        paymentDao.observePayments(goalId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun getGoal(goalId: Long): Goal? = goalDao.getGoal(goalId)?.toDomain()

    override suspend fun createGoal(goal: Goal): Long = goalDao.insert(goal.toEntity())

    override suspend fun updateGoal(goal: Goal) = goalDao.update(goal.toEntity())

    override suspend fun setGoalCompleted(goalId: Long, completedAt: Long?) =
        goalDao.setCompletedAt(goalId, completedAt)

    override suspend fun deleteGoal(goalId: Long) = goalDao.deleteGoal(goalId)

    override suspend fun addPayment(payment: Payment): Long = paymentDao.insert(payment.toEntity())

    override suspend fun updatePayment(payment: Payment) = paymentDao.update(payment.toEntity())

    override suspend fun deletePayment(payment: Payment) = paymentDao.delete(payment.toEntity())

    override suspend fun getPayments(goalId: Long): List<Payment> =
        paymentDao.getPayments(goalId).map { it.toDomain() }
}
