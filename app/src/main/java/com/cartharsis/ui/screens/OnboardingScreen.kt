package com.cartharsis.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.ProfileStore

private const val STEP_WELCOME = 0
private const val STEP_ACCOUNT = 1
private const val STEP_ADDRESS = 2
private const val STEP_PAYMENT = 3

/**
 * The signup every shop makes you do, played completely straight: account,
 * delivery address, payment method. Per the checkout dramaturgy the flow
 * looks real; the frame stays honest (no email, no password, this phone
 * only). The name entered here ends up on the Imagination Express card.
 */
@Composable
fun OnboardingScreen(viewModel: ShopViewModel) {
    val haptics = LocalHapticFeedback.current
    var step by rememberSaveable { mutableIntStateOf(STEP_WELCOME) }
    var name by rememberSaveable { mutableStateOf("") }
    var street by rememberSaveable { mutableStateOf(ProfileStore.DEFAULT_STREET) }
    var city by rememberSaveable { mutableStateOf(ProfileStore.DEFAULT_CITY) }

    BackHandler(enabled = step > STEP_WELCOME) { step-- }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        if (step > STEP_WELCOME) {
            StepDots(current = step, modifier = Modifier.padding(top = 24.dp))
        }
        AnimatedContent(
            targetState = step,
            label = "onboarding",
            transitionSpec = {
                val forward = targetState > initialState
                (slideInHorizontally { if (forward) it / 3 else -it / 3 } + fadeIn())
                    .togetherWith(slideOutHorizontally { if (forward) -it / 3 else it / 3 } + fadeOut())
            },
            modifier = Modifier.fillMaxWidth(),
        ) { current ->
            when (current) {
                STEP_WELCOME -> WelcomeStep(onStart = { step = STEP_ACCOUNT })
                STEP_ACCOUNT -> AccountStep(
                    name = name,
                    onNameChange = { name = it },
                    onContinue = { step = STEP_ADDRESS },
                )
                STEP_ADDRESS -> AddressStep(
                    street = street,
                    onStreetChange = { street = it },
                    city = city,
                    onCityChange = { city = it },
                    onContinue = { step = STEP_PAYMENT },
                )
                else -> PaymentStep(
                    name = name,
                    onAddCard = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.completeOnboarding(name, street, city)
                    },
                )
            }
        }
    }
}

@Composable
private fun WelcomeStep(onStart: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 96.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🛒", fontSize = 72.sp)
        Text(
            text = "Cartharsis",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Add to cart. Feel better. Buy nothing.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Spacer(Modifier.height(48.dp))
        Button(
            onClick = onStart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Create your account")
        }
        Text(
            text = "Takes 30 seconds. No email, no password,\nand nothing real will ever happen.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun AccountStep(name: String, onNameChange: (String) -> Unit, onContinue: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 32.dp)) {
        Text("Create your account", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Lives on this phone and nowhere else.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            enabled = name.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun AddressStep(
    street: String,
    onStreetChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(top = 32.dp)) {
        Text("Add a delivery address", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "Our courier can find anywhere you can imagine.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )
        OutlinedTextField(
            value = street,
            onValueChange = onStreetChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Street") },
            singleLine = true,
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = city,
            onValueChange = onCityChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("City") },
            singleLine = true,
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onContinue,
            enabled = street.isNotBlank() && city.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Continue")
        }
    }
}

@Composable
private fun PaymentStep(name: String, onAddCard: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 32.dp)) {
        Text("Add a payment method", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "You've been pre-approved for the Imagination Express.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
        )
        ImaginationCard(cardHolder = name)
        Text(
            text = "Credit limit: ∞ · APR: 0.00% · Annual fee: imaginary",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onAddCard,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text("Add card and start shopping")
        }
        TextButton(
            onClick = onAddCard,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) {
            Text("It's the only payment method. There was never a choice.")
        }
    }
}

@Composable
private fun StepDots(current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = "Step $current of 3" },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        (STEP_ACCOUNT..STEP_PAYMENT).forEach { stepIndex ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (stepIndex <= current) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ),
            )
        }
    }
}
