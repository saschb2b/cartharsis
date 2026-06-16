package com.cartharsis.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

/**
 * Device tilt as a normalized [Offset], each axis roughly in [-1, 1], for the
 * holographic foil on the Imagination Express card and the chase cards. x is
 * left/right roll (it rests at 0, so the highlight sits put when the phone is
 * held flat); y is forward/back pitch, de-biased to whatever resting angle you
 * grip the phone at, so the shimmer answers a *change* in tilt rather than the
 * particular hold.
 *
 * Reads the fused gravity sensor, falling back to the raw accelerometer, and
 * low-pass smooths the reading so the highlight glides instead of jittering. On
 * a device with no such sensor (some emulators) it simply reports zero and the
 * foil falls back to its own idle sweep. Hand-rolled, no dependency, and the
 * listener unregisters with the composable so it never costs anything off-screen.
 */
@Composable
fun rememberTilt(rollGain: Float = 1.6f, pitchGain: Float = 1.4f): State<Offset> {
    val context = LocalContext.current
    val tilt = remember { mutableStateOf(Offset.Zero) }
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (sensorManager == null || sensor == null) {
            onDispose { }
        } else {
            // The accelerometer fallback carries gravity plus motion; the
            // smoothing below settles it, and the bias tracks the resting pitch.
            var pitchBias = Float.NaN
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val g = SensorManager.GRAVITY_EARTH
                    val targetX = (event.values[0] / g * rollGain).coerceIn(-1f, 1f)
                    val rawPitch = event.values[1]
                    pitchBias = if (pitchBias.isNaN()) rawPitch else pitchBias + 0.02f * (rawPitch - pitchBias)
                    val targetY = ((rawPitch - pitchBias) / g * pitchGain).coerceIn(-1f, 1f)
                    val prev = tilt.value
                    val next = Offset(
                        prev.x + 0.18f * (targetX - prev.x),
                        prev.y + 0.18f * (targetY - prev.y),
                    )
                    // Skip imperceptible deltas so a still phone stops redrawing.
                    if (abs(next.x - prev.x) > 0.0015f || abs(next.y - prev.y) > 0.0015f) {
                        tilt.value = next
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager.unregisterListener(listener) }
        }
    }
    return tilt
}
