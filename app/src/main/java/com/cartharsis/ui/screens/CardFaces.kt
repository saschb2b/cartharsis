package com.cartharsis.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cartharsis.data.CardPull
import com.cartharsis.data.FakeCatalog
import com.cartharsis.ui.theme.ElectricPurple
import com.cartharsis.ui.theme.HotPink
import com.cartharsis.ui.theme.LemonYellow
import com.cartharsis.ui.theme.SkyBlue
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The card faces, one layout per game, mirroring how the three genre
 * pillars actually structure their fronts (the backs in CardBacks.kt set
 * the identity; the faces carry it through):
 *
 * - Pokémon fronts are element-tinted and information-light: stage → name →
 *   HP across the header, gilt-framed art, an italic species strip, flavor
 *   in its own box. CrittersFace follows that skeleton.
 * - Yu-Gi-Oh fronts encode the card's kind in the frame color (monster /
 *   spell / trap), stack a name plate, level stars, heavy-framed art, the
 *   bracketed type line, and close with ATK/DEF over a rule. DuelboundFace
 *   keeps that order.
 * - Magic fronts are parchment plates: title banner with the cost, art,
 *   a type banner wearing the set symbol (its color IS the rarity), a roomy
 *   text box, and the P/T box overlapping the corner. ManaforgeFace builds
 *   those plates.
 *
 * Faces are print-constant: like the backs, they keep their own ink and
 * paper regardless of app theme — a card is a printed object.
 */
@Composable
internal fun GameCardFace(card: CardPull, theme: PackTheme, holo: Boolean) {
    when (theme.game) {
        "critters" -> CrittersFace(card, holo)
        else -> ClassicFace(card, theme, holo)
    }
}

/** The rarity's accent: the gem dot in the name bar and the footer tint. */
@Composable
private fun rarityAccent(rarity: String): Color = when {
    rarity.startsWith("Common") -> Color(0xFFB9B2A6)
    rarity.startsWith("Uncommon") -> SkyBlue
    else -> LemonYellow
}

/**
 * The rarity gem, shape-coded the way the genre does it: circle for
 * common, diamond for uncommon, star for everything rarer.
 */
@Composable
private fun RarityGem(rarity: String, modifier: Modifier = Modifier) {
    val accent = rarityAccent(rarity)
    when {
        rarity.startsWith("Common") ->
            Box(modifier.size(9.dp).clip(CircleShape).background(accent))
        rarity.startsWith("Uncommon") ->
            Box(modifier.size(10.dp).rotate(45f).clip(RoundedCornerShape(2.dp)).background(accent))
        else -> Canvas(modifier.size(14.dp)) {
            val outer = size.minDimension / 2f
            val inner = outer * 0.45f
            val star = Path().apply {
                for (i in 0 until 10) {
                    val r = if (i % 2 == 0) outer else inner
                    val angle = -PI / 2 + i * PI / 5
                    val x = center.x + (r * cos(angle)).toFloat()
                    val y = center.y + (r * sin(angle)).toFloat()
                    if (i == 0) moveTo(x, y) else lineTo(x, y)
                }
                close()
            }
            drawPath(star, accent)
        }
    }
}

/**
 * The foil treatment: a faint standing iridescence (the card reads as holo
 * even at rest) under a tinted gloss band that sweeps like light over foil.
 */
@Composable
private fun HoloSheen() {
    val t = rememberInfiniteTransition(label = "sheen")
    val x by t.animateFloat(
        initialValue = -0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(1700, easing = LinearEasing)),
        label = "sheenX",
    )
    Canvas(Modifier.fillMaxSize()) {
        drawRect(
            Brush.linearGradient(
                0.0f to SkyBlue.copy(alpha = 0.07f),
                0.35f to HotPink.copy(alpha = 0.06f),
                0.7f to LemonYellow.copy(alpha = 0.08f),
                1.0f to ElectricPurple.copy(alpha = 0.06f),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
            ),
        )
        drawRect(
            Brush.linearGradient(
                0.0f to Color.Transparent,
                0.42f to Color.Transparent,
                0.47f to SkyBlue.copy(alpha = 0.16f),
                0.5f to Color.White.copy(alpha = 0.3f),
                0.53f to HotPink.copy(alpha = 0.14f),
                0.58f to Color.Transparent,
                1.0f to Color.Transparent,
                start = Offset(size.width * (x - 0.5f), 0f),
                end = Offset(size.width * (x + 0.5f), size.height),
            ),
        )
    }
}

// ------------------------------------------- interim face (being replaced)

