package com.cartharsis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Extended color role for savings / money-kept accents (the one accent the
 * conventions allow beside CTAs). Brand MintGreen reads fine on dark surfaces
 * but fails text contrast on the light cream ones, so light theme deepens it.
 * Decorative uses (confetti, shelf gradients, alpha washes) keep raw MintGreen.
 */
val LocalSavingsColor = staticCompositionLocalOf { MintGreen }

private val DarkColorScheme = darkColorScheme(
    primary = HotPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF551133),
    onPrimaryContainer = HotPinkDim,
    secondary = ElectricPurpleDim,
    onSecondary = Color(0xFF2A1B52),
    secondaryContainer = Color(0xFF3B2A66),
    onSecondaryContainer = ElectricPurpleDim,
    tertiary = JuicyOrangeDim,
    onTertiary = Color(0xFF4A2200),
    tertiaryContainer = Color(0xFF5E2E00),
    onTertiaryContainer = JuicyOrangeDim,
    background = NightBg,
    onBackground = NightText,
    surface = NightSurface,
    onSurface = NightText,
    surfaceVariant = Color(0xFF362932),
    onSurfaceVariant = Color(0xFFCBB8C3),
)

private val LightColorScheme = lightColorScheme(
    primary = HotPink,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E7),
    onPrimaryContainer = Color(0xFF5A0030),
    secondary = ElectricPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9DDFF),
    onSecondaryContainer = Color(0xFF22005D),
    tertiary = JuicyOrange,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC6),
    onTertiaryContainer = Color(0xFF3A1A00),
    background = CreamBg,
    onBackground = InkText,
    surface = CreamSurface,
    onSurface = InkText,
    surfaceVariant = Color(0xFFF6E4EC),
    onSurfaceVariant = Color(0xFF564650),
)

// Dynamic color is deliberately off: the dopamine palette IS the brand.
@Composable
fun CartharsisTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalSavingsColor provides if (darkTheme) MintGreen else MintGreenDeep) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content,
        )
    }
}
