package com.glidepath.app.ui.logpayment

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
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
import com.glidepath.app.ui.components.NumberKeypad
import com.glidepath.app.ui.components.PrimaryButton
import com.glidepath.app.ui.components.SectionLabel
import com.glidepath.app.ui.theme.GlideShapes
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

/**
 * Fast payment entry (§7.3): big mono amount, quick chips, optional note, custom keypad.
 * Amount supports minor units (pence/cents) via the currency-aware decimal key; [onConfirm]
 * receives pennies.
 */
@Composable
fun LogPaymentScreen(
    isDebt: Boolean,
    currencyCode: String,
    monthlyTargetPennies: Long,
    onConfirm: (amountPennies: Long, note: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val glide = LocalGlide.current
    val decimals = currencyDecimals(currencyCode)
    var raw by remember { mutableStateOf("") }
    var note by remember { mutableStateOf(TextFieldValue("")) }
    val amountPennies = moneyRawToPennies(raw)
    val valid = amountPennies > 0L

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 16.dp),
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Cancel",
                style = GlideType.body.copy(color = glide.muted),
                modifier = Modifier.clickable(onClick = onCancel),
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (isDebt) "Log a payment" else "Add to savings",
                style = GlideType.body.copy(color = glide.text),
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        Spacer(Modifier.height(28.dp))

        // Big amount
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.Top) {
            Text(
                currencySymbol(currencyCode),
                style = GlideType.milestonePercent.copy(color = glide.accent, fontSize = androidx.compose.ui.unit.TextUnit(34f, androidx.compose.ui.unit.TextUnitType.Sp)),
                modifier = Modifier.padding(top = 6.dp, end = 2.dp),
            )
            Text(
                moneyRawDisplay(raw, currencyCode),
                style = GlideType.milestonePercent.copy(color = if (valid) glide.text else glide.muted),
            )
        }
        Text(
            if (isDebt) "How much did you pay?" else "How much are you adding?",
            style = GlideType.secondary.copy(color = glide.muted),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(Modifier.height(20.dp))

        // Quick chips
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickChip("${currencySymbol(currencyCode)}50", Modifier.weight(1f)) { raw = "50" }
            QuickChip("${currencySymbol(currencyCode)}100", Modifier.weight(1f)) { raw = "100" }
            if (monthlyTargetPennies > 0) {
                QuickChip(formatMoney(monthlyTargetPennies, currencyCode), Modifier.weight(1f)) {
                    raw = penniesToMoneyRaw(monthlyTargetPennies, decimals)
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("Note (optional)")
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(GlideShapes.tile)
                .background(glide.surface)
                .padding(14.dp),
        ) {
            BasicTextField(
                value = note,
                onValueChange = { note = it },
                textStyle = GlideType.body.copy(color = glide.text),
                cursorBrush = androidx.compose.ui.graphics.SolidColor(glide.accent),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (note.text.isEmpty()) {
                        Text("Add a note", style = GlideType.body.copy(color = glide.muted))
                    }
                    inner()
                },
            )
        }

        Spacer(Modifier.weight(1f))

        NumberKeypad(
            onDigit = { d -> raw = appendMoneyDigit(raw, d, decimals) },
            onBackspace = { raw = backspaceMoney(raw) },
            decimalKey = if (decimals > 0) currencyDecimalSeparator(currencyCode) else null,
            onDecimal = { raw = appendMoneySeparator(raw, decimals) },
        )

        Spacer(Modifier.height(12.dp))
        PrimaryButton(
            text = "Confirm",
            enabled = valid,
            onClick = { onConfirm(amountPennies, note.text) },
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun QuickChip(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val glide = LocalGlide.current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(glide.accentSoft)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = GlideType.body.copy(color = glide.accent))
    }
}
