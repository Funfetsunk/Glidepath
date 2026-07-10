package com.glidepath.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.glidepath.app.ui.theme.GlideMode
import com.glidepath.app.ui.theme.GlidePalette
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** Theme choice. Default = Pinewood, Dark (§4). */
data class ThemePrefs(
    val palette: GlidePalette = GlidePalette.PINEWOOD,
    val mode: GlideMode = GlideMode.DARK,
)

/** Notification toggles. Defaults: milestone ON, monthly ON, streak OFF (§10). */
data class NotifPrefs(
    val milestone: Boolean = true,
    val monthly: Boolean = true,
    val streak: Boolean = false,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "glidepath_prefs")

/** Thin wrapper over Preferences DataStore for app settings and per-goal milestone tracking. */
@Singleton
class GlidePreferences @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private object Keys {
        val PALETTE = stringPreferencesKey("palette")
        val MODE = stringPreferencesKey("mode")
        val NOTIF_MILESTONE = booleanPreferencesKey("notif_milestone")
        val NOTIF_MONTHLY = booleanPreferencesKey("notif_monthly")
        val NOTIF_STREAK = booleanPreferencesKey("notif_streak")
        val ACTIVE_GOAL_ID = longPreferencesKey("active_goal_id")
        fun highestMilestone(goalId: Long) = intPreferencesKey("highest_milestone_$goalId")
    }

    val theme: Flow<ThemePrefs> = context.dataStore.data.map { p ->
        ThemePrefs(
            palette = p[Keys.PALETTE]?.let { runCatching { GlidePalette.valueOf(it) }.getOrNull() }
                ?: GlidePalette.PINEWOOD,
            mode = p[Keys.MODE]?.let { runCatching { GlideMode.valueOf(it) }.getOrNull() }
                ?: GlideMode.DARK,
        )
    }

    val notifications: Flow<NotifPrefs> = context.dataStore.data.map { p ->
        NotifPrefs(
            milestone = p[Keys.NOTIF_MILESTONE] ?: true,
            monthly = p[Keys.NOTIF_MONTHLY] ?: true,
            streak = p[Keys.NOTIF_STREAK] ?: false,
        )
    }

    val activeGoalId: Flow<Long?> = context.dataStore.data.map { it[Keys.ACTIVE_GOAL_ID] }

    suspend fun setTheme(palette: GlidePalette, mode: GlideMode) {
        context.dataStore.edit {
            it[Keys.PALETTE] = palette.name
            it[Keys.MODE] = mode.name
        }
    }

    suspend fun setNotifications(prefs: NotifPrefs) {
        context.dataStore.edit {
            it[Keys.NOTIF_MILESTONE] = prefs.milestone
            it[Keys.NOTIF_MONTHLY] = prefs.monthly
            it[Keys.NOTIF_STREAK] = prefs.streak
        }
    }

    suspend fun setActiveGoalId(id: Long?) {
        context.dataStore.edit {
            if (id == null) it.remove(Keys.ACTIVE_GOAL_ID) else it[Keys.ACTIVE_GOAL_ID] = id
        }
    }

    fun highestMilestone(goalId: Long): Flow<Int> =
        context.dataStore.data.map { it[Keys.highestMilestone(goalId)] ?: 0 }

    suspend fun highestMilestoneFirst(goalId: Long): Int =
        context.dataStore.data.map { it[Keys.highestMilestone(goalId)] ?: 0 }.first()

    suspend fun setHighestMilestone(goalId: Long, value: Int) {
        context.dataStore.edit { it[Keys.highestMilestone(goalId)] = value }
    }
}
