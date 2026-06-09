package com.cartharsis.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.MintGreen
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class Particle(random: Random) {
    val startX = random.nextFloat()
    val horizontalDrift = (random.nextFloat() - 0.5f) * 0.6f
    val fallSpeed = 0.6f + random.nextFloat() * 0.9f
    val size = 8f + random.nextFloat() * 14f
    val spin = random.nextFloat() * 360f
    val spinSpeed = (random.nextFloat() - 0.5f) * 720f
    val color = listOf(HotPink, ElectricPurple, JuicyOrange, MintGreen, Color.Yellow)[random.nextInt(5)]
    val delay = random.nextFloat() * 0.3f
}

/**
 * Hand-rolled confetti rain — the order-placed dopamine spike, no dependencies.
 * Scale [particleCount] with the size of the moment; sameness cheapens it.
 */
@Composable
fun ConfettiOverlay(
    modifier: Modifier = Modifier,
    durationMillis: Int = 3200,
    particleCount: Int = 90,
) {
    val particles = remember { List(particleCount) { Particle(Random(it)) } }
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis, easing = LinearEasing))
    }
    Canvas(modifier = modifier) {
        val t = progress.value
        particles.forEach { p ->
            val local = ((t - p.delay) / (1f - p.delay)).coerceIn(0f, 1f)
            if (local <= 0f || local >= 1f) return@forEach
            val x = (p.startX + p.horizontalDrift * local) * size.width +
                sin(local * 12f + p.spin) * 18f
            val y = local * p.fallSpeed * (size.height + 100f) - 50f
            val angle = p.spin + p.spinSpeed * local
            val alpha = if (local > 0.8f) (1f - local) * 5f else 1f
            rotate(degrees = angle, pivot = Offset(x, y)) {
                drawRect(
                    color = p.color.copy(alpha = alpha.coerceIn(0f, 1f)),
                    topLeft = Offset(x - p.size / 2, y - p.size / 4),
                    size = Size(p.size, p.size / 2 + p.size / 2 * cos(angle / 57f)),
                )
            }
        }
    }
}
