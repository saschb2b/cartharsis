package com.cartharsis.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.Currency
import com.cartharsis.data.ProfileStore
import com.cartharsis.ui.theme.Motion

private const val STEP_INTRO_WHAT = 0
private const val STEP_INTRO_WHY = 1
private const val STEP_ACCOUNT = 2
private const val STEP_ADDRESS = 3
private const val STEP_PAYMENT = 4

/**
 * The signup every shop makes you do, played completely straight: account,
 * delivery address, payment method. Per the checkout dramaturgy the flow
 * looks real; the frame stays honest (no email, no password, this phone
 * only). The name entered here ends up on the Imagination Express card.
 *
 * Every step shares one frame ([StepScaffold]): a fixed progress zone up top
 * and a primary CTA anchored to the bottom, so the button lands at the same
 * height on each screen instead of chasing the content (Material's "fixed
 * button + pagination" onboarding rule).
 */
@Composable
fun OnboardingScreen(viewModel: ShopViewModel) {
    val haptics = LocalHapticFeedback.current
    var step by rememberSaveable { mutableIntStateOf(STEP_INTRO_WHAT) }
    var name by rememberSaveable { mutableStateOf("") }
    var street by rememberSaveable { mutableStateOf(ProfileStore.DEFAULT_STREET) }
    var city by rememberSaveable { mutableStateOf(ProfileStore.DEFAULT_CITY) }
    // Picked on the payment step; setCurrency applies it app-wide and persists.
    val currency by viewModel.currency.collectAsState()

    BackHandler(enabled = step > STEP_INTRO_WHAT) { step-- }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .padding(24.dp),
    ) {
        // A fixed-height header: a progress indicator (2 dots for the intro
        // pitch, 3 for the setup steps) and a Skip on the intro slides, so the
        // pitch is never a trap. Stays 48dp tall so content below never jumps.
        val onIntro = step <= STEP_INTRO_WHY
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProgressDots(
                current = step,
                steps = if (onIntro) STEP_INTRO_WHAT..STEP_INTRO_WHY else STEP_ACCOUNT..STEP_PAYMENT,
            )
            Spacer(Modifier.weight(1f))
            if (onIntro) {
                TextButton(onClick = { step = STEP_ACCOUNT }) { Text("Skip") }
            }
        }
        AnimatedContent(
            targetState = step,
            label = "onboarding",
            transitionSpec = {
                val forward = targetState > initialState
                (
                    slideInHorizontally(Motion.spatial()) { if (forward) it / 3 else -it / 3 } +
                        fadeIn(Motion.effects())
                    ).togetherWith(
                    slideOutHorizontally(Motion.spatial()) { if (forward) -it / 3 else it / 3 } +
                        fadeOut(Motion.effects()),
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // Swipe between the intro slides (the carousel users expect on
                // Android); the form steps stay button-only so a swipe never
                // skips past a field that still needs filling.
                .then(
                    if (onIntro) {
                        Modifier.pointerInput(step) {
                            var dragged = 0f
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (dragged < -40f && step < STEP_INTRO_WHY) {
                                        step++
                                    } else if (dragged > 40f && step > STEP_INTRO_WHAT) {
                                        step--
                                    }
                                    dragged = 0f
                                },
                            ) { _, dragAmount -> dragged += dragAmount }
                        }
                    } else {
                        Modifier
                    },
                ),
        ) { current ->
            when (current) {
                STEP_INTRO_WHAT -> IntroWhatStep(onNext = { step = STEP_INTRO_WHY })
                STEP_INTRO_WHY -> IntroWhyStep(onCreateAccount = { step = STEP_ACCOUNT })
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
                    currency = currency,
                    onCurrencyChange = viewModel::setCurrency,
                    onAddCard = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.completeOnboarding(name, street, city)
                    },
                )
            }
        }
    }
}

/**
 * The shared step frame: a content area that fills the available height (and
 * scrolls when a keyboard crowds it), an optional centered footnote, and the
 * primary CTA pinned to the bottom as the last element — so the button is at
 * the same height on every step.
 */
