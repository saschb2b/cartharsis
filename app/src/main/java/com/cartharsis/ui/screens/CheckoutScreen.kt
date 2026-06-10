package com.cartharsis.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ShopViewModel
import com.cartharsis.data.CartItem
import com.cartharsis.data.formatPrice
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.MintGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface CheckoutPhase {
    data object Form : CheckoutPhase
    data class Processing(val totalCents: Long) : CheckoutPhase
    data class Success(val orderId: Int) : CheckoutPhase
}

/**
 * The fake bank takes a moment; shown labor is what makes nothing feel
 * earned. The ceremony plays it straight at the full fake price — the
 * $0.00 truth is the success screen's punchline, never an opening spoiler.
 */
private fun processingLines(totalCents: Long) = listOf(
    "Contacting Imagination Express…",
    "Verifying available balance…",
    "Charging ${formatPrice(totalCents)}…",
)

/** The last line read is the one remembered — rotate the punchline. */
private val successLines = listOf(
    "Your card was charged \$0.00. It didn't notice.",
    "Everything you wanted, nothing you'll owe.",
    "The bank has confirmed: nothing happened.",
    "Receipt available wherever you imagine it.",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: ShopViewModel,
    onTrackOrder: (Int) -> Unit,
    onBack: () -> Unit,
    onKeepShopping: () -> Unit = onBack,
) {
    val cart by viewModel.cart.collectAsState()
    var phase by remember { mutableStateOf<CheckoutPhase>(CheckoutPhase.Form) }
    val haptics = LocalHapticFeedback.current

    when (val p = phase) {
        is CheckoutPhase.Form -> {
            if (cart.isEmpty()) {
                LaunchedEffect(Unit) { onBack() }
                return
            }
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Checkout") },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Text(
                                    "←",
                                    fontSize = 22.sp,
                                    modifier = Modifier.clearAndSetSemantics { contentDescription = "Back" },
                                )
                            }
                        },
                    )
                },
                bottomBar = {
                    val total = viewModel.cartTotalCents(cart)
                    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                        HoldToPlaceOrderButton(
                            totalCents = total,
                            onPlaced = { phase = CheckoutPhase.Processing(total) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                        )
                    }
                },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CheckoutSection("Deliver to") {
                        Text("Your Imagination", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Apt ∞, Anticipation Street, Dopamine City",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "🚚 $DELIVERY_PROMISE",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }

                    Text(
                        text = "Pay with",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    ImaginationCard()

                    Text(
                        text = "Order summary",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            cart.forEach { item -> SummaryLine(item) }
                            HorizontalDivider(Modifier.padding(vertical = 2.dp))
                            // No "$0.00" reveal here: the checkout plays it
                            // straight so the ceremony has stakes. The truth
                            // is the success screen's punchline.
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Spacer(Modifier.weight(1f))
                                Text(
                                    text = formatPrice(viewModel.cartTotalCents(cart)),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
            }
        }

        is CheckoutPhase.Processing -> {
            val lines = remember(p.totalCents) { processingLines(p.totalCents) }
            var lineIndex by remember { mutableIntStateOf(0) }
            var showCheck by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            // You can't back out mid-payment, even of a payment of nothing.
            BackHandler { }
            LaunchedEffect(Unit) {
                lines.indices.drop(1).forEach {
                    delay(600)
                    lineIndex = it
                }
                delay(600)
                showCheck = true
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (!showCheck) {
                    CircularProgressIndicator()
                    AnimatedContent(targetState = lineIndex, label = "processing") { index ->
                        Text(
                            text = lines[index],
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    }
                } else {
                    AnimatedCheckmark(
                        onDrawn = {
                            // Haptic fires at the exact moment the stroke completes,
                            // then one breath before the celebration.
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            scope.launch {
                                delay(250)
                                phase = CheckoutPhase.Success(viewModel.placeOrder())
                            }
                        },
                    )
                    Text(
                        text = "Payment complete",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }

        is CheckoutPhase.Success -> {
            // Back from the confirmation goes shopping, not to a dead cart.
            BackHandler { onKeepShopping() }
            SuccessScreen(
                viewModel = viewModel,
                orderId = p.orderId,
                onTrackOrder = onTrackOrder,
                onKeepShopping = onKeepShopping,
            )
        }
    }
}

/**
 * The payment ceremony: you don't tap a fake purchase, you commit to it —
 * at the full fake price, because a commitment to $0.00 carries no weight.
 * Hold ~0.9s while the fill rises with haptic ticks; release early and it
 * springs back. Screen readers get a plain activate action instead.
 */
@Composable
private fun HoldToPlaceOrderButton(totalCents: Long, onPlaced: () -> Unit, modifier: Modifier = Modifier) {
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }
    var done by remember { mutableStateOf(false) }
    // A tap-trained thumb will tap; tell it what the button actually wants.
    var showHoldHint by remember { mutableStateOf(false) }
    LaunchedEffect(showHoldHint) {
        if (showHoldHint) {
            delay(1_400)
            showHoldHint = false
        }
    }

    fun complete() {
        if (!done) {
            done = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            onPlaced()
        }
    }

    val labelColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (progress.value > 0.4f) Color.White else MaterialTheme.colorScheme.primary,
        animationSpec = tween(150),
        label = "labelColor",
    )

    Box(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(27.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val fill = scope.launch {
                            var lastTick = 0
                            progress.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = (900 * (1f - progress.value)).toInt(),
                                    easing = LinearEasing,
                                ),
                            ) {
                                val tick = (value * 4).toInt()
                                if (tick > lastTick && value < 1f) {
                                    lastTick = tick
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                            complete()
                        }
                        tryAwaitRelease()
                        if (!done) {
                            fill.cancel()
                            if (progress.value < 0.5f) showHoldHint = true
                            scope.launch {
                                progress.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                            }
                        }
                    },
                )
            }
            .semantics(mergeDescendants = true) {
                role = Role.Button
                onClick(label = "Pay ${formatPrice(totalCents)}") {
                    complete()
                    true
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.value)
                .background(Brush.horizontalGradient(listOf(HotPink, ElectricPurple)))
                .align(Alignment.CenterStart),
        )
        Crossfade(targetState = showHoldHint, label = "holdHint") { hinting ->
            Text(
                text = if (hinting) {
                    "Keep holding — commitment takes a second"
                } else {
                    "Hold to pay ${formatPrice(totalCents)}"
                },
                fontWeight = FontWeight.Bold,
                color = labelColor,
            )
        }
    }
}

/** Apple-Pay-style resolution: a checkmark drawn as a stroke, not stamped. */
@Composable
private fun AnimatedCheckmark(onDrawn: () -> Unit) {
    val t = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        t.animateTo(1f, tween(durationMillis = 380, easing = FastOutSlowInEasing))
        onDrawn()
    }
    Canvas(
        Modifier
            .size(96.dp)
            .clearAndSetSemantics { contentDescription = "Payment of zero dollars complete" },
    ) {
        val stroke = Stroke(width = 10f, cap = StrokeCap.Round)
        drawArc(
            color = MintGreen,
            startAngle = -90f,
            sweepAngle = 360f * (t.value * 1.5f).coerceAtMost(1f),
            useCenter = false,
            style = stroke,
        )
        val checkT = ((t.value - 0.35f) / 0.65f).coerceIn(0f, 1f)
        if (checkT > 0f) {
            val check = Path().apply {
                moveTo(size.width * 0.29f, size.height * 0.53f)
                lineTo(size.width * 0.45f, size.height * 0.68f)
                lineTo(size.width * 0.72f, size.height * 0.36f)
            }
            val measure = PathMeasure().apply { setPath(check, false) }
            val partial = Path()
            measure.getSegment(0f, measure.length * checkT, partial, true)
            drawPath(partial, MintGreen, style = stroke)
        }
    }
}

