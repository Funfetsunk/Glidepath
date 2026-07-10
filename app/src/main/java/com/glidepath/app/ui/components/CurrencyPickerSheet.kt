package com.glidepath.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.glidepath.app.domain.model.Currency
import com.glidepath.app.ui.theme.GlideShapes
import com.glidepath.app.ui.theme.GlideType
import com.glidepath.app.ui.theme.LocalGlide

/** A tappable chip showing the selected currency (symbol + code), used to open the picker. */
@Composable
fun CurrencyChip(currencyCode: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val glide = LocalGlide.current
    val currency = Currency.fromCode(currencyCode)
    Row(
        modifier = modifier
            .clip(GlideShapes.chip)
            .background(glide.accentSoft)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("${currency.symbol} ${currency.code}", style = GlideType.body.copy(color = glide.accent), fontWeight = FontWeight.W700)
        Text(" ▾", style = GlideType.body.copy(color = glide.accent))
    }
}

/** Modal bottom sheet listing all supported currencies (§7.1). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPickerSheet(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val glide = LocalGlide.current
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = glide.surface,
        contentColor = glide.text,
    ) {
        Column(modifier = Modifier.navigationBarsPadding().padding(horizontal = 22.dp)) {
            Text("Currency", style = GlideType.screenTitle.copy(color = glide.text))
            Spacer(Modifier.height(12.dp))
            LazyColumn {
                items(Currency.entries) { currency ->
                    CurrencyRow(currency, selected == currency.code) {
                        onSelect(currency.code)
                        onDismiss()
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CurrencyRow(currency: Currency, selected: Boolean, onClick: () -> Unit) {
    val glide = LocalGlide.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(GlideShapes.tile)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(glide.accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            Text(currency.symbol, style = GlideType.body.copy(color = glide.accent), fontWeight = FontWeight.W700)
        }
        Spacer(Modifier.size(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(currency.label, style = GlideType.body.copy(color = glide.text))
            Text(currency.code, style = GlideType.secondary.copy(color = glide.muted))
        }
        if (selected) Text("✓", style = GlideType.body.copy(color = glide.accent), fontWeight = FontWeight.W700)
    }
}
