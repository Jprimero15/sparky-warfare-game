package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

class WorldRenderer(
    private val camera: OrthographicCamera,
    private val hudCamera: OrthographicCamera,
    private val shapeRenderer: ShapeRenderer,
    private val batch: SpriteBatch,
    private val glow: GlowRenderer,
    private val bloom: BloomRenderer,
    private val particles: ParticleDebris,
    private val font: BitmapFont,
    private val input: InputController
) {
    private val scratchUi = Vector2()
    private val layout = GlyphLayout()
    private val backdropColor = Color(0.003f, 0.005f, 0.008f, 1f)
    private val floorAccentColor = Color(0.018f, 0.028f, 0.036f, 1f)
    private val wallHighlight = Color(1f, 1f, 1f, 0.045f)
    private val wallShadow = Color(0f, 0f, 0f, 0.14f)
    private val hudTextColor = Color(0.85f, 0.95f, 1f, 0.85f)
    private val fireTextColor = Color(1f, 0.86f, 0.9f, 0.95f)

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
        if (player.alive) glow.drawTank(player.position, player.angle, player.color, player.radius)
        enemies.forEach { if (it.alive) glow.drawTank(it.position, it.angle, it.color, it.radius) }
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
        glow.drawTank(scratchUi, 45f, Color(0.2f, 0.9f, 1f, 1f))
        glow.end()
        bloom.endAndComposite()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    fun drawTouchControls(controlsSwapped: Boolean) {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val controlRadius = (h * 0.22f).coerceIn(82f, 104f)
        val leftX = controlRadius + 54f
        val baseY = controlRadius + 46f
        val rightX = w - controlRadius - 54f
        val baseX = if (controlsSwapped) rightX else leftX
        val fireX = if (controlsSwapped) leftX else rightX

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.015f, 0.02f, 0.025f, 0.72f)
        shapeRenderer.circle(baseX, baseY, controlRadius, 40)
        shapeRenderer.color = Color(0.18f, 0.72f, 1f, 0.12f)
        shapeRenderer.circle(baseX, baseY, controlRadius - 6f, 40)
        val knob = if (input.joystick.active) input.joystick.knobForRender(h, scratchUi) else scratchUi.set(baseX, baseY)
        shapeRenderer.color = Color(0.25f, 0.85f, 1f, if (input.joystick.active) 0.7f else 0.42f)
        shapeRenderer.circle(knob.x, knob.y, (controlRadius * 0.48f).coerceIn(38f, 50f), 36)

        shapeRenderer.color = Color(0.02f, 0.008f, 0.012f, 0.72f)
        shapeRenderer.circle(fireX, baseY, controlRadius, 40)
        shapeRenderer.color = if (input.firing) Color(1f, 0.22f, 0.3f, 0.76f) else Color(1f, 0.25f, 0.34f, 0.4f)
        shapeRenderer.circle(fireX, baseY, controlRadius - 16f, 40)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.65f, 0.9f, 1f, 0.55f)
        shapeRenderer.circle(baseX, baseY, controlRadius, 40)
        shapeRenderer.color = Color(1f, 0.65f, 0.7f, 0.65f)
        shapeRenderer.circle(fireX, baseY, controlRadius, 40)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        // Large, high-contrast labels stay inside the control rings and remain
        // readable on landscape phones without relying on tiny text.
        font.data.setScale(1.08f)
        drawCentered("MOVE", baseX, baseY + 10f, hudTextColor)
        drawCentered("FIRE", fireX, baseY + 10f, fireTextColor)
        batch.end()
        font.data.setScale(1f)
    }

    private fun drawArenaBackdrop() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = backdropColor
        shapeRenderer.rect(0f, 0f, GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT)
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
                WallType.STEEL -> Color(0.28f, 0.42f, 0.5f, 0.38f)
                WallType.BRICK, WallType.RED_BRICK -> Color(0.75f, 0.32f, 0.18f, 0.28f)
                WallType.CONCRETE -> Color(0.55f, 0.6f, 0.64f, 0.24f)
                WallType.METAL -> Color(0.22f, 0.62f, 0.7f, 0.28f)
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
            shapeRenderer.color = Color(0f, 0f, 0f, 0.65f)
            shapeRenderer.rect(enemy.position.x - width / 2f, y, width, 3f)
            shapeRenderer.color = if (enemy.radius >= 22f) Color(1f, 0.72f, 0.16f, 0.95f) else Color(0.35f, 0.9f, 1f, 0.9f)
            val ratio = (enemy.health.toFloat() / enemy.maxHealth.coerceAtLeast(1)).coerceIn(0f, 1f)
            shapeRenderer.rect(enemy.position.x - width / 2f, y, width * ratio, 3f)
        }
        shapeRenderer.end()
    }

    private fun clear(r: Float, g: Float, b: Float) {
        Gdx.gl.glClearColor(r, g, b, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
    }

    private fun drawCentered(text: String, centerX: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, centerX - layout.width / 2f, y)
    }
}
