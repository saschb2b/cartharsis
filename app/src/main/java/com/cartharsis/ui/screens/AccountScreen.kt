package com.cartharsis.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.Currency
import com.cartharsis.data.ProfileStore
import com.cartharsis.ui.theme.LocalSavingsColor
import kotlinx.coroutines.delay

/**
 * The account area, reached from the avatar on Home. Everything the onboarding
 * collected is editable here (name, address, currency), plus room for more
 * settings to slot in under their own section headers. The currency applies
 * instantly the way a selector should; the text details take a Save, the way a
 * form should.
 */
@Composable
fun AccountScreen(viewModel: ShopViewModel, onBack: () -> Unit) {
    val profile by viewModel.profile.collectAsState()
    val currency by viewModel.currency.collectAsState()
    AccountContent(
        profile = profile,
        currency = currency,
        onSave = viewModel::updateProfile,
        onSelectCurrency = viewModel::setCurrency,
        onBack = onBack,
    )
}

@Composable
internal fun AccountContent(
    profile: ProfileStore.Profile?,
    currency: Currency,
    onSave: (String, String, String) -> Unit,
    onSelectCurrency: (Currency) -> Unit,
    onBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        NestedTopBar(onBack = onBack, title = "Account")
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            ProfileEditor(profile = profile, onSave = onSave)

            Column {
                SectionHeader("Currency")
                Text(
                    text = "Every price in the app, your way.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                )
                CurrencySelector(selected = currency, onSelect = onSelectCurrency)
            }

            Column {
                SectionHeader("Payment")
                Spacer(Modifier.height(12.dp))
                ImaginationCard(cardHolder = profile?.name.orEmpty())
                Text(
                    text = "Credit limit: ∞ · APR: 0.00% · Annual fee: imaginary",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            Text(
                text = "Cartharsis keeps everything on this phone. No account, " +
                    "no server, nothing to leak — there was never anything real to lose.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProfileEditor(profile: ProfileStore.Profile?, onSave: (String, String, String) -> Unit) {
    // Re-seed the fields whenever the saved profile changes (initial load, or
    // right after a save), so "dirty" tracks against the persisted values.
    var name by remember(profile) { mutableStateOf(profile?.name.orEmpty()) }
    var street by remember(profile) { mutableStateOf(profile?.street.orEmpty()) }
    var city by remember(profile) { mutableStateOf(profile?.city.orEmpty()) }
    val haptics = LocalHapticFeedback.current
    var justSaved by remember { mutableStateOf(false) }
    LaunchedEffect(justSaved) {
        if (justSaved) {
            delay(2_000)
            justSaved = false
        }
    }

    val valid = name.isNotBlank() && street.isNotBlank() && city.isNotBlank()
    val dirty = profile != null &&
        (name != profile.name || street != profile.street || city != profile.city)

    Column {
        SectionHeader("Your details")
        Text(
            text = "Lives on this phone and nowhere else.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = street,
            onValueChange = { street = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Street") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("City") },
            singleLine = true,
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSave(name, street, city)
                    justSaved = true
                },
                enabled = dirty && valid,
            ) {
                Text("Save changes")
            }
            AnimatedVisibility(visible = justSaved && !dirty) {
                Text(
                    text = "Saved ✓",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = LocalSavingsColor.current,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
}
