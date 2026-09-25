package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

class WorldRenderer(
    private val camera: OrthographicCamera,
    private val hudCamera: OrthographicCamera,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val glow: GlowRenderer,
    private val tanks: TankRenderer,
    private val bloom: BloomRenderer,
    private val particles: ParticleDebris,
    private val font: BitmapFont,
    private val bodyFont: BitmapFont,
    private val input: InputController,
    private val ui: UiLayout,
    private val text: UiText
) {
    private val scratchUi = Vector2()
    private val idleTankColor = Color(0.2f, 0.9f, 1f, 1f)
    private val idleTank = Tank(scratchUi, angle = 45f, isPlayer = true, color = idleTankColor, radius = 14f)

    private val backdropColor = Color(0.003f, 0.005f, 0.008f, 1f)
    private val floorAccentColor = Color(0.018f, 0.028f, 0.036f, 1f)
    private val wallHighlight = Color(1f, 1f, 1f, 0.045f)
    private val wallShadow = Color(0f, 0f, 0f, 0.14f)
    private val hudTextColor = Color(0.85f, 0.95f, 1f, 0.85f)
    private val fireTextColor = Color(1f, 0.86f, 0.9f, 0.95f)
    private val steelLine = Color(0.28f, 0.42f, 0.5f, 0.38f)
    private val brickLine = Color(0.75f, 0.32f, 0.18f, 0.28f)
    private val concreteLine = Color(0.55f, 0.6f, 0.64f, 0.24f)
    private val metalLine = Color(0.22f, 0.62f, 0.7f, 0.28f)
    private val touchBase = Color(0.015f, 0.02f, 0.025f, 0.72f)
    private val touchAccent = Color(0.18f, 0.72f, 1f, 0.12f)
    private val touchKnobIdle = Color(0.25f, 0.85f, 1f, 0.42f)
    private val touchKnobActive = Color(0.25f, 0.85f, 1f, 0.7f)
    private val fireBase = Color(0.02f, 0.008f, 0.012f, 0.72f)
    private val fireIdle = Color(1f, 0.25f, 0.34f, 0.4f)
    private val fireActive = Color(1f, 0.22f, 0.3f, 0.76f)
    private val moveOutline = Color(0.65f, 0.9f, 1f, 0.55f)
    private val fireOutline = Color(1f, 0.65f, 0.7f, 0.65f)
    private val healthBack = Color(0f, 0f, 0f, 0.65f)
    private val healthElite = Color(1f, 0.72f, 0.16f, 0.95f)
    private val healthNormal = Color(0.35f, 0.9f, 1f, 0.9f)

    fun renderCombat(
        player: Tank,
        enemies: List<Tank>,
        lasers: List<Laser>,
        walls: List<Wall>,
        bursts: List<Burst>,
        powerUps: List<PowerUp>,
        domainBursts: List<Burst>
    ) {
        clear(0.001f, 0.002f, 0.003f)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        drawArenaBackdrop()
        drawWalls(walls)
        bloom.begin()
        glow.beginAdditive()
        if (player.alive) tanks.drawTank(player)
        enemies.forEach { if (it.alive) tanks.drawTank(it) }
        lasers.forEach { glow.drawLaser(it) }
        bursts.forEach { glow.drawBurst(it.position, it.t, it.color) }
        powerUps.forEach { glow.drawPowerUp(it.position, it.pulse, it.color) }
        domainBursts.forEach { glow.drawDomainBurst(it.position, it.t, it.color) }
        glow.end()
        bloom.endAndComposite()
        particles.draw(camera.combined)
        drawEnemyHealthBars(enemies)
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    fun renderIdle(walls: List<Wall>) {
        clear(0.02f, 0.02f, 0.06f)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        drawArenaBackdrop()
        drawWalls(walls)
        scratchUi.set(GameConfig.WORLD_WIDTH / 2f, GameConfig.WORLD_HEIGHT / 2f)
        bloom.begin()
        glow.beginAdditive()
        idleTank.position.set(scratchUi)
        tanks.drawTank(idleTank)
        glow.end()
        bloom.endAndComposite()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    fun drawTouchControls(controlsSwapped: Boolean) {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        ui.touchControls(w, h, controlsSwapped)
        input.setTouchRadius(ui.touchControls.radius)

        val touch = ui.touchControls
        val baseX = touch.move.x
        val fireX = touch.fire.x
        val baseY = h - touch.move.y
        val controlRadius = touch.radius
        val knobRadius = (controlRadius * 0.34f).coerceIn(48f, 60f)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        UiShapes.glassCircle(shapeRenderer, baseX, baseY, controlRadius, UiTheme.CYAN)
        UiShapes.glassCircle(shapeRenderer, fireX, baseY, controlRadius, UiTheme.MAGENTA)

        val knob = if (input.joystick.active) {
            input.joystick.knobForRender(h, scratchUi)
        } else {
            scratchUi.set(baseX, baseY)
        }
        val knobAccent = if (input.joystick.active) UiTheme.CYAN else UiTheme.CYAN_SOFT
        UiShapes.glassCircle(shapeRenderer, knob.x, knob.y, knobRadius, knobAccent)

        shapeRenderer.color = if (input.joystick.active) {
            UiTheme.TOUCH_KNOB_ACTIVE
        } else {
            UiTheme.TOUCH_KNOB_IDLE
        }
        shapeRenderer.circle(knob.x, knob.y, knobRadius * 0.68f, 40)

        shapeRenderer.color = if (input.firing) UiTheme.FIRE_ACTIVE else UiTheme.FIRE_IDLE
        shapeRenderer.circle(fireX, baseY, controlRadius * 0.52f, 44)
        shapeRenderer.color = Color(1f, 1f, 1f, if (input.firing) 0.30f else 0.12f)
        shapeRenderer.circle(fireX, baseY, controlRadius * 0.19f, 32)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = UiTheme.MOVE_OUTLINE
        shapeRenderer.circle(baseX, baseY, controlRadius, 56)
        shapeRenderer.color = UiTheme.FIRE_OUTLINE
        shapeRenderer.circle(fireX, baseY, controlRadius, 56)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        text.fitWithin(bodyFont, "MOVE", knobRadius * 2.5f, 28f, 0.66f, 0.44f)
        text.centered(bodyFont, "MOVE", baseX, baseY + text.height(bodyFont, "MOVE") / 2f, UiTheme.TEXT_PRIMARY)
        text.fitWithin(bodyFont, "FIRE", knobRadius * 2.5f, 28f, 0.66f, 0.44f)
        text.centered(bodyFont, "FIRE", fireX, baseY + text.height(bodyFont, "FIRE") / 2f, UiTheme.TEXT_PRIMARY)
        text.reset(bodyFont)
        batch.end()
    }

    private fun drawArenaBackdrop() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = backdropColor
        shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT)

        // Very soft atmospheric pools replace the old tactical grid.
        shapeRenderer.color = Color(0.02f, 0.42f, 0.50f, 0.045f)
        shapeRenderer.circle(GameConfig.WORLD_WIDTH * 0.12f, GameConfig.WORLD_HEIGHT * 0.78f, 210f, 48)
        shapeRenderer.color = Color(0.44f, 0.10f, 0.52f, 0.035f)
        shapeRenderer.circle(GameConfig.WORLD_WIDTH * 0.86f, GameConfig.WORLD_HEIGHT * 0.30f, 250f, 48)
        shapeRenderer.color = floorAccentColor
        shapeRenderer.rect(GameConfig.TILE, GameConfig.TILE, GameConfig.WORLD_WIDTH - GameConfig.TILE * 2f, 3f)
        shapeRenderer.end()
    }

    private fun drawWalls(walls: List<Wall>) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (wall in walls) {
            shapeRenderer.color = wall.color
            shapeRenderer.rect(wall.bounds.x, wall.bounds.y, wall.bounds.width, wall.bounds.height)
            val x = wall.bounds.x
            val y = wall.bounds.y
            val w = wall.bounds.width
            val h = wall.bounds.height
            shapeRenderer.color = wallHighlight
            shapeRenderer.rect(x + 2f, y + h - 4f, w - 4f, 2f)
            shapeRenderer.color = wallShadow
            shapeRenderer.rect(x + 2f, y + 2f, w - 4f, 2f)
        }
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        for (wall in walls) {
            val x = wall.bounds.x
            val y = wall.bounds.y
            val w = wall.bounds.width
            val h = wall.bounds.height
            shapeRenderer.color = when (wall.type) {
                WallType.STEEL -> steelLine
                WallType.BRICK, WallType.RED_BRICK -> brickLine
                WallType.CONCRETE -> concreteLine
                WallType.METAL -> metalLine
            }
            shapeRenderer.rect(x + 1f, y + 1f, w - 2f, h - 2f)
            if (wall.type == WallType.BRICK || wall.type == WallType.RED_BRICK) {
                shapeRenderer.line(x, y + h / 2f, x + w, y + h / 2f)
            }
        }
        shapeRenderer.end()
    }

    private fun drawEnemyHealthBars(enemies: List<Tank>) {
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (enemy in enemies) {
            if (!enemy.alive || enemy.health <= 0) continue
            val width = enemy.radius * 2.4f
            val y = enemy.position.y + enemy.radius + 6f
            shapeRenderer.color = healthBack
            shapeRenderer.rect(enemy.position.x - width / 2f, y, width, 3f)
            shapeRenderer.color = if (enemy.enemyTier == EnemyTier.ELITE) healthElite else healthNormal
            val ratio = (enemy.health.toFloat() / enemy.maxHealth.coerceAtLeast(1)).coerceIn(0f, 1f)
            shapeRenderer.rect(enemy.position.x - width / 2f, y, width * ratio, 3f)
        }
        shapeRenderer.end()
    }

    private fun clear(r: Float, g: Float, b: Float) {
        Gdx.gl.glClearColor(r, g, b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

}
