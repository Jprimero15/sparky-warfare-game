package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Rectangle

data class SafeArea(val left: Float, val right: Float, val bottom: Float, val top: Float)

class UiLayout {
    val singleButton = Rectangle()
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
    val touchControls = TouchControlGeometry()

    var safeArea = SafeArea(0f, 0f, 0f, 0f)
        private set

    fun update(width: Float, height: Float) {
        val left = Gdx.graphics.safeInsetLeft.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN)
        val right = (width - Gdx.graphics.safeInsetRight).coerceAtMost(width - GameConfig.Ui.SAFE_MARGIN)
        val bottom = Gdx.graphics.safeInsetBottom.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN)
        val top = (height - Gdx.graphics.safeInsetTop).coerceAtMost(height - GameConfig.Ui.SAFE_MARGIN)
        safeArea = SafeArea(left, right, bottom, top)
    }

    fun menu(width: Float, height: Float) {
        val sw = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val sh = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val cx = (safeArea.left + safeArea.right) / 2f
        val cy = (safeArea.bottom + safeArea.top) / 2f
        if (sw >= sh * 1.35f) {
            val panelW = minOf(sw * 0.92f, 1220f).coerceAtLeast(280f)
            val panelH = minOf(sh * 0.90f, 680f).coerceAtLeast(250f)
            val panelX = cx - panelW / 2f
            val panelY = cy - panelH / 2f
            val split = panelX + panelW * 0.54f
            val buttonW = (panelX + panelW - split - 48f).coerceIn(150f, 500f)
            val buttonH = (panelH * 0.18f).coerceIn(46f, 84f)
            val gap = (panelH * 0.035f).coerceIn(10f, 18f)
            val bx = split + (panelX + panelW - split - buttonW) / 2f
            val by = panelY + (panelH - (buttonH * 3f + gap * 2f)) / 2f
            singleButton.set(bx, by + (buttonH + gap) * 2f, buttonW, buttonH)
            multiButton.set(bx, by + buttonH + gap, buttonW, buttonH)
            settingsButton.set(bx, by, buttonW, buttonH)
        } else {
            val bw = minOf(sw * 0.78f, 560f).coerceAtLeast(220f)
            val bh = (sh * 0.13f).coerceIn(46f, 72f)
            val gap = 12f
            val total = bh * 3f + gap * 2f
            val by = cy - total / 2f
            singleButton.set(cx - bw / 2f, by + (bh + gap) * 2f, bw, bh)
            multiButton.set(cx - bw / 2f, by + bh + gap, bw, bh)
            settingsButton.set(cx - bw / 2f, by, bw, bh)
        }
    }

    fun hud() {
        val size = 58f
        pauseButton.set(safeArea.right - size, safeArea.top - size, size, size)
    }

    fun touchControls(width: Float, height: Float, controlsSwapped: Boolean) {
        val leftSafe = Gdx.graphics.safeInsetLeft.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN)
        val rightSafe = (width - Gdx.graphics.safeInsetRight).coerceAtMost(width - GameConfig.Ui.SAFE_MARGIN)
        val bottomSafe = Gdx.graphics.safeInsetBottom.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN)
        val radius = (height * 0.29f).coerceIn(112f, 156f)
        val leftX = leftSafe + radius + 30f
        val rightX = rightSafe - radius - 30f
        val baseY = bottomSafe + radius + 24f
        val centerYScreen = height - baseY
        val moveX = if (controlsSwapped) rightX else leftX
        val fireX = if (controlsSwapped) leftX else rightX
        touchControls.set(moveX, centerYScreen, fireX, centerYScreen, radius)
    }

    fun settings(width: Float, height: Float) {
        val sw = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val sh = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val cx = (safeArea.left + safeArea.right) / 2f
        val cy = (safeArea.bottom + safeArea.top) / 2f
        val bw = minOf(sw * 0.36f, 430f).coerceAtLeast(150f)
        val bh = (sh * 0.18f).coerceIn(46f, 72f)
        val gap = (sh * 0.045f).coerceIn(12f, 22f)
        val leftX = cx - bw - gap / 2f
        val rightX = cx + gap / 2f
        val topY = cy + bh + gap * 0.7f
        val bottomY = cy - bh - gap * 0.7f
        toggleSfxButton.set(leftX, topY, bw, bh)
        toggleHapticsButton.set(rightX, topY, bw, bh)
        volumeSlider.set(leftX, bottomY, bw * 2f + gap, 28f)
        swapControlsButton.set(leftX, bottomY - bh - gap, bw, bh)
        menuButton.set(rightX, bottomY - bh - gap, bw, bh)
    }

    fun pause(width: Float, height: Float) {
        val sw = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val sh = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val cx = (safeArea.left + safeArea.right) / 2f
        val cy = (safeArea.bottom + safeArea.top) / 2f
        val bw = minOf(sw * 0.30f, 420f).coerceAtLeast(150f)
        val bh = (sh * 0.18f).coerceIn(48f, 82f)
        val gap = (sw * 0.035f).coerceIn(16f, 30f)
        resumeButton.set(cx - bw - gap / 2f, cy - bh / 2f, bw, bh)
        menuButton.set(cx + gap / 2f, cy - bh / 2f, bw, bh)
    }

    fun gameOver(width: Float, height: Float) {
        val sw = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val sh = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val cx = (safeArea.left + safeArea.right) / 2f
        val cy = (safeArea.bottom + safeArea.top) / 2f
        val panelW = minOf(sw * 0.78f, 980f).coerceAtLeast(320f)
        val buttonW = ((panelW - 40f) / 2f).coerceIn(130f, 420f)
        val gap = 18f
        singleButton.set(cx - buttonW - gap / 2f, cy - sh * 0.28f, buttonW, 64f)
        menuButton.set(cx + gap / 2f, cy - sh * 0.28f, buttonW, 64f)
    }
}


class TouchControlGeometry {
    val move = Circle()
    val fire = Circle()
    val moveHit = Circle()
    val fireHit = Circle()
    var radius: Float = 0f
        private set

    fun set(moveX: Float, centerY: Float, fireX: Float, fireY: Float, radius: Float) {
        this.radius = radius
        move.set(moveX, centerY, radius)
        fire.set(fireX, fireY, radius)
        val hitRadius = radius + 26f
        moveHit.set(moveX, centerY, hitRadius)
        fireHit.set(fireX, fireY, hitRadius)
    }
}
