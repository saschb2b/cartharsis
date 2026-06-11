package com.cartharsis.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.JuicyOrange
import com.cartharsis.ui.theme.LemonYellow

/**
 * The card backs, one per game, each built the way its genre pillar built
 * theirs (studied, not copied — every mark here is invented):
 *
 * - Pokémon's back is a cobalt field of marbled cloud-swirls with one glossy,
 *   toy-like object dead center and a chunky outlined wordmark — identity
 *   through saturated primary contrast, organic texture, and a single
 *   dimensional emblem. Pocket Critters follows that recipe with its own
 *   colors and its own ball.
 * - Magic's back is a leather-bound spellbook cover: mottled brown parchment,
 *   a beaded border, an inner oval, a serif wordmark, and the five-color mana
 *   pentagon around a dark sphere — identity through antique bookbinding and
 *   an emblem that carries the game's lore. Manaforge does the same with its
 *   own five elements.
 * - Yu-Gi-Oh's back is textless near-black lacquer with one bronze spiral
 *   vortex in a beveled medallion — identity through restraint, implied
 *   rotation, and faked metalwork. Duelbound keeps the silence.
 *
 * All hand-rolled Canvas, like every other visual in the app.
 */
@Composable
internal fun GameCardBack(theme: PackTheme, modifier: Modifier = Modifier) {
    when (theme.game) {
        "critters" -> CrittersBack(modifier)
        else -> GenericBack(theme, modifier)
    }
}

// ---------------------------------------------------------------- Critters

private val CrittersCobalt = Color(0xFF1E63C8)
private val CrittersNavy = Color(0xFF0D2F73)
private val CrittersInk = Color(0xFF0A2452)
private val CrittersSwirl = Color(0xFF5FA8E8)
private val CrittersSwirlHi = Color(0xFF9CCBF2)
private val CrittersCream = Color(0xFFF6F1E7)

/**
 * The Pocket Critters back, in the Pokémon mold: marbled cobalt swirls, a
 * glossy critter ball with a specular highlight, the wordmark in heavy
 * yellow over a navy outline.
 */
@Composable
private fun CrittersBack(modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            // The cobalt field.
            drawRect(Brush.verticalGradient(listOf(CrittersCobalt, CrittersNavy)))
            // Marbled cloud-swirls: layered translucent arcs at pseudo-seeded
            // positions — organic enough to break flatness, calm enough to
            // sit behind the emblem.
            repeat(14) { i ->
                val cx = size.width * (0.08f + 0.84f * ((i * 37 + 13) % 100) / 100f)
                val cy = size.height * (0.05f + 0.9f * ((i * 53 + 29) % 100) / 100f)
                val r = size.width * (0.10f + 0.16f * ((i * 71 + 7) % 100) / 100f)
                val start = (i * 97f) % 360f
                val sweep = 150f + (i * 61) % 160f
                drawArc(
                    color = if (i % 3 == 0) CrittersSwirlHi else CrittersSwirl,
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(cx - r, cy - r),
                    size = Size(r * 2, r * 2),
                    alpha = if (i % 3 == 0) 0.10f else 0.14f,
                    style = Stroke(width = (8 + (i * 31) % 14).dp.toPx()),
                )
            }
            // Soft glow pooling behind the emblem.
            drawCircle(
                Brush.radialGradient(
                    listOf(CrittersSwirlHi.copy(alpha = 0.35f), Color.Transparent),
                    center = Offset(size.width / 2, size.height * 0.58f),
                    radius = size.width * 0.42f,
                ),
                radius = size.width * 0.42f,
                center = Offset(size.width / 2, size.height * 0.58f),
            )
            // The critter ball — glossy and dimensional, not a flat icon.
            drawCritterBall(center = Offset(size.width / 2, size.height * 0.58f), r = size.width * 0.21f)
            // A navy rim just inside the cut, like the printed edge.
            inset(this, 4.dp.toPx()) { rect ->
                drawRoundRect(
                    color = CrittersInk,
                    topLeft = rect.topLeft,
                    size = rect.size,
                    cornerRadius = CornerRadius(10.dp.toPx()),
                    style = Stroke(2.dp.toPx()),
                )
            }
        }
        Text(
            text = "POCKET CRITTERS",
            style = TextStyle(
                color = LemonYellow,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp,
                shadow = Shadow(color = CrittersInk, offset = Offset(0f, 6f), blurRadius = 1f),
            ),
            maxLines = 1,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp),
        )
    }
}

