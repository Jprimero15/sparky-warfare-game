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

        if (sw >= sh * 1.30f) {
            val panelH = minOf(sh * 0.86f, 640f).coerceAtLeast(300f)
            val panelW = minOf(sw * 0.88f, 1180f).coerceAtLeast(520f)
            val panelX = cx - panelW / 2f
            val panelY = cy - panelH / 2f
            val splitX = panelX + panelW * 0.50f
            val buttonLeft = splitX + 44f
            val buttonRight = panelX + panelW - 44f
            val buttonW = (buttonRight - buttonLeft).coerceIn(190f, 500f)
            val buttonH = (panelH * 0.18f).coerceIn(60f, 86f)
            val gap = (panelH * 0.045f).coerceIn(14f, 22f)
            val totalH = buttonH * 2f + gap
            val startY = panelY + (panelH - totalH) / 2f

            singleButton.set(buttonLeft, startY + buttonH + gap, buttonW, buttonH)
            settingsButton.set(buttonLeft, startY, buttonW, buttonH)
        } else {
            val bw = minOf(sw * 0.80f, 560f).coerceAtLeast(240f)
            val bh = (sh * 0.16f).coerceIn(58f, 80f)
            val gap = 16f
            val total = bh * 2f + gap
            val by = cy - total / 2f
            singleButton.set(cx - bw / 2f, by + bh + gap, bw, bh)
            settingsButton.set(cx - bw / 2f, by, bw, bh)
        }
    }

    fun hud() {
        val size = 72f
        val inset = 14f
        pauseButton.set(
            safeArea.right - size - inset,
            safeArea.top - size - inset,
            size,
            size
        )
    }

    fun touchControls(width: Float, height: Float, controlsSwapped: Boolean) {
        val leftSafe = Gdx.graphics.safeInsetLeft.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN)
        val rightSafe = (width - Gdx.graphics.safeInsetRight).coerceAtMost(width - GameConfig.Ui.SAFE_MARGIN)
        val bottomSafe = Gdx.graphics.safeInsetBottom.toFloat().coerceAtLeast(GameConfig.Ui.SAFE_MARGIN)

        // Large thumb controls, kept clear of the center arena.
        val radius = (height * 0.30f).coerceIn(128f, 170f)
        val leftX = leftSafe + radius + 30f
        val rightX = rightSafe - radius - 30f
        val baseY = bottomSafe + radius + 26f
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
        val bw = minOf(sw * 0.34f, 420f).coerceAtLeast(160f)
        val bh = (sh * 0.16f).coerceIn(52f, 74f)
        val gap = (sh * 0.035f).coerceIn(12f, 20f)
        val leftX = cx - bw - gap / 2f
        val rightX = cx + gap / 2f
        val topY = cy + bh + gap
        val bottomY = cy - bh - gap
        toggleSfxButton.set(leftX, topY, bw, bh)
        toggleHapticsButton.set(rightX, topY, bw, bh)
        volumeSlider.set(leftX, bottomY, bw * 2f + gap, 34f)
        swapControlsButton.set(leftX, bottomY - bh - gap, bw, bh)
        menuButton.set(rightX, bottomY - bh - gap, bw, bh)
    }

    fun pause(width: Float, height: Float) {
        val sw = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val sh = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val cx = (safeArea.left + safeArea.right) / 2f
        val cy = (safeArea.bottom + safeArea.top) / 2f
        val bw = minOf(sw * 0.28f, 400f).coerceAtLeast(170f)
        val bh = (sh * 0.17f).coerceIn(56f, 82f)
        val gap = (sw * 0.03f).coerceIn(16f, 28f)
        resumeButton.set(cx - bw - gap / 2f, cy - bh / 2f, bw, bh)
        menuButton.set(cx + gap / 2f, cy - bh / 2f, bw, bh)
    }

    fun gameOver(width: Float, height: Float) {
        val sw = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val sh = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val cx = (safeArea.left + safeArea.right) / 2f
        val cy = (safeArea.bottom + safeArea.top) / 2f
        val panelW = minOf(sw * 0.72f, 920f).coerceAtLeast(340f)
        val buttonW = ((panelW - 42f) / 2f).coerceIn(150f, 390f)
        val gap = 20f
        val y = cy - sh * 0.25f
        singleButton.set(cx - buttonW - gap / 2f, y, buttonW, 68f)
        menuButton.set(cx + gap / 2f, y, buttonW, 68f)
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
        val hitRadius = radius + UiTheme.Metrics.TOUCH_EXTRA_HIT
        moveHit.set(moveX, centerY, hitRadius)
        fireHit.set(fireX, fireY, hitRadius)
    }
}
