package com.cartharsis

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi

/**
 * The hold-to-pay tactile track: a buzz that *rises* in intensity as the bar
 * fills, with ticks that tighten toward the finish, then a firm thunk the
 * instant it locks in. Per the reward-moment research, anticipation needs the
 * feedback to build — flat, evenly-spaced ticks read as nothing, which is the
 * "I can't feel it" problem this replaces.
 *
 * Played on the raw [Vibrator] rather than performHapticFeedback so the buzz
 * and the ticks can be a single layered waveform on devices with amplitude
 * control, instead of fighting for the actuator. Tiered fallbacks keep it felt
 * on lesser hardware:
 *   1. API 26+ with amplitude control → buzz ramp with overlaid spikes
 *   2. API 26+ without amplitude control → accelerating on/off ticks
 *   3. API 24–25 → legacy accelerating on/off pattern
 *
 * Marked as sonification/touch feedback (mirroring [Chime]) so a user who has
 * turned haptics off is left alone.
 */
object HoldHaptics {

    private val audioTouchAttrs: AudioAttributes by lazy {
        AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    /**
     * Play [effect] tagged as touch feedback so a user who has turned haptics
     * off is left alone. API 33 deprecated the AudioAttributes overload in
     * favour of VibrationAttributes — use the right one per platform. Only
     * reached from API 26+ paths (callers fall back to the legacy pattern below).
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun Vibrator.vibrateTouch(effect: VibrationEffect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
        } else {
            @Suppress("DEPRECATION")
            vibrate(effect, audioTouchAttrs)
        }
    }

    fun vibrator(context: Context): Vibrator? {
        val v = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        return v?.takeIf { it.hasVibrator() }
    }

    /**
     * Begin the building buzz over the next [durationMs], resuming the ramp from
     * [fromFraction] (0..1) so a re-press after a partial release doesn't restart
     * the intensity from cold. Cancel with [cancel] on early release.
     */
    fun startFill(vibrator: Vibrator?, durationMs: Int, fromFraction: Float) {
        val vib = vibrator ?: return
        if (durationMs <= 0) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            startLegacy(vib, durationMs, fromFraction)
            return
        }
        val effect = if (vib.hasAmplitudeControl()) {
            risingBuzz(durationMs, fromFraction)
        } else {
            acceleratingTicks(durationMs, fromFraction)
        }
        vib.vibrateTouch(effect)
    }

    /** Stop an in-progress fill (the thumb lifted before completion). */
    fun cancel(vibrator: Vibrator?) {
        vibrator?.cancel()
    }

    /**
     * A single discrete tick whose strength scales with [intensity] (0..1) —
     * for escalating beats like prying a parcel lid looser tap by tap. On
     * hardware without amplitude control the intensity rides the duration
     * instead, so it still reads as "harder."
     */
    fun tick(vibrator: Vibrator?, intensity: Float) {
        val vib = vibrator ?: return
        val clamped = intensity.coerceIn(0f, 1f)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amp = if (vib.hasAmplitudeControl()) {
                (55f + 200f * clamped).toInt().coerceIn(1, 255)
            } else {
                VibrationEffect.DEFAULT_AMPLITUDE
            }
            vib.vibrateTouch(VibrationEffect.createOneShot((14f + 24f * clamped).toLong(), amp))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate((14f + 24f * clamped).toLong())
        }
    }

    /** The completion thunk — fired at the exact frame the bar locks in. */
    fun thunk(vibrator: Vibrator?) {
        val vib = vibrator ?: return
        vib.cancel() // cut any waveform tail so the thunk lands clean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vib.vibrateTouch(VibrationEffect.createOneShot(38, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vib.vibrate(38)
        }
    }

    /**
     * A buzz whose amplitude eases up (quadratic, so it builds slowly then
     * surges) with brief 255 spikes laid over it — the spikes get closer
     * together toward the end (an accelerando), each articulated by a short
     * valley just before it so it pops against the rising buzz.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun risingBuzz(durationMs: Int, fromFraction: Float): VibrationEffect {
        val seg = 16L
        val count = (durationMs / seg).toInt().coerceIn(1, 400)
        val timings = LongArray(count) { seg }
        val amps = IntArray(count)
        val totalTicks = 18
        var lastTick = (fromFraction * fromFraction * totalTicks).toInt()
        for (i in 0 until count) {
            val f = fromFraction + (1f - fromFraction) * ((i + 1) / count.toFloat())
            amps[i] = (45f + 165f * f * f).toInt().coerceIn(1, 255) // 45 → 210, ease-in
        }
        // Overlay the accelerating spikes, each preceded by a valley.
        for (i in 0 until count) {
            val f = fromFraction + (1f - fromFraction) * ((i + 1) / count.toFloat())
            val tick = (f * f * totalTicks).toInt()
            if (tick > lastTick) {
                lastTick = tick
                amps[i] = 255
                if (i > 0) amps[i - 1] = (amps[i - 1] - 120).coerceIn(12, 255)
            }
        }
        return VibrationEffect.createWaveform(timings, amps, -1)
    }

    /** No amplitude control: accelerating on/off ticks (gap shrinks toward the end). */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun acceleratingTicks(durationMs: Int, fromFraction: Float): VibrationEffect =
        VibrationEffect.createWaveform(tickPattern(durationMs, fromFraction), -1)

    @Suppress("DEPRECATION")
    private fun startLegacy(vibrator: Vibrator, durationMs: Int, fromFraction: Float) {
        vibrator.vibrate(tickPattern(durationMs, fromFraction), -1)
    }

    /**
     * An off/on/off/on… pattern (the leading 0 is the legacy-API convention for
     * "start immediately") whose silent gaps shrink as the fill rises, so the
     * ticks rev up toward completion.
     */
    private fun tickPattern(durationMs: Int, fromFraction: Float): LongArray {
        val pattern = ArrayList<Long>()
        pattern.add(0L) // wait nothing before the first buzz
        val onMs = 14L
        var elapsed = 0L
        var f = fromFraction
        while (elapsed < durationMs && pattern.size < 80) {
            // Gap goes from ~110ms when cold to ~16ms near the finish.
            val gap = (110f - 94f * f).toLong().coerceIn(16L, 110L)
            pattern.add(onMs)
            pattern.add(gap)
            elapsed += onMs + gap
            f = (fromFraction + (1f - fromFraction) * (elapsed.toFloat() / durationMs)).coerceAtMost(1f)
        }
        return pattern.toLongArray()
    }
}
