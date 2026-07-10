package com.glidepath.app.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderDecisionTest {

    @Test
    fun nudges_when_monthly_on_and_not_logged_recently() {
        assertEquals(
            ReminderKind.NUDGE,
            reminderDecision(monthlyOn = true, streakOn = false, recentlyLogged = false, hasPayments = false),
        )
    }

    @Test
    fun no_nudge_when_logged_recently() {
        assertNull(
            reminderDecision(monthlyOn = true, streakOn = false, recentlyLogged = true, hasPayments = true),
        )
    }

    @Test
    fun encourages_when_streak_on_and_rhythm_steady() {
        assertEquals(
            ReminderKind.ENCOURAGE,
            reminderDecision(monthlyOn = false, streakOn = true, recentlyLogged = true, hasPayments = true),
        )
    }

    @Test
    fun no_encouragement_without_payments() {
        assertNull(
            reminderDecision(monthlyOn = false, streakOn = true, recentlyLogged = true, hasPayments = false),
        )
    }

    @Test
    fun nudge_takes_priority_over_encouragement() {
        // Not logged recently -> nudge, even if streak is also on.
        assertEquals(
            ReminderKind.NUDGE,
            reminderDecision(monthlyOn = true, streakOn = true, recentlyLogged = false, hasPayments = true),
        )
    }

    @Test
    fun nothing_when_all_off() {
        assertNull(
            reminderDecision(monthlyOn = false, streakOn = false, recentlyLogged = false, hasPayments = true),
        )
    }
}
