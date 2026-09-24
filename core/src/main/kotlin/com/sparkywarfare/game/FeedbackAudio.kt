package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound

object FeedbackAudio {
    enum class Cue { UI, LASER, HIT, EXPLOSION, POWER_UP }

    private var laser: Sound? = null
    private var hit: Sound? = null
    private var explosion: Sound? = null
    private var available = true
    private var muted = false

    fun init() {
        if (!available || laser != null) return
        try {
            laser = Gdx.audio.newSound(Gdx.files.internal("audio/laser.ogg"))
            hit = Gdx.audio.newSound(Gdx.files.internal("audio/hit.ogg"))
            explosion = Gdx.audio.newSound(Gdx.files.internal("audio/explosion.ogg"))
        } catch (_: Throwable) {
            available = false
            disposeSounds()
        }
    }

    fun setMuted(value: Boolean) { muted = value }
    fun isMuted(): Boolean = muted

    fun play(cue: Cue) {
        if (!available || muted) return
        init()
        try {
            when (cue) {
                Cue.UI -> hit?.play(0.28f, 1.65f, 0f)
                Cue.LASER -> laser?.play(0.34f, 1.05f, 0f)
                Cue.HIT -> hit?.play(0.5f, 0.9f, 0f)
                Cue.EXPLOSION -> explosion?.play(0.78f, 0.82f, 0f)
                Cue.POWER_UP -> laser?.play(0.4f, 1.45f, 0f)
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
        laser = null
        hit = null
        explosion = null
    }
}
