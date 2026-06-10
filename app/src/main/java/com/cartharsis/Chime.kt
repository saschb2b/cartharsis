package com.cartharsis

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Short UI sounds for reward moments. Per the reward-moment research, audio
 * must fire at the exact visual peak, co-designed with the haptic — so play()
 * calls sit right next to performHapticFeedback() at the moment that matters.
 * Sonification usage keeps the chime behaving like a system sound rather
 * than media.
 */
object Chime {

    private var soundPool: SoundPool? = null
    private var successId = 0
    private var successLoaded = false

    fun init(context: Context) {
        if (soundPool != null) return
        val pool = SoundPool.Builder()
            .setMaxStreams(2)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            .build()
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0 && sampleId == successId) successLoaded = true
        }
        successId = pool.load(context, R.raw.success_chime, 1)
        soundPool = pool
    }

    /** The payment-complete chime; silently does nothing if not ready yet. */
    fun playSuccess() {
        if (successLoaded) {
            soundPool?.play(successId, 1f, 1f, 1, 0, 1f)
        }
    }
}
