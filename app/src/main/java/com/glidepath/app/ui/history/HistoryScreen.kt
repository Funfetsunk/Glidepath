package com.glidepath.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.Payment
import com.glidepath.app.domain.model.appendMoneyDigit
import com.glidepath.app.domain.model.appendMoneySeparator
import com.glidepath.app.domain.model.backspaceMoney
import com.glidepath.app.domain.model.currencyDecimalSeparator
import com.glidepath.app.domain.model.currencyDecimals
import com.glidepath.app.domain.model.currencySymbol
import com.glidepath.app.domain.model.formatMoney
import com.glidepath.app.domain.model.moneyRawDisplay
import com.glidepath.app.domain.model.moneyRawToPennies
import com.glidepath.app.domain.model.penniesToMoneyRaw
import com.glidepath.app.ui.HomeData
import com.glidepath.app.ui.components.GlideCard
import com.glidepath.app.ui.components.NumberKeypad
import com.glidepath.app.ui.components.PrimaryButton
import com.glidepath.app.ui.components.SectionLabel
import com.glidepath.app.ui.components.StatCard
import com.glidepath.app.ui.theme.GlideShapes
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide
import com.glidepath.app.ui.util.formatDate

/** Payment history (§7.4): totals, per-entry rows with delete, calm empty state. */
@Composable
fun HistoryScreen(
    data: HomeData,
    onDelete: (Payment) -> Unit,
    onEdit: (Payment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    val isDebt = data.goal.type == com.glidepath.app.domain.model.GoalType.DEBT
    var editing by remember { mutableStateOf<Payment?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        Text("History", style = GlideType.bigTitle.copy(color = glide.text))
        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                label = if (isDebt) "Total paid" else "Total saved",
                value = formatMoney(data.progress.paid, data.goal.currency),
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Entries",
                value = data.payments.size.toString(),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(20.dp))

        if (data.payments.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(data.payments, key = { it.id }) { payment ->
                    PaymentRow(payment, data.goal.currency, onDelete, onClick = { editing = payment })
                }
            }
        }
    }

    editing?.let { payment ->
        PaymentEditorSheet(
            payment = payment,
            currencyCode = data.goal.currency,
            onSave = { onEdit(it); editing = null },
            onDelete = { onDelete(it); editing = null },
            onDismiss = { editing = null },
        )
    }
}

@Composable
private fun PaymentRow(payment: Payment, currency: String, onDelete: (Payment) -> Unit, onClick: () -> Unit) {
    val glide = LocalGlide.current
    GlideCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(GlideShapes.tile)
                    .background(glide.accentSoft),
                contentAlignment = Alignment.Center,
            ) {
                Text("↓", style = GlideType.body.copy(color = glide.accent))
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "+ ${formatMoney(payment.amount, currency)}",
                    style = GlideType.body.copy(color = glide.text),
                )
                Text(
                    text = buildString {
                        append(formatDate(payment.date))
                        if (payment.note.isNotBlank()) append(" · ${payment.note}")
                    },
                    style = GlideType.secondary.copy(color = glide.muted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                "Delete",
                style = GlideType.secondary.copy(color = glide.muted),
                modifier = Modifier.clickable { onDelete(payment) }.padding(8.dp),
            )
        }
    }
}

/** Edit an existing payment: adjust amount (keypad) and note, or delete. Keeps the original date. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentEditorSheet(
    payment: Payment,
    currencyCode: String,
    onSave: (Payment) -> Unit,
    onDelete: (Payment) -> Unit,
    onDismiss: () -> Unit,
) {
    val glide = LocalGlide.current
    val decimals = currencyDecimals(currencyCode)
    var raw by remember { mutableStateOf(penniesToMoneyRaw(payment.amount, decimals)) }
    var note by remember { mutableStateOf(TextFieldValue(payment.note)) }
    val pennies = moneyRawToPennies(raw)
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = glide.surface,
        contentColor = glide.text,
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 22.dp, vertical = 8.dp)) {
            Text("Edit payment", style = GlideType.screenTitle.copy(color = glide.text))
            Spacer(Modifier.height(16.dp))
            Text(
                "${currencySymbol(currencyCode)}${moneyRawDisplay(raw, currencyCode)}",
                style = GlideType.milestonePercent.copy(color = if (pennies > 0) glide.text else glide.muted),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier.fillMaxWidth().clip(GlideShapes.tile).background(glide.bg).padding(14.dp),
            ) {
                BasicTextField(
                    value = note,
                    onValueChange = { note = it },
                    singleLine = true,
                    textStyle = GlideType.body.copy(color = glide.text),
                    cursorBrush = SolidColor(glide.accent),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (note.text.isEmpty()) Text("Add a note", style = GlideType.body.copy(color = glide.muted))
                        inner()
                    },
                )
            }
            Spacer(Modifier.height(12.dp))
            NumberKeypad(
                onDigit = { raw = appendMoneyDigit(raw, it, decimals) },
                onBackspace = { raw = backspaceMoney(raw) },
                decimalKey = if (decimals > 0) currencyDecimalSeparator(currencyCode) else null,
                onDecimal = { raw = appendMoneySeparator(raw, decimals) },
            )
            Spacer(Modifier.height(12.dp))
            PrimaryButton(
                "Save changes",
                enabled = pennies > 0,
                onClick = { onSave(payment.copy(amount = pennies, note = note.text.trim())) },
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Delete this payment",
                style = GlideType.body.copy(color = glide.accent),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().clickable { onDelete(payment) }.padding(12.dp),
            )
        }
    }
}

@Composable
private fun EmptyState() {
    val glide = LocalGlide.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "No payments yet.",
            style = GlideType.body.copy(color = glide.text),
        )
        Text(
            "Your first one starts the glide.",
            style = GlideType.secondary.copy(color = glide.muted),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