private fun DrawScope.drawCritterBall(center: Offset, r: Float) {
    // Shadowed underside grounds the ball on the field.
    drawCircle(Color.Black.copy(alpha = 0.30f), radius = r, center = center + Offset(0f, r * 0.12f))
    // Top hemisphere in the game's own pink-to-orange — homage to the
    // capture-ball trope, deliberately not the genre's red.
    drawArc(
        brush = Brush.linearGradient(
            listOf(HotPink, JuicyOrange),
            start = center - Offset(r, r * 0.6f),
            end = center + Offset(r, -r * 0.1f),
        ),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = center - Offset(r, r),
        size = Size(r * 2, r * 2),
    )
    // Cream lower hemisphere.
    drawArc(
        color = CrittersCream,
        startAngle = 0f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = center - Offset(r, r),
        size = Size(r * 2, r * 2),
    )
    // The band, and the app's own heart as the latch button.
    drawRect(
        color = CrittersInk,
        topLeft = Offset(center.x - r, center.y - r * 0.085f),
        size = Size(r * 2, r * 0.17f),
    )
    drawCircle(CrittersInk, radius = r * 0.30f, center = center)
    drawHeart(center = center, s = r * 0.20f, color = CrittersCream)
    // Rim, then the specular gleam that makes it read as lacquered plastic.
    drawCircle(CrittersInk, radius = r, center = center, style = Stroke(r * 0.07f))
    drawOval(
        color = Color.White.copy(alpha = 0.55f),
        topLeft = center + Offset(-r * 0.62f, -r * 0.74f),
        size = Size(r * 0.52f, r * 0.30f),
    )
    drawOval(
        color = Color.White.copy(alpha = 0.30f),
        topLeft = center + Offset(-r * 0.78f, -r * 0.42f),
        size = Size(r * 0.16f, r * 0.10f),
    )
}

/** A filled heart — the app's mark, doing duty as the ball's latch. */
private fun DrawScope.drawHeart(center: Offset, s: Float, color: Color) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y + s * 0.55f)
        cubicTo(
            center.x - s * 1.15f, center.y - s * 0.25f,
            center.x - s * 0.5f, center.y - s * 0.95f,
            center.x, center.y - s * 0.35f,
        )
        cubicTo(
            center.x + s * 0.5f, center.y - s * 0.95f,
            center.x + s * 1.15f, center.y - s * 0.25f,
            center.x, center.y + s * 0.55f,
        )
        close()
    }
    drawPath(path, color)
}

// ----------------------------------------------------------------- shared

private inline fun inset(scope: DrawScope, px: Float, draw: (Rect) -> Unit) {
    draw(Rect(px, px, scope.size.width - px, scope.size.height - px))
}

// ----------------------------------------------- placeholder (to be replaced)

/** The pre-redesign generic back; each game's own back is replacing it. */
@Composable
private fun GenericBack(theme: PackTheme, modifier: Modifier = Modifier) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            drawRect(Brush.linearGradient(theme.wrapper))
            drawRect(Color.Black.copy(alpha = 0.22f))
            val c = Offset(size.width / 2, size.height / 2)
            val r = size.width * 0.25f
            drawCircle(Color.White.copy(alpha = 0.08f), radius = r, center = c)
            drawCircle(Color.White.copy(alpha = 0.4f), radius = r, center = c, style = Stroke(2.dp.toPx()))
            drawCircle(Color.White.copy(alpha = 0.22f), radius = r * 0.78f, center = c, style = Stroke(1.dp.toPx()))
        }
        Text(
            text = theme.emoji,
            fontSize = 48.sp,
            modifier = Modifier.align(Alignment.Center),
        )
        Text(
            text = theme.title.uppercase(),
            style = TextStyle(
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            ),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 48.dp),
        )
    }
}
