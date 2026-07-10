package com.glidepath.app.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.Goal
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.domain.model.formatMoney
import com.glidepath.app.ui.components.GlideCard
import com.glidepath.app.ui.components.PrimaryButton
import com.glidepath.app.ui.components.SectionLabel
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

/**
 * Goal complete (§7.6) — a dedicated finish line, distinct from a runway stuck at 100%.
 * Shows the touchdown, totals, and the two continue actions.
 */
@Composable
fun GoalCompleteScreen(
    goal: Goal,
    paidPennies: Long,
    paymentCount: Int,
    onStartNew: () -> Unit,
    onBackToGoal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    val isDebt = goal.type == GoalType.DEBT
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))
        // Checkered finish flag glyph.
        Text("🏁", style = GlideType.milestonePercent.copy(color = glide.accent))
        Spacer(Modifier.height(16.dp))
        SectionLabel("Touchdown")
        Spacer(Modifier.height(8.dp))
        Text(
            if (isDebt) "${goal.name} is paid off" else "${goal.name} — fully funded",
            style = GlideType.bigTitle.copy(color = glide.text),
            textAlign = TextAlign.Center,
        )
        Text(
            if (isDebt) "You cleared it. Every payment brought you here."
            else "You hit your target. That's a real achievement.",
            style = GlideType.body.copy(color = glide.muted),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 10.dp),
        )

        Spacer(Modifier.height(28.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GlideCard(modifier = Modifier.weight(1f)) {
                SectionLabel(if (isDebt) "Total paid" else "Total saved")
                Text(
                    formatMoney(paidPennies, goal.currency),
                    style = GlideType.mono.copy(color = glide.text),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            GlideCard(modifier = Modifier.weight(1f)) {
                SectionLabel("Payments")
                Text(
                    paymentCount.toString(),
                    style = GlideType.mono.copy(color = glide.text),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        Spacer(Modifier.weight(1f))
        PrimaryButton("Start a new goal", onStartNew, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(4.dp))
        Text(
            "Back to my goal",
            style = GlideType.body.copy(color = glide.muted),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBackToGoal)
                .padding(12.dp),
            textAlign = TextAlign.Center,
        )
    }
}
