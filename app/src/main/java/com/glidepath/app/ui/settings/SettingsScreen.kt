package com.glidepath.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.domain.model.formatMoney
import com.glidepath.app.ui.components.GlideCard
import com.glidepath.app.ui.components.SectionLabel
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

/**
 * Settings (§7.7) — stub for the core-loop milestone. The full build (edit goal, appearance,
 * notification toggles, export, support purchase) lands in Phase 3+.
 */
@Composable
fun SettingsScreen(
    goal: Goal,
    themeLabel: String,
    onEditGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Text("Settings", style = GlideType.bigTitle.copy(color = glide.text))
        Spacer(Modifier.height(16.dp))

        SectionLabel("Your goal")
        Spacer(Modifier.height(8.dp))
        GlideCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onEditGoal)) {
            Text(goal.name, style = GlideType.body.copy(color = glide.text), fontWeight = FontWeight.W700)
            val kind = if (goal.type == GoalType.DEBT) "Debt" else "Savings"
            val pace = if (goal.monthlyTarget > 0) " · ${formatMoney(goal.monthlyTarget, goal.currency)}/mo" else ""
            Text(
                "$kind · ${formatMoney(goal.total, goal.currency)}$pace",
                style = GlideType.secondary.copy(color = glide.muted),
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("Appearance")
        Spacer(Modifier.height(8.dp))
        GlideCard(modifier = Modifier.fillMaxWidth()) {
            Text(themeLabel, style = GlideType.body.copy(color = glide.text))
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "Glidepath v1.0 · made local-first, no account needed",
            style = GlideType.caption.copy(color = glide.muted),
        )
    }
}
