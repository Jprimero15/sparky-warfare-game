package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle

data class SafeArea(val left: Float, val right: Float, val bottom: Float, val top: Float)

class UiLayout {
    val singleButton = Rectangle()
    val multiButton = Rectangle()
    val settingsButton = Rectangle()
    val pauseButton = Rectangle()
    val resumeButton = Rectangle()
    val menuButton = Rectangle()
    val toggleSfxButton = Rectangle()
    val toggleHapticsButton = Rectangle()
    val volumeSlider = Rectangle()
    val swapControlsButton = Rectangle()
    val tutorialButton = Rectangle()
    val upgradeButtons = Array(3) { Rectangle() }

    var safeArea = SafeArea(0f, 0f, 0f, 0f)
        private set

    fun update(width: Float, height: Float) {
        safeArea = SafeArea(
            Gdx.graphics.safeInsetLeft.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN),
            (width - Gdx.graphics.safeInsetRight).coerceAtMost(width - GameConfig.Ui.SAFE_MARGIN),
            Gdx.graphics.safeInsetBottom.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN),
            (height - Gdx.graphics.safeInsetTop).coerceAtMost(height - GameConfig.Ui.SAFE_MARGIN)
        )
    }

    fun menu(width: Float, height: Float) {
        val buttonW = (width * 0.64f).coerceIn(300f, 520f)
        val buttonH = (height * 0.11f).coerceIn(56f, 78f)
        val centerX = (safeArea.left + safeArea.right) / 2f
        singleButton.set(centerX - buttonW / 2f, height * 0.35f, buttonW, buttonH)
        multiButton.set(centerX - buttonW / 2f, height * 0.22f, buttonW, buttonH)
        settingsButton.set(centerX - buttonW / 2f, height * 0.09f, buttonW, buttonH)
    }

    fun hud() {
        pauseButton.set(safeArea.right - 58f, safeArea.top - 48f, 48f, 38f)
    }

    fun settings(width: Float, height: Float) {
        val centerX = (safeArea.left + safeArea.right) / 2f
        val bw = (width * 0.6f).coerceIn(300f, 520f)
        val bh = 58f
        toggleSfxButton.set(centerX - bw / 2f, height * 0.48f, bw, bh)
        toggleHapticsButton.set(centerX - bw / 2f, height * 0.36f, bw, bh)
        volumeSlider.set(centerX - bw / 2f, height * 0.26f, bw, 28f)
        swapControlsButton.set(centerX - bw / 2f, height * 0.16f, bw, bh)
        menuButton.set(centerX - bw / 2f, height * 0.06f, bw, bh)
    }

    fun pause(width: Float, height: Float) {
        val centerX = (safeArea.left + safeArea.right) / 2f
        resumeButton.set(centerX - 150f, height * 0.36f, 300f, 60f)
        menuButton.set(centerX - 150f, height * 0.23f, 300f, 60f)
    }

    fun gameOver(width: Float, height: Float) {
        val panelW = (width * 0.72f).coerceIn(340f, 620f)
        val panelH = (height * 0.5f).coerceIn(260f, 370f)
        val left = (width - panelW) / 2f
        val bottom = (height - panelH) / 2f
        singleButton.set(left + 18f, bottom + 18f, panelW / 2f - 27f, 54f)
        menuButton.set(left + panelW / 2f + 9f, bottom + 18f, panelW / 2f - 27f, 54f)
    }
}
