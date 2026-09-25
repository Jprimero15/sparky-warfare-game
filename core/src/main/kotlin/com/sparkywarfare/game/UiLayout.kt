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
            (height - Gdx.graphics.safeInsetTop).toFloat().coerceAtMost(height - GameConfig.Ui.SAFE_MARGIN)
        )
    }

    fun menu(width: Float, height: Float) {
        val safeWidth = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val safeHeight = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val centerX = (safeArea.left + safeArea.right) / 2f

        val buttonW = (safeWidth * 0.58f).coerceIn(280f, 560f)
        val buttonH = (safeHeight * 0.14f).coerceIn(52f, 74f)
        val gap = (safeHeight * 0.045f).coerceIn(10f, 20f)

        val stackH = buttonH * 3f + gap * 2f
        val stackBottom = (safeArea.bottom + safeArea.top) / 2f - stackH / 2f - 8f

        singleButton.set(centerX - buttonW / 2f, stackBottom + (buttonH + gap) * 2f, buttonW, buttonH)
        multiButton.set(centerX - buttonW / 2f, stackBottom + buttonH + gap, buttonW, buttonH)
        settingsButton.set(centerX - buttonW / 2f, stackBottom, buttonW, buttonH)
    }

    fun hud() {
        pauseButton.set(safeArea.right - 58f, safeArea.top - 48f, 48f, 38f)
    }

    fun settings(width: Float, height: Float) {
        val centerX = (safeArea.left + safeArea.right) / 2f
        val bw = (safeArea.right - safeArea.left).coerceIn(300f, 560f) * 0.66f
        val bh = 52f
        toggleSfxButton.set(centerX - bw / 2f, height * 0.50f, bw, bh)
        toggleHapticsButton.set(centerX - bw / 2f, height * 0.39f, bw, bh)
        volumeSlider.set(centerX - bw / 2f, height * 0.29f, bw, 24f)
        swapControlsButton.set(centerX - bw / 2f, height * 0.17f, bw, bh)
        menuButton.set(centerX - bw / 2f, height * 0.06f, bw, bh)
    }

    fun pause(width: Float, height: Float) {
        val centerX = (safeArea.left + safeArea.right) / 2f
        val bw = (safeArea.right - safeArea.left).coerceIn(300f, 520f) * 0.58f
        resumeButton.set(centerX - bw / 2f, height * 0.37f, bw, 58f)
        menuButton.set(centerX - bw / 2f, height * 0.23f, bw, 58f)
    }

    fun gameOver(width: Float, height: Float) {
        val panelW = (safeArea.right - safeArea.left).coerceIn(340f, 660f) * 0.86f
        val panelH = (safeArea.top - safeArea.bottom).coerceIn(280f, 420f) * 0.82f
        val left = (safeArea.left + safeArea.right) / 2f - panelW / 2f
        val bottom = (safeArea.bottom + safeArea.top) / 2f - panelH / 2f
        val gap = 12f
        val buttonW = (panelW - 36f - gap) / 2f
        singleButton.set(left + 18f, bottom + 18f, buttonW, 54f)
        menuButton.set(singleButton.x + buttonW + gap, bottom + 18f, buttonW, 54f)
    }
}
