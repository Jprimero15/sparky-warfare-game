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
        val leftInset = Gdx.graphics.safeInsetLeft.toFloat()
        val rightInset = Gdx.graphics.safeInsetRight.toFloat()
        val bottomInset = Gdx.graphics.safeInsetBottom.toFloat()
        val topInset = Gdx.graphics.safeInsetTop.toFloat()

        safeArea = SafeArea(
            leftInset.coerceAtLeast(GameConfig.Ui.SAFE_MARGIN),
            (width - rightInset).coerceAtMost(width - GameConfig.Ui.SAFE_MARGIN),
            bottomInset.coerceAtLeast(GameConfig.Ui.SAFE_MARGIN),
            (height - topInset).coerceAtMost(height - GameConfig.Ui.SAFE_MARGIN)
        )
    }

    fun menu(width: Float, height: Float) {
        val safeWidth = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val safeHeight = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val centerX = (safeArea.left + safeArea.right) / 2f
        val buttonW = (safeWidth * 0.62f).coerceIn(300f, 620f)
        val buttonH = (safeHeight * 0.105f).coerceIn(58f, 76f)
        val gap = (safeHeight * 0.028f).coerceIn(10f, 18f)
        val stackH = buttonH * 3f + gap * 2f
        val stackBottom = (safeArea.bottom + safeHeight * 0.15f).coerceAtMost(
            safeArea.top - stackH - 42f
        )

        singleButton.set(centerX - buttonW / 2f, stackBottom + (buttonH + gap) * 2f, buttonW, buttonH)
        multiButton.set(centerX - buttonW / 2f, stackBottom + buttonH + gap, buttonW, buttonH)
        settingsButton.set(centerX - buttonW / 2f, stackBottom, buttonW, buttonH)
    }

    fun hud() {
        val buttonSize = 58f
        pauseButton.set(
            safeArea.right - buttonSize,
            safeArea.top - buttonSize,
            buttonSize,
            buttonSize
        )
    }

    fun settings(width: Float, height: Float) {
        val safeWidth = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val safeHeight = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val centerX = (safeArea.left + safeArea.right) / 2f
        val bw = (safeWidth * 0.68f).coerceIn(320f, 600f)
        val bh = (safeHeight * 0.105f).coerceIn(54f, 72f)
        val gap = (safeHeight * 0.035f).coerceIn(12f, 20f)
        val totalH = bh * 3f + 24f + gap * 2f + 48f
        val start = (safeArea.bottom + safeHeight / 2f + totalH / 2f - bh).coerceAtMost(safeArea.top - 16f)

        toggleSfxButton.set(centerX - bw / 2f, start - bh, bw, bh)
        toggleHapticsButton.set(centerX - bw / 2f, toggleSfxButton.y - gap - bh, bw, bh)
        volumeSlider.set(centerX - bw / 2f, toggleHapticsButton.y - gap - 24f, bw, 24f)
        swapControlsButton.set(centerX - bw / 2f, volumeSlider.y - gap - bh, bw, bh)
        menuButton.set(centerX - bw / 2f, safeArea.bottom + 8f, bw, bh)
    }

    fun pause(width: Float, height: Float) {
        val safeWidth = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val centerX = (safeArea.left + safeArea.right) / 2f
        val bw = (safeWidth * 0.58f).coerceIn(320f, 560f)
        val bh = 64f
        resumeButton.set(centerX - bw / 2f, height * 0.39f, bw, bh)
        menuButton.set(centerX - bw / 2f, height * 0.24f, bw, bh)
    }

    fun gameOver(width: Float, height: Float) {
        val safeWidth = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val safeHeight = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val panelW = safeWidth.coerceIn(380f, 920f) * 0.88f
        val panelH = safeHeight.coerceIn(330f, 520f) * 0.82f
        val left = (safeArea.left + safeArea.right) / 2f - panelW / 2f
        val bottom = (safeArea.bottom + safeArea.top) / 2f - panelH / 2f
        val gap = 14f
        val buttonW = (panelW - 42f - gap) / 2f
        singleButton.set(left + 21f, bottom + 22f, buttonW, 58f)
        menuButton.set(singleButton.x + buttonW + gap, bottom + 22f, buttonW, 58f)
    }
}