/** Sequenced celebration: confetti → headline springs in → details breathe up. */
@Composable
private fun SuccessScreen(
    viewModel: ShopViewModel,
    orderId: Int,
    onTrackOrder: (Int) -> Unit,
    onKeepShopping: () -> Unit,
) {
    val orders by viewModel.orders.collectAsState()
    val stats by viewModel.lifetimeStats.collectAsState()
    val order = orders.firstOrNull { it.id == orderId }
    val punchline = remember { successLines.random() }
    // First order ever and every tenth get the bigger sky.
    val milestone = stats.ordersPlaced == 1 || (stats.ordersPlaced > 0 && stats.ordersPlaced % 10 == 0)

    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val heroScale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heroScale",
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("🎉", fontSize = 84.sp, modifier = Modifier.scale(heroScale))
            Text(
                text = if (milestone && stats.ordersPlaced == 1) "First order placed!" else "Order placed!",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.scale(heroScale),
            )
            Text(
                text = "Order #$orderId · arriving in about a minute",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            order?.let {
                Text(
                    text = "${animatedDollars(it.totalCents, delayMillis = 400)} stays yours",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MintGreen,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
            Text(
                text = punchline,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            AnimatedVisibility(
                visible = revealed,
                enter = fadeIn(tween(durationMillis = 300, delayMillis = 450)),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = { onTrackOrder(orderId) },
                        modifier = Modifier.padding(top = 24.dp).height(48.dp),
                    ) {
                        Text("Track your nothing 🚚", fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onKeepShopping, modifier = Modifier.padding(top = 4.dp)) {
                        Text("Keep shopping")
                    }
                }
            }
        }
        ConfettiOverlay(
            Modifier.fillMaxSize(),
            particleCount = if (milestone) 170 else 90,
        )
    }
}

@Composable
private fun CheckoutSection(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Column(Modifier.fillMaxWidth().padding(14.dp)) { content() }
        }
    }
}

