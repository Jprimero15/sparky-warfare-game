package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound

object FeedbackAudio {
    enum class Cue { UI, LASER, HIT, EXPLOSION, POWER_UP }

    private var laser: Sound? = null
    private var uiConfirm: Sound? = null
    private var hit: Sound? = null
    private var explosion: Sound? = null
    private var available = true
    private var muted = false
    private var masterVolume = 1f

    fun init() {
        if (!available || laser != null) return
        try {
            laser = Gdx.audio.newSound(Gdx.files.internal("audio/laser.ogg"))
            uiConfirm = Gdx.audio.newSound(Gdx.files.internal("audio/ui_confirm.ogg"))
            hit = Gdx.audio.newSound(Gdx.files.internal("audio/hit.ogg"))
            explosion = Gdx.audio.newSound(Gdx.files.internal("audio/explosion.ogg"))
        } catch (_: Throwable) {
            available = false
            disposeSounds()
        }
    }

    fun setMuted(value: Boolean) { muted = value }
    fun isMuted(): Boolean = muted

    fun setMasterVolume(value: Float) {
        masterVolume = value.coerceIn(0f, 1f)
    }

    fun masterVolume(): Float = masterVolume

    fun play(cue: Cue) {
        if (!available || muted || masterVolume <= 0f) return
        init()
        val volume = masterVolume
        try {
            when (cue) {
                Cue.UI -> uiConfirm?.play(volume * 0.32f, 1.1f, 0f)
                Cue.LASER -> laser?.play(volume * 0.34f, 1.05f, 0f)
                Cue.HIT -> hit?.play(volume * 0.5f, 0.9f, 0f)
                Cue.EXPLOSION -> explosion?.play(volume * 0.78f, 0.82f, 0f)
                Cue.POWER_UP -> laser?.play(volume * 0.4f, 1.45f, 0f)
            }
        } catch (_: Throwable) {
            available = false
        }
    }

    fun dispose() {
        disposeSounds()
        available = true
    }

    private fun disposeSounds() {
        try { laser?.dispose() } catch (_: Throwable) {}
        try { hit?.dispose() } catch (_: Throwable) {}
        try { explosion?.dispose() } catch (_: Throwable) {}
        try { uiConfirm?.dispose() } catch (_: Throwable) {}
        laser = null
        hit = null
        explosion = null
        uiConfirm = null
    }
}