@Composable
private fun StepScaffold(
    primaryLabel: String,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    centerContent: Boolean = false,
    footnote: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        val contentModifier = Modifier.weight(1f).fillMaxWidth()
        Column(
            // The welcome hero centers in the space; form steps top-align and
            // scroll so a raised keyboard never traps a field.
            modifier = if (centerContent) {
                contentModifier
            } else {
                contentModifier.verticalScroll(rememberScrollState())
            },
            verticalArrangement = if (centerContent) Arrangement.Center else Arrangement.Top,
            horizontalAlignment = if (centerContent) Alignment.CenterHorizontally else Alignment.Start,
            content = content,
        )
        footnote?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 12.dp),
            )
        }
        Button(
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(primaryLabel)
        }
    }
}

/** Title + subtitle in the same style and spacing on every form step. */
@Composable
private fun StepHeader(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.headlineSmall)
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp),
    )
}

/** First slide, value-first: what Cartharsis actually is, before any setup. */
@Composable
internal fun IntroWhatStep(onNext: () -> Unit) {
    StepScaffold(
        primaryLabel = "Why would I want that?",
        onPrimary = onNext,
        centerContent = true,
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
        Text(
            text = "A real-feeling store to wander: fill a cart, check out, and watch " +
                "a courier head to your door. The twist is it's all imaginary. " +
                "Nothing is charged, nothing ships, nothing ever arrives.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 20.dp),
        )
    }
}

/** Second slide: why that helps, then straight into creating the account. */
@Composable
internal fun IntroWhyStep(onCreateAccount: () -> Unit) {
    StepScaffold(
        primaryLabel = "Create your account",
        onPrimary = onCreateAccount,
        centerContent = true,
        footnote = "Takes 30 seconds. No email, no password,\nand nothing real will ever happen.",
    ) {
        Text("🧘", fontSize = 72.sp)
        Text(
            text = "All the dopamine.\nNone of the bill.",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "Most of the buzz of shopping is the wanting, not the owning. " +
                "Cartharsis keeps the anticipation and bins the credit-card bill. " +
                "A calm place to want things freely and watch your money stay yours.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
internal fun AccountStep(name: String, onNameChange: (String) -> Unit, onContinue: () -> Unit) {
    StepScaffold(
        primaryLabel = "Continue",
        onPrimary = onContinue,
        primaryEnabled = name.isNotBlank(),
    ) {
        StepHeader("Create your account", "Lives on this phone and nowhere else.")
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
        )
    }
}

@Composable
internal fun AddressStep(
    street: String,
    onStreetChange: (String) -> Unit,
    city: String,
    onCityChange: (String) -> Unit,
    onContinue: () -> Unit,
) {
    StepScaffold(
        primaryLabel = "Continue",
        onPrimary = onContinue,
        primaryEnabled = street.isNotBlank() && city.isNotBlank(),
    ) {
        StepHeader("Add a delivery address", "Our courier can find anywhere you can imagine.")
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
    }
}

@Composable
internal fun PaymentStep(
    name: String,
    currency: Currency,
    onCurrencyChange: (Currency) -> Unit,
    onAddCard: () -> Unit,
) {
    StepScaffold(
        primaryLabel = "Add card and start shopping",
        onPrimary = onAddCard,
        footnote = "It's the only payment method. There was never a choice.",
    ) {
        StepHeader("Add a payment method", "You've been pre-approved for the Imagination Express.")
        ImaginationCard(cardHolder = name)
        Text(
            text = "Credit limit: ∞ · APR: 0.00% · Annual fee: imaginary",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 10.dp),
        )
        Text(
            text = "Currency",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp),
        )
        CurrencySelector(selected = currency, onSelect = onCurrencyChange)
    }
}

/** Filled-to-current dots over an arbitrary step range (the intro pitch or the
 *  setup steps), announced as "Step X of Y". */
@Composable
private fun ProgressDots(current: Int, steps: IntRange, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "Step ${current - steps.first + 1} of ${steps.count()}"
        },
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        steps.forEach { stepIndex ->
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
