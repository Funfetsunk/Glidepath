package com.glidepath.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.Currency
import com.glidepath.app.domain.model.GoalType
import com.glidepath.app.domain.model.appendMoneyDigit
import com.glidepath.app.domain.model.appendMoneySeparator
import com.glidepath.app.domain.model.backspaceMoney
import com.glidepath.app.domain.model.clampMoneyRawDecimals
import com.glidepath.app.domain.model.currencyDecimalSeparator
import com.glidepath.app.domain.model.currencyDecimals
import com.glidepath.app.domain.model.currencySymbol
import com.glidepath.app.domain.model.moneyRawDisplay
import com.glidepath.app.domain.model.moneyRawToPennies
import com.glidepath.app.domain.model.penniesToMoneyRaw
import com.glidepath.app.ui.components.NumberKeypad
import com.glidepath.app.ui.components.PrimaryButton
import com.glidepath.app.ui.components.RunwayPath
import com.glidepath.app.ui.components.SectionLabel
import com.glidepath.app.ui.theme.GlideShapes
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

private enum class Step { WELCOME, DETAILS, AMOUNT, PACE }

/** Collected onboarding result, amounts in pennies. */
data class OnboardingResult(
    val name: String,
    val type: GoalType,
    val totalPennies: Long,
    val currency: String,
    val monthlyTargetPennies: Long,
)

/**
 * Goal setup (§7.1). Multi-step with progress dots; finishes by calling [onFinish].
 * When [editMode] is true the Welcome step is skipped, fields are prefilled from [initial],
 * and the finish label becomes "Save changes" (payments are kept by the caller).
 */
@Composable
fun OnboardingScreen(
    onFinish: (OnboardingResult) -> Unit,
    modifier: Modifier = Modifier,
    editMode: Boolean = false,
    initial: OnboardingResult? = null,
) {
    val glide = LocalGlide.current
    var step by remember { mutableStateOf(if (editMode) Step.DETAILS else Step.WELCOME) }
    var name by remember { mutableStateOf(TextFieldValue(initial?.name ?: "")) }
    var type by remember { mutableStateOf(initial?.type ?: GoalType.DEBT) }
    var currency by remember { mutableStateOf(initial?.currency ?: "GBP") }
    val decimals = currencyDecimals(currency)
    var amountRaw by remember {
        mutableStateOf(initial?.totalPennies?.takeIf { it > 0 }?.let { penniesToMoneyRaw(it, decimals) } ?: "")
    }
    var paceRaw by remember {
        mutableStateOf(initial?.monthlyTargetPennies?.takeIf { it > 0 }?.let { penniesToMoneyRaw(it, decimals) } ?: "")
    }
    val finishLabel = if (editMode) "Save changes" else "Start gliding"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(glide.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 20.dp),
    ) {
        if (step != Step.WELCOME) {
            ProgressDots(index = step.ordinal - 1, count = 3)
            Spacer(Modifier.height(24.dp))
        }

        when (step) {
            Step.WELCOME -> WelcomeStep(onNext = { step = Step.DETAILS })
            Step.DETAILS -> DetailsStep(
                name = name, onName = { name = it },
                type = type, onType = { type = it },
                onNext = { if (name.text.isNotBlank()) step = Step.AMOUNT },
            )
            Step.AMOUNT -> AmountStep(
                type = type, currency = currency,
                onCurrency = { newCode ->
                    currency = newCode
                    val d = currencyDecimals(newCode)
                    amountRaw = clampMoneyRawDecimals(amountRaw, d)
                    paceRaw = clampMoneyRawDecimals(paceRaw, d)
                },
                raw = amountRaw, decimals = decimals,
                onDigit = { amountRaw = appendMoneyDigit(amountRaw, it, decimals) },
                onDecimal = { amountRaw = appendMoneySeparator(amountRaw, decimals) },
                onBackspace = { amountRaw = backspaceMoney(amountRaw) },
                onNext = { if (moneyRawToPennies(amountRaw) > 0) step = Step.PACE },
            )
            Step.PACE -> PaceStep(
                currency = currency, raw = paceRaw, decimals = decimals, finishLabel = finishLabel,
                onDigit = { paceRaw = appendMoneyDigit(paceRaw, it, decimals) },
                onDecimal = { paceRaw = appendMoneySeparator(paceRaw, decimals) },
                onBackspace = { paceRaw = backspaceMoney(paceRaw) },
                onFinish = {
                    onFinish(
                        OnboardingResult(
                            name = name.text,
                            type = type,
                            totalPennies = moneyRawToPennies(amountRaw),
                            currency = currency,
                            monthlyTargetPennies = moneyRawToPennies(paceRaw),
                        ),
                    )
                },
            )
        }
    }
}

@Composable
private fun ProgressDots(index: Int, count: Int) {
    val glide = LocalGlide.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { i ->
            Box(
                modifier = Modifier
                    .size(if (i == index) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (i <= index) glide.accent else glide.track2),
            )
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val glide = LocalGlide.current
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.height(40.dp))
        RunwayPath(type = GoalType.SAVINGS, progress = 0.55f)
        Spacer(Modifier.height(40.dp))
        Text("One number,\none runway.", style = GlideType.onboardingHeadline.copy(color = glide.text))
        Text(
            "Set what you owe or what you're saving toward, and watch it glide.",
            style = GlideType.body.copy(color = glide.muted),
            modifier = Modifier.padding(top = 10.dp),
        )
        Spacer(Modifier.weight(1f))
        PrimaryButton("Set up your goal", onNext)
    }
}

