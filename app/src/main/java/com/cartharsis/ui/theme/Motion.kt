package com.cartharsis.ui.theme

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

/**
 * M3 Expressive motion, applied by hand. This material3 build keeps the
 * MotionScheme / MaterialExpressiveTheme APIs internal, so we mirror their model
 * with our own spring tokens instead of the app's old fixed-duration tweens.
 *
 * The split is the heart of it: **spatial** springs carry movement, size, scale,
 * and rotation and may overshoot a touch (the lively, physical feel);
 * **effects** springs carry color and alpha and never overshoot (so a fade or a
 * tint settles cleanly instead of wobbling).
 */
object Motion {
    /** Movement / size / scale, the default expressive feel — a gentle overshoot. */
    fun <T> spatial(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow)

    /** A snappier spatial spring for small, quick moves. */
    fun <T> spatialFast(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium)

    /** A bouncier spatial spring, reserved for hero / reward moments. */
    fun <T> spatialExpressive(): FiniteAnimationSpec<T> =
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)

    /** Color / alpha — critically damped, no overshoot. */
    fun <T> effects(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = Spring.StiffnessMedium)
}