@Composable
private fun SummaryLine(item: CartItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))) {
            EmojiHero(
                emoji = item.product.emoji,
                modifier = Modifier.fillMaxSize(),
                fontSize = 18,
                seed = item.product.id,
            )
        }
        Text(
            text = item.product.name + if (item.quantity > 1) "  ×${item.quantity}" else "",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
        )
        Text(
            text = formatPrice(item.totalCents),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * The "payment method", drawn with real card anatomy — ISO 1.586:1 ratio,
 * EMV chip, contactless mark, embossed digit groups, holder and expiry
 * blocks — because the ceremony only works if the props look real.
 * Balance: infinite, by definition.
 */
@Composable
private fun ImaginationCard() {
    val embossed = Shadow(color = Color.Black.copy(alpha = 0.35f), offset = Offset(0f, 3f), blurRadius = 4f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.586f)
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(ElectricPurple, HotPink))),
    ) {
        // A diagonal sheen so the surface reads as plastic, not as a fill.
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colorStops = arrayOf(
                            0f to Color.White.copy(alpha = 0.18f),
                            0.45f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.12f),
                        ),
                    ),
                ),
        )
        Column(Modifier.fillMaxSize().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "IMAGINATION EXPRESS",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                )
                Spacer(Modifier.weight(1f))
                ContactlessMark()
            }
            Spacer(Modifier.weight(1f))
            EmvChip()
            Spacer(Modifier.height(10.dp))
            Text(
                "5310  0000  0000  0000",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.5.sp,
                    shadow = embossed,
                ),
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                CardField(label = "CARD HOLDER", value = "YOUR IMAGINATION", embossed)
                Spacer(Modifier.width(20.dp))
                CardField(label = "VALID THRU", value = "∞∞/∞∞", embossed)
                Spacer(Modifier.weight(1f))
                Text(
                    "∞",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge.copy(shadow = embossed),
                )
            }
        }
    }
}

@Composable
private fun CardField(label: String, value: String, shadow: Shadow) {
    Column {
        Text(label, color = Color.White.copy(alpha = 0.65f), fontSize = 8.sp, letterSpacing = 1.sp)
        Text(
            text = value,
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = FontFamily.Monospace,
                shadow = shadow,
            ),
        )
    }
}

/** Gold contact chip, simplified to the pattern everyone recognizes. */
@Composable
private fun EmvChip() {
    Canvas(Modifier.size(width = 42.dp, height = 31.dp)) {
        drawRoundRect(
            brush = Brush.linearGradient(listOf(Color(0xFFEED787), Color(0xFFC49A45))),
            cornerRadius = CornerRadius(6.dp.toPx()),
        )
        val line = Color(0xFF8A6B2F)
        val strokeWidth = 1.4.dp.toPx()
        val w = size.width
        val h = size.height
        drawLine(line, Offset(0f, h / 2), Offset(w, h / 2), strokeWidth)
        drawLine(line, Offset(w * 0.33f, 0f), Offset(w * 0.33f, h), strokeWidth)
        drawLine(line, Offset(w * 0.67f, 0f), Offset(w * 0.67f, h), strokeWidth)
        drawRoundRect(
            color = line,
            topLeft = Offset(w * 0.33f, h * 0.28f),
            size = Size(w * 0.34f, h * 0.44f),
            cornerRadius = CornerRadius(3.dp.toPx()),
            style = Stroke(strokeWidth),
        )
    }
}

/** The radio-waves mark; ours broadcasts nothing, contactlessly. */
@Composable
private fun ContactlessMark() {
    Canvas(Modifier.size(22.dp)) {
        val color = Color.White.copy(alpha = 0.85f)
        val stroke = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        val cx = size.width * 0.18f
        val cy = size.height * 0.5f
        listOf(0.28f, 0.52f, 0.76f).forEach { f ->
            val r = size.width * f
            drawArc(
                color = color,
                startAngle = -38f,
                sweepAngle = 76f,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = Size(2 * r, 2 * r),
                style = stroke,
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun ImaginationCardPreview() {
    com.cartharsis.ui.theme.CartharsisTheme {
        ImaginationCard()
    }
}