/** The shared pre-redesign face; per-game faces are replacing it. */
@Composable
private fun ClassicFace(card: CardPull, theme: PackTheme, holo: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFFDF8F2)),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 8.dp),
            ) {
                BasicText(
                    text = card.name,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF26221C)),
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 14.sp, stepSize = 1.sp),
                    modifier = Modifier.weight(1f).padding(end = 6.dp),
                )
                RarityGem(card.rarity)
            }
            EmojiHero(
                emoji = card.emoji,
                fontSize = 64,
                seed = card.name.hashCode(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp)
                    .border(
                        width = if (holo) 1.5.dp else 1.dp,
                        color = if (holo) LemonYellow else Color(0xFFD8D2C6),
                        shape = RoundedCornerShape(8.dp),
                    )
                    .clip(RoundedCornerShape(8.dp)),
            )
            if (card.type.isNotBlank()) {
                BasicText(
                    text = card.type,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF26221C)),
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 12.sp, stepSize = 1.sp),
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 7.dp),
                )
            }
            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                if (card.flavor.isNotBlank()) {
                    Text(
                        text = card.flavor,
                        style = TextStyle(fontSize = 10.sp, fontStyle = FontStyle.Italic, color = Color(0xFF5A554C)),
                        maxLines = 2,
                    )
                }
                Text(
                    text = card.rarity,
                    style = TextStyle(fontSize = 9.sp, color = Color(0xFF6A655C)),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = FakeCatalog.collectorNumberOf(theme.game, card),
                        style = TextStyle(fontSize = 9.sp, color = Color(0xFF8A8478)),
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                    if (card.stat.isNotBlank()) {
                        Text(
                            text = card.stat,
                            style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF26221C)),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
        if (holo) HoloSheen()
    }
}

// ---------------------------------------------------------------- Critters

private data class CritterElement(val name: String, val accent: Color, val wash: Color, val washLo: Color)

private fun critterElement(type: String): CritterElement {
    val element = type.split(' ').getOrNull(type.split(' ').lastIndex - 1) ?: ""
    return when (element) {
        "Flame" -> CritterElement(element, Color(0xFFC84A1B), Color(0xFFFFE8CC), Color(0xFFFFD3A6))
        "Tide" -> CritterElement(element, Color(0xFF2D6FB8), Color(0xFFD9EBFB), Color(0xFFBBD9F4))
        "Spark" -> CritterElement(element, Color(0xFFB8860B), Color(0xFFFFF4C2), Color(0xFFFFE98F))
        "Bloom" -> CritterElement(element, Color(0xFF3E7A42), Color(0xFFDDF0D8), Color(0xFFC2E2B8))
        "Dream" -> CritterElement(element, Color(0xFF6A4FA3), Color(0xFFE8DFF6), Color(0xFFD4C5EE))
        "Sky" -> CritterElement(element, Color(0xFF3A8FA8), Color(0xFFDCF2F7), Color(0xFFC0E6EF))
        else -> CritterElement("Meadow", Color(0xFF7A7234), Color(0xFFF0EDD4), Color(0xFFE2DDB4))
    }
}

private val CritterInk = Color(0xFF26221C)
private val CritterGilt = Color(0xFFD9B44A)
private val CritterGiltDark = Color(0xFF9A7B2A)

/**
 * The Pocket Critters face, on Pokémon's skeleton: stage chip, name, HP in
 * the header; gilt-framed art on an element-tinted body; the type line as
 * an italic species strip; flavor in its own thin box.
 */
@Composable
private fun CrittersFace(card: CardPull, holo: Boolean) {
    val element = critterElement(card.type)
    val stage = when {
        card.type.startsWith("Stage 2") -> "Stage 2"
        card.type.startsWith("Stage 1") -> "Stage 1"
        else -> "Basic"
    }
    Box(
        Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(9.dp))
            .background(Brush.verticalGradient(listOf(element.wash, element.washLo))),
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 9.dp, end = 9.dp, top = 9.dp, bottom = 6.dp),
            ) {
                Text(
                    text = stage,
                    style = TextStyle(
                        color = CritterInk,
                        fontSize = 7.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                )
                BasicText(
                    text = card.name,
                    style = TextStyle(color = CritterInk, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 14.sp, stepSize = 0.5.sp),
                    modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                )
                if (card.stat.isNotBlank()) {
                    Text(
                        text = card.stat,
                        style = TextStyle(
                            color = element.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                        ),
                        maxLines = 1,
                        modifier = Modifier.padding(end = 5.dp),
                    )
                }
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(element.accent)
                        .border(1.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                )
            }
            // The art in a gilt frame — the genre's picture-frame moment.
            EmojiHero(
                emoji = card.emoji,
                fontSize = 64,
                seed = card.name.hashCode(),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 9.dp)
                    .border(3.dp, if (holo) CritterGilt else Color.White.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                    .border(1.dp, CritterGiltDark.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .clip(RoundedCornerShape(6.dp)),
            )
            // The species strip, italic in a soft plate, like the line that
            // tells you what the creature is.
            Text(
                text = "${element.name} Critter · $stage",
                style = TextStyle(
                    color = CritterInk.copy(alpha = 0.85f),
                    fontSize = 8.5.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                ),
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            )
            Column(Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
                if (card.flavor.isNotBlank()) {
                    Text(
                        text = card.flavor,
                        style = TextStyle(
                            color = CritterInk.copy(alpha = 0.8f),
                            fontSize = 9.5.sp,
                            fontStyle = FontStyle.Italic,
                            lineHeight = 12.sp,
                        ),
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, element.accent.copy(alpha = 0.35f), RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    Text(
                        text = card.rarity,
                        style = TextStyle(color = CritterInk.copy(alpha = 0.7f), fontSize = 8.5.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 6.dp),
                    )
                    Text(
                        text = FakeCatalog.collectorNumberOf("critters", card),
                        style = TextStyle(color = CritterInk.copy(alpha = 0.55f), fontSize = 8.sp),
                        maxLines = 1,
                        modifier = Modifier.padding(end = 5.dp),
                    )
                    RarityGem(card.rarity)
                }
            }
        }
        if (holo) HoloSheen()
    }
}
