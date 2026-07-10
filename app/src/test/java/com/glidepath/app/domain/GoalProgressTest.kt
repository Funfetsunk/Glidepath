package com.glidepath.app.domain

import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.domain.model.Payment
import com.glidepath.app.domain.model.goalProgressOf
import com.glidepath.app.domain.model.monthsToClear
import com.glidepath.app.domain.model.recentAveragePayment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalProgressTest {

    private fun debtGoal(total: Long = 850_00) = Goal(
        id = 1, name = "Card", type = GoalType.DEBT, total = total,
        currency = "GBP", monthlyTarget = 0, createdAt = 0L,
    )

    private fun savingsGoal(total: Long = 850_00) = Goal(
        id = 1, name = "Trip", type = GoalType.SAVINGS, total = total,
        currency = "GBP", monthlyTarget = 0, createdAt = 0L,
    )

    private fun pay(amount: Long, date: Long = 0L) =
        Payment(goalId = 1, amount = amount, date = date)

    @Test
    fun debt_hero_is_remaining_and_shrinks() {
        val p = goalProgressOf(debtGoal(total = 1000_00), listOf(pay(250_00)))
        assertEquals(250_00L, p.paid)
        assertEquals(750_00L, p.remaining)
        assertEquals(750_00L, p.hero) // debt hero = remaining
        assertEquals(0.25f, p.progress, 0.0001f)
        assertFalse(p.isComplete)
    }

    @Test
    fun savings_hero_is_paid_and_grows() {
        val p = goalProgressOf(savingsGoal(total = 1000_00), listOf(pay(250_00)))
        assertEquals(250_00L, p.paid)
        assertEquals(750_00L, p.remaining)
        assertEquals(250_00L, p.hero) // savings hero = paid
        assertEquals(0.25f, p.progress, 0.0001f)
    }

    @Test
    fun overpayment_clamps_progress_and_floors_remaining() {
        val p = goalProgressOf(debtGoal(total = 1000_00), listOf(pay(1200_00)))
        assertEquals(0L, p.remaining)
        assertEquals(1f, p.progress, 0.0001f)
        assertTrue(p.isComplete)
    }

    @Test
    fun exact_completion_is_complete() {
        val p = goalProgressOf(savingsGoal(total = 500_00), listOf(pay(500_00)))
        assertEquals(1f, p.progress, 0.0001f)
        assertTrue(p.isComplete)
    }

    @Test
    fun zero_total_does_not_divide_by_zero() {
        val p = goalProgressOf(debtGoal(total = 0), listOf(pay(100_00)))
        assertEquals(0f, p.progress, 0.0001f)
    }

    @Test
    fun no_payments_is_zero_progress() {
        val p = goalProgressOf(debtGoal(total = 1000_00), emptyList())
        assertEquals(0L, p.paid)
        assertEquals(1000_00L, p.remaining)
        assertEquals(0f, p.progress, 0.0001f)
    }

    @Test
    fun months_to_clear_rounds_up() {
        assertEquals(3, monthsToClear(remaining = 250_00, perMonth = 100_00))
        assertEquals(1, monthsToClear(remaining = 100_00, perMonth = 100_00))
    }

    @Test
    fun months_to_clear_null_when_no_rate_or_nothing_left() {
        assertNull(monthsToClear(remaining = 0, perMonth = 100_00))
        assertNull(monthsToClear(remaining = 100_00, perMonth = 0))
    }

    @Test
    fun recent_average_uses_last_three_by_date() {
        val payments = listOf(
            pay(100_00, date = 1),
            pay(200_00, date = 2),
            pay(300_00, date = 3),
            pay(900_00, date = 4),
        )
        // three most recent by date: 900, 300, 200 -> 466_66 (integer division)
        assertEquals((900_00L + 300_00L + 200_00L) / 3, recentAveragePayment(payments))
    }

    @Test
    fun recent_average_null_when_empty() {
        assertNull(recentAveragePayment(emptyList()))
    }
}
