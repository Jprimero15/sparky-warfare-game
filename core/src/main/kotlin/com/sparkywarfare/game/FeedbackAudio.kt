package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.AudioDevice
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin

/**
 * Lightweight synthesized feedback sounds.
 *
 * This keeps the first audio pass asset-free while still using LibGDX's
 * Android audio abstraction. If the device cannot open an AudioDevice,
 * gameplay continues silently.
 */
object FeedbackAudio {
    enum class Cue { UI, LASER, HIT, EXPLOSION, POWER_UP }

    private const val SAMPLE_RATE = 22050
    private var device: AudioDevice? = null
    private var executor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "sparky-audio").apply { isDaemon = true }
    }
    private val available = AtomicBoolean(true)

    fun init() {
        if (device != null || !available.get()) return
        try {
            device = Gdx.audio.newAudioDevice(SAMPLE_RATE, true)
        } catch (_: Throwable) {
            available.set(false)
        }
    }

    fun play(cue: Cue) {
        if (!available.get()) return
        init()
        val target = device ?: return
        val samples = synthesize(cue)
        try {
            executor.execute {
                try {
                    target.writeSamples(samples, 0, samples.size)
                } catch (_: Throwable) {
                    available.set(false)
                }
            }
        } catch (_: Throwable) {
            available.set(false)
        }
    }

    fun dispose() {
        val target = device
        device = null
        try {
            executor.shutdownNow()
        } catch (_: Throwable) {
        }
        try {
            target?.dispose()
        } catch (_: Throwable) {
        }
        if (available.get()) {
            executor = Executors.newSingleThreadExecutor { runnable ->
                Thread(runnable, "sparky-audio").apply { isDaemon = true }
            }
        }
    }

    private fun synthesize(cue: Cue): ShortArray {
        val duration = when (cue) {
            Cue.UI -> 0.055f
            Cue.LASER -> 0.075f
            Cue.HIT -> 0.095f
            Cue.EXPLOSION -> 0.18f
            Cue.POWER_UP -> 0.16f
        }
        val count = (SAMPLE_RATE * duration).toInt()
        val output = ShortArray(count)
        val startFrequency = when (cue) {
            Cue.UI -> 520f
            Cue.LASER -> 880f
            Cue.HIT -> 150f
            Cue.EXPLOSION -> 110f
            Cue.POWER_UP -> 440f
        }
        val endFrequency = when (cue) {
            Cue.UI -> 760f
            Cue.LASER -> 420f
            Cue.HIT -> 70f
            Cue.EXPLOSION -> 42f
            Cue.POWER_UP -> 980f
        }
        val amplitude = when (cue) {
            Cue.UI -> 0.20f
            Cue.LASER -> 0.18f
            Cue.HIT -> 0.28f
            Cue.EXPLOSION -> 0.34f
            Cue.POWER_UP -> 0.22f
        }

        var phase = 0.0
        for (i in output.indices) {
            val t = i.toFloat() / output.size.toFloat()
            val frequency = startFrequency + (endFrequency - startFrequency) * t
            phase += 2.0 * PI * frequency / SAMPLE_RATE
            val envelope = when {
                t < 0.08f -> t / 0.08f
                else -> ((1f - t) / 0.92f).coerceAtLeast(0f)
            }
            val harmonic = when (cue) {
                Cue.EXPLOSION -> sin(phase * 0.5) * 0.55 + sin(phase * 1.7) * 0.35
                Cue.HIT -> sin(phase) * 0.8 + sin(phase * 2.0) * 0.2
                else -> sin(phase)
            }
            output[i] = (harmonic * amplitude * envelope * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return output
    }
}