@Composable
private fun DetailsStep(
    name: TextFieldValue, onName: (TextFieldValue) -> Unit,
    type: GoalType, onType: (GoalType) -> Unit,
    onNext: () -> Unit,
) {
    val glide = LocalGlide.current
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Name your goal", style = GlideType.onboardingHeadline.copy(color = glide.text))
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier.fillMaxWidth().clip(GlideShapes.tile).background(glide.surface).padding(14.dp),
        ) {
            BasicTextField(
                value = name, onValueChange = onName,
                textStyle = GlideType.body.copy(color = glide.text),
                cursorBrush = SolidColor(glide.accent),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (name.text.isEmpty()) Text("e.g. Credit card, Japan trip", style = GlideType.body.copy(color = glide.muted))
                    inner()
                },
            )
        }
        Spacer(Modifier.height(24.dp))
        SectionLabel("What is it?")
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TypeCard("Paying down", "A debt to clear", type == GoalType.DEBT, Modifier.weight(1f)) { onType(GoalType.DEBT) }
            TypeCard("Saving up", "A target to reach", type == GoalType.SAVINGS, Modifier.weight(1f)) { onType(GoalType.SAVINGS) }
        }
        Spacer(Modifier.weight(1f))
        PrimaryButton("Continue", onNext, enabled = name.text.isNotBlank())
    }
}

@Composable
private fun TypeCard(title: String, subtitle: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val glide = LocalGlide.current
    Column(
        modifier = modifier
            .clip(GlideShapes.card)
            .background(if (selected) glide.accentSoft else glide.surface)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) glide.accent else glide.line,
                shape = GlideShapes.card,
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Text(title, style = GlideType.body.copy(color = glide.text), fontWeight = FontWeight.W700)
        Text(subtitle, style = GlideType.secondary.copy(color = glide.muted), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun AmountStep(
    type: GoalType, currency: String, onCurrency: (String) -> Unit,
    raw: String, decimals: Int,
    onDigit: (Char) -> Unit, onDecimal: () -> Unit, onBackspace: () -> Unit, onNext: () -> Unit,
) {
    val glide = LocalGlide.current
    val pennies = moneyRawToPennies(raw)
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            if (type == GoalType.DEBT) "How much do you owe?" else "What's your target?",
            style = GlideType.onboardingHeadline.copy(color = glide.text),
        )
        Spacer(Modifier.height(20.dp))
        CurrencyChips(currency, onCurrency)
        Spacer(Modifier.height(16.dp))
        Text(
            "${currencySymbol(currency)}${moneyRawDisplay(raw, currency)}",
            style = GlideType.heroCounter.copy(color = if (pennies > 0) glide.text else glide.muted),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        NumberKeypad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            decimalKey = if (decimals > 0) currencyDecimalSeparator(currency) else null,
            onDecimal = onDecimal,
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton("Continue", onNext, enabled = pennies > 0)
    }
}

@Composable
private fun PaceStep(
    currency: String, raw: String, decimals: Int, finishLabel: String,
    onDigit: (Char) -> Unit, onDecimal: () -> Unit, onBackspace: () -> Unit, onFinish: () -> Unit,
) {
    val glide = LocalGlide.current
    val pennies = moneyRawToPennies(raw)
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Monthly pace", style = GlideType.onboardingHeadline.copy(color = glide.text))
        Text(
            "Optional — it powers your projected finish date.",
            style = GlideType.body.copy(color = glide.muted),
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(Modifier.height(20.dp))
        Text(
            if (raw.isEmpty()) "${currencySymbol(currency)}0" else "${currencySymbol(currency)}${moneyRawDisplay(raw, currency)}/mo",
            style = GlideType.heroCounter.copy(color = if (pennies > 0) glide.text else glide.muted),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.weight(1f))
        NumberKeypad(
            onDigit = onDigit,
            onBackspace = onBackspace,
            decimalKey = if (decimals > 0) currencyDecimalSeparator(currency) else null,
            onDecimal = onDecimal,
        )
        Spacer(Modifier.height(12.dp))
        PrimaryButton(finishLabel, onFinish)
        Spacer(Modifier.height(4.dp))
        Text(
            "Skip for now",
            style = GlideType.body.copy(color = glide.muted),
            modifier = Modifier.fillMaxWidth().clickable(onClick = onFinish).padding(8.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CurrencyChips(selected: String, onSelect: (String) -> Unit) {
    val glide = LocalGlide.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Currency.entries.take(4).forEach { c ->
            val isSel = c.code == selected
            Box(
                modifier = Modifier
                    .clip(GlideShapes.chip)
                    .background(if (isSel) glide.accentSoft else glide.surface)
                    .border(1.dp, if (isSel) glide.accent else glide.line, GlideShapes.chip)
                    .clickable { onSelect(c.code) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text("${c.symbol} ${c.code}", style = GlideType.caption.copy(color = if (isSel) glide.accent else glide.muted))
            }
        }
    }
}
