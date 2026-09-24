package com.sparkywarfare.game.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.sparkywarfare.game.SparkyWarfareGame
import com.sparkywarfare.game.WORLD_WIDTH
import com.sparkywarfare.game.WORLD_HEIGHT

/** Run with `./gradlew desktop:run` for fast local iteration on the VFX. */
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("Sparky Warfare")
        setWindowedMode(WORLD_WIDTH.toInt(), WORLD_HEIGHT.toInt())
        useVsync(true)
        setForegroundFPS(60)
    }
    Lwjgl3Application(SparkyWarfareGame(), config)
}
