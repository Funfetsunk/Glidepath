package com.glidepath.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glidepath.app.data.local.NotifPrefs
import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.domain.model.formatMoney
import com.glidepath.app.ui.components.GlideCard
import com.glidepath.app.ui.components.PrimaryButton
import com.glidepath.app.ui.components.SectionLabel
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

/** Settings (§7.7). Edit-goal entry, Appearance, notification toggles, backup stub, support card. */
@Composable
fun SettingsScreen(
    goal: Goal,
    themeLabel: String,
    notifications: NotifPrefs,
    onEditGoal: () -> Unit,
    onOpenAppearance: () -> Unit,
    onNotificationsChange: (NotifPrefs) -> Unit,
    onSupport: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Text("Settings", style = GlideType.bigTitle.copy(color = glide.text))
        Spacer(Modifier.height(16.dp))

        SectionLabel("Your goal")
        Spacer(Modifier.height(8.dp))
        GlideCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onEditGoal)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, style = GlideType.body.copy(color = glide.text), fontWeight = FontWeight.W700)
                    val kind = if (goal.type == GoalType.DEBT) "Debt" else "Savings"
                    val pace = if (goal.monthlyTarget > 0) " · ${formatMoney(goal.monthlyTarget, goal.currency)}/mo" else ""
                    Text(
                        "$kind · ${formatMoney(goal.total, goal.currency)}$pace",
                        style = GlideType.secondary.copy(color = glide.muted),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Text("›", style = GlideType.bigTitle.copy(color = glide.muted))
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("App")
        Spacer(Modifier.height(8.dp))
        GlideCard(modifier = Modifier.fillMaxWidth()) {
            RowLink("Appearance", themeLabel, onOpenAppearance)
            Spacer(Modifier.height(4.dp))
            ToggleRow("Milestone alerts", notifications.milestone) {
                onNotificationsChange(notifications.copy(milestone = it))
            }
            ToggleRow("Monthly nudge", notifications.monthly) {
                onNotificationsChange(notifications.copy(monthly = it))
            }
            ToggleRow("Keep-it-up encouragement", notifications.streak) {
                onNotificationsChange(notifications.copy(streak = it))
            }
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("Data")
        Spacer(Modifier.height(8.dp))
        GlideCard(modifier = Modifier.fillMaxWidth()) {
            Text("Back up your data", style = GlideType.body.copy(color = glide.text), fontWeight = FontWeight.W700)
            Text(
                "Export everything to a file (JSON). Coming soon.",
                style = GlideType.secondary.copy(color = glide.muted),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(glide.accentSoft, com.glidepath.app.ui.theme.GlideShapes.card)
                .padding(16.dp),
        ) {
            Text("Glidepath is free, and stays that way", style = GlideType.body.copy(color = glide.text), fontWeight = FontWeight.W700)
            Text(
                "No pressure, no locked features. If it's helped, you can leave a tip.",
                style = GlideType.secondary.copy(color = glide.muted),
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            PrimaryButton("Support the app", onSupport)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Glidepath v1.0 · made local-first, no account needed",
            style = GlideType.caption.copy(color = glide.muted),
        )
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun RowLink(title: String, value: String, onClick: () -> Unit) {
    val glide = LocalGlide.current
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = GlideType.body.copy(color = glide.text), modifier = Modifier.weight(1f))
        Text(value, style = GlideType.secondary.copy(color = glide.muted))
        Text(" ›", style = GlideType.body.copy(color = glide.muted))
    }
}

@Composable
private fun ToggleRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    val glide = LocalGlide.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = GlideType.body.copy(color = glide.text), modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = glide.onAccent,
                checkedTrackColor = glide.accent,
                uncheckedThumbColor = glide.muted,
                uncheckedTrackColor = glide.track2,
            ),
        )
    }
}
