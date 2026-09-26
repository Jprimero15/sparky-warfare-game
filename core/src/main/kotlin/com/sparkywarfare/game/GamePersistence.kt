package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences

data class SavedStats(val highScore: Int, val bestWave: Int, val totalKills: Int)

data class SavedSettings(
    val muteSfx: Boolean,
    val muteHaptics: Boolean,
    val sfxVolume: Float,
    val controlsSwapped: Boolean,
    val tutorialSeen: Boolean
)

/** Backward-compatible wrapper for the existing preference keys. */
class GamePersistence(
    private val prefs: Preferences = Gdx.app.getPreferences("Sparky Warfare")
) {
    fun stats() = SavedStats(
        prefs.getInteger("highScore", 0),
        prefs.getInteger("bestWave", 0),
        prefs.getInteger("totalKills", 0)
    )

    fun settings() = SavedSettings(
        prefs.getBoolean("muteSfx", false),
        prefs.getBoolean("muteHaptics", false),
        prefs.getFloat("sfxVolume", 0.8f),
        prefs.getBoolean("controlsSwapped", false),
        prefs.getBoolean("tutorialSeen", false)
    )

    fun setMuteSfx(value: Boolean) = prefs.putBoolean("muteSfx", value).flush()
    fun setMuteHaptics(value: Boolean) = prefs.putBoolean("muteHaptics", value).flush()
    fun setSfxVolume(value: Float, flush: Boolean = true) {
        prefs.putFloat("sfxVolume", value.coerceIn(0f, 1f))
        if (flush) prefs.flush()
    }

    fun flush() = prefs.flush()
    fun setControlsSwapped(value: Boolean) = prefs.putBoolean("controlsSwapped", value).flush()
    fun setTutorialSeen(value: Boolean) = prefs.putBoolean("tutorialSeen", value).flush()

    fun saveProgress(highScore: Int, bestWave: Int, totalKills: Int) {
        prefs.putInteger("highScore", highScore)
        prefs.putInteger("bestWave", bestWave)
        prefs.putInteger("totalKills", totalKills)
        prefs.flush()
    }
}