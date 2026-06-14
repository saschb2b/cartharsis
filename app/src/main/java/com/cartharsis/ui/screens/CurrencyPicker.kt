package com.cartharsis.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cartharsis.data.Currency

/**
 * Currency selection, shared by onboarding and the Account screen. A compact
 * field shows the current pick; tapping opens a bottom-sheet list of every
 * currency (symbol, code, full name), the chosen one checked.
 *
 * It used to be a chip per currency in a FlowRow, which wrapped into a wall once
 * past a handful — segmented chips are a 2–6 option pattern. A field-plus-sheet
 * is the pattern for a set that keeps growing, and shows the full name a bare
 * "₩ KRW" chip never could.
 */
@Composable
internal fun CurrencySelector(selected: Currency, onSelect: (Currency) -> Unit) {
    var open by remember { mutableStateOf(false) }
    CurrencyField(selected = selected, onClick = { open = true })
    if (open) {
        CurrencyPickerSheet(
            selected = selected,
            onSelect = {
                onSelect(it)
                open = false
            },
            onDismiss = { open = false },
        )
    }
}

/** The compact trigger: current symbol, code, and name, with a dropdown caret. */
@Composable
private fun CurrencyField(selected: Currency, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clearAndSetSemantics {
                contentDescription =
                    "Currency: ${selected.code}, ${selected.displayName}. Tap to change."
            },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            CurrencyGlyph(selected.symbol, highlighted = true)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = selected.code,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = selected.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "▾",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(selected: Currency, onSelect: (Currency) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        CurrencyPickerContent(selected = selected, onSelect = onSelect)
    }
}

/** The sheet body — also rendered on its own in previews (sheets don't preview). */
@Composable
internal fun CurrencyPickerContent(selected: Currency, onSelect: (Currency) -> Unit) {
    Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
        Text(
            text = "Choose your currency",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 2.dp),
        )
        Text(
            text = "Every price in the app shows in it.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )
        Currency.entries.forEach { currency ->
            CurrencyRow(
                currency = currency,
                selected = currency == selected,
                onClick = { onSelect(currency) },
            )
        }
    }
}

@Composable
private fun CurrencyRow(currency: Currency, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
    ) {
        CurrencyGlyph(currency.symbol, highlighted = selected)
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(
                text = currency.code,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            )
            Text(
                text = currency.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (selected) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

/** The symbol in a rounded tinted square — one fixed size whatever its length. */
@Composable
private fun CurrencyGlyph(symbol: String, highlighted: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (highlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        },
        modifier = Modifier.size(40.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
        }
    }
}
