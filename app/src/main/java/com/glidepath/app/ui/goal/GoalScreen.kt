package com.glidepath.app.ui.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.domain.model.formatMoney
import com.glidepath.app.ui.HomeData
import com.glidepath.app.ui.components.AccentChip
import com.glidepath.app.ui.components.Odometer
import com.glidepath.app.ui.components.PrimaryButton
import com.glidepath.app.ui.components.RunwayPath
import com.glidepath.app.ui.components.StatCard
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide
import com.glidepath.app.ui.util.projectDate
import kotlin.math.roundToInt

/** Home — the Runway. Odometer + glide slope + stats + the primary log action. */
@Composable
fun GoalScreen(
    data: HomeData,
    onLogPayment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    val goal = data.goal
    val isDebt = goal.type == GoalType.DEBT
    val percent = (data.progress.progress * 100).roundToInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(goal.name, style = GlideType.screenTitle.copy(color = glide.text))
                Text(
                    if (isDebt) "Paying down" else "Saving toward",
                    style = GlideType.secondary.copy(color = glide.muted),
                )
            }
            AccentChip(if (isDebt) "DEBT" else "SAVINGS")
        }

        Spacer(Modifier.height(24.dp))

        // Hero odometer
        val heroDesc = if (isDebt) "$percent% paid off, ${formatMoney(data.progress.remaining, goal.currency)} remaining"
        else "$percent% saved, ${formatMoney(data.progress.paid, goal.currency)} of ${formatMoney(goal.total, goal.currency)}"
        Odometer(
            amountPennies = data.progress.hero,
            currencyCode = goal.currency,
            modifier = Modifier.semantics { contentDescription = heroDesc },
        )
        Text(
            text = if (isDebt) "of ${formatMoney(goal.total, goal.currency)} to touchdown"
            else "${formatMoney(data.progress.remaining, goal.currency)} to ${formatMoney(goal.total, goal.currency)}",
            style = GlideType.secondary.copy(color = glide.muted),
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(Modifier.height(20.dp))

        // Runway
        RunwayPath(type = goal.type, progress = data.progress.progress)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(if (isDebt) "Start" else "Ground", style = GlideType.caption.copy(color = glide.muted))
            Text(
                "$percent% ${if (isDebt) "down" else "up"}",
                style = GlideType.caption.copy(color = glide.accent),
            )
            Text(
                if (isDebt) "Touchdown · ${formatMoney(0, goal.currency)}"
                else "Goal · ${formatMoney(goal.total, goal.currency)}",
                style = GlideType.caption.copy(color = glide.muted),
            )
        }

        Spacer(Modifier.height(24.dp))

        // Stats
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = if (isDebt) "Paid so far" else "Saved so far",
                value = formatMoney(data.progress.paid, goal.currency),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "On target",
                value = projectDate(data.onTargetMonths),
                secondary = "at your pace · ${projectDate(data.atPaceMonths)}",
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(24.dp))

        PrimaryButton(
            text = if (isDebt) "Log a payment" else "Add to savings",
            onClick = onLogPayment,
        )
        Spacer(Modifier.height(8.dp))
    }
}
