package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport

const val WORLD_WIDTH = 1600f
const val WORLD_HEIGHT = 900f
const val TILE = 32f
const val VIEW_WIDTH = 480f
const val VIEW_HEIGHT = 270f

enum class GameState { MENU, PLAYING, GAME_OVER }

class GameScreen : Screen, InputAdapter() {
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var glow: GlowRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont
    private lateinit var hudCamera: OrthographicCamera
    private lateinit var layout: GlyphLayout

    private lateinit var player: Tank
    private val enemies = mutableListOf<Tank>()
    private val lasers = mutableListOf<Laser>()
    private val walls = mutableListOf<Wall>()
    private val bursts = mutableListOf<Burst>()
    private val powerUps = mutableListOf<PowerUp>()
    private val domainBursts = mutableListOf<Burst>()

    private var state = GameState.MENU
    private var score = 0
    private var wave = 0
    private var screenShake = 0f
    private var hitFlash = 0f

    private val joystick = VirtualJoystick()
    private val firePointers = mutableSetOf<Int>()
    private var firing = false
    private val singleButton = Rectangle()
    private val multiButton = Rectangle()

    private companion object {
        const val TANK_RADIUS = 14f
        const val TANK_SEPARATION = 28f
    }

    private data class Burst(val position: Vector2, val color: Color, var t: Float = 0f)

    override fun show() {
        camera = OrthographicCamera()
        viewport = ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, camera)
        shapeRenderer = ShapeRenderer()
        glow = GlowRenderer(shapeRenderer)
        batch = SpriteBatch()
        font = BitmapFont().apply {
            data.setScale(2f)
            setUseIntegerPositions(false)
            setFixedWidthGlyphs("ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789:-")
        }
        layout = GlyphLayout()
        FeedbackAudio.init()
        hudCamera = OrthographicCamera()
        hudCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        Gdx.input.inputProcessor = this
        resetGame()
    }

    private fun resetGame() {
        enemies.clear(); lasers.clear(); walls.clear(); bursts.clear()
        powerUps.clear(); domainBursts.clear()
        score = 0
        wave = 0
        screenShake = 0f
        hitFlash = 0f
        firePointers.clear()
        firing = false
        joystick.reset()

        player = Tank(
            position = Vector2(WORLD_WIDTH / 2f, 120f),
            isPlayer = true,
            color = Color(0.2f, 0.9f, 1f, 1f),
            speed = 115f,
            health = 3,
            fireRate = 0.28f
        )
        buildArena()
        nextWave()
        centerCamera(true)
    }

    private fun nextWave() {
        wave += 1
        val count = minOf(10, 2 + wave)
        val spawnY = (player.position.y + VIEW_HEIGHT * 0.65f).coerceAtMost(WORLD_HEIGHT - 70f)
        val left = (player.position.x - VIEW_WIDTH * 0.9f).coerceAtLeast(70f)
        val right = (player.position.x + VIEW_WIDTH * 0.9f).coerceAtMost(WORLD_WIDTH - 70f)
        val spacing = ((right - left) / (count + 1)).coerceAtLeast(42f)
        for (i in 0 until count) {
            val x = (left + spacing * (i + 1)).coerceIn(60f, WORLD_WIDTH - 60f)
            val enemy = Tank(
                position = Vector2(x, spawnY),
                angle = 270f,
                color = Color(1f, 0.25f, 0.35f, 1f),
                speed = (55f + wave * 3f).coerceAtMost(95f),
                health = 1 + (wave - 1) / 4,
                fireRate = (1.15f - wave * 0.035f).coerceAtLeast(0.55f)
            )
            enemy.aiFireTimer = MathUtils.random(0.35f, 1.25f)
            enemies.add(enemy)
        }
    }

    private fun buildArena() {
        walls.clear()
        val cols = (WORLD_WIDTH / TILE).toInt()
        val rows = (WORLD_HEIGHT / TILE).toInt()
        for (c in 0 until cols) {
            walls.add(wallAt(c, 0, WallType.STEEL))
            walls.add(wallAt(c, rows - 1, WallType.STEEL))
        }
        for (r in 0 until rows) {
            walls.add(wallAt(0, r, WallType.STEEL))
            walls.add(wallAt(cols - 1, r, WallType.STEEL))
        }
        for (r in 4..8) {
            walls.add(wallAt(6, r, WallType.BRICK))
            walls.add(wallAt(cols - 7, r, WallType.BRICK))
        }
    }

    private fun wallAt(col: Int, row: Int, type: WallType) =
        Wall(Rectangle(col * TILE, row * TILE, TILE, TILE), type)

    override fun render(delta: Float) {
        when (state) {
            GameState.MENU -> {
                drawWorldIdle()
                drawMenuOverlay()
            }
            GameState.PLAYING -> {
                update(delta.coerceIn(0f, 0.05f))
                draw()
                drawHud()
            }
            GameState.GAME_OVER -> {
                draw()
                drawGameOverOverlay()
            }
        }
    }

    private fun startSinglePlayer() {
        resetGame()
        state = GameState.PLAYING
        FeedbackAudio.play(FeedbackAudio.Cue.UI)
        haptic(Input.VibrationType.LIGHT)
    }

    private fun startOrRestart() {
        startSinglePlayer()
    }

    private fun update(delta: Float) {
        handleInput(delta)
        player.update(delta)
        enemies.forEach { it.update(delta) }
        powerUps.forEach { it.update(delta) }
        updateEnemyAi(delta)

        lasers.forEach { it.update(delta) }
        lasers.removeAll { !it.alive || outOfBounds(it.position) }

        bursts.forEach { it.t += delta * 1.6f }
        bursts.removeAll { it.t >= 1f }
        screenShake = (screenShake - delta * 2.8f).coerceAtLeast(0f)
        hitFlash = (hitFlash - delta * 2.5f).coerceAtLeast(0f)
        centerCamera(false)
        domainBursts.forEach { it.t += delta * 0.9f }
        domainBursts.removeAll { it.t >= 1f }

        checkLaserCollisions()
        checkPowerUpPickups()
        enemies.removeAll { !it.alive }
        walls.removeAll { !it.alive }

        if (!player.alive) {
            state = GameState.GAME_OVER
            FeedbackAudio.play(FeedbackAudio.Cue.EXPLOSION)
            haptic(Input.VibrationType.HEAVY)
        }
        else if (enemies.isEmpty()) {
            spawnPowerUp()
            nextWave()
        }
    }

    private fun updateEnemyAi(delta: Float) {
        for (enemy in enemies) {
            if (!enemy.alive) continue
            val toPlayer = Vector2(player.position).sub(enemy.position)
            if (toPlayer.len2() > 1f) {
                val distance = toPlayer.len()
                val direction = toPlayer.nor()
                enemy.angle = direction.angleDeg()
                val factor = if (distance > 95f) 0.65f else 0.28f
                tryMoveTank(enemy, direction, enemy.speed * factor * delta)
            }
            enemy.aiFireTimer -= delta
            if (enemy.aiFireTimer <= 0f && enemy.canFire()) {
                fireLaser(enemy)
                enemy.aiFireTimer = MathUtils.random(0.75f, 1.55f)
            }
        }
    }

    private fun handleInput(delta: Float) {
        var moveX = 0f
        var moveY = 0f
        if (joystick.active) {
            moveX = joystick.direction.x
            moveY = joystick.direction.y
        }
        if (moveX != 0f || moveY != 0f) {
            val dir = Vector2(moveX, moveY).nor()
            player.angle = dir.angleDeg()
            tryMoveTank(player, dir, player.speed * delta)
        }
        val wantsFire = firing
        if (wantsFire && player.canFire()) fireLaser(player)
    }

    private fun tryMoveTank(tank: Tank, direction: Vector2, distance: Float) {
        if (distance <= 0f) return
        val nextX = Vector2(tank.position).add(direction.x * distance, 0f)
        if (canOccupy(tank, nextX)) tank.position.set(nextX)
        val nextY = Vector2(tank.position).add(0f, direction.y * distance)
        if (canOccupy(tank, nextY)) tank.position.set(nextY)
        tank.position.x = MathUtils.clamp(tank.position.x, TILE + 1f, WORLD_WIDTH - TILE - 1f)
        tank.position.y = MathUtils.clamp(tank.position.y, TILE + 1f, WORLD_HEIGHT - TILE - 1f)
    }

    private fun canOccupy(tank: Tank, position: Vector2): Boolean {
        if (walls.any { it.alive && circleIntersectsRectangle(position, TANK_RADIUS, it.bounds) }) return false
        if (enemies.any { it !== tank && it.alive && it.position.dst2(position) < TANK_SEPARATION * TANK_SEPARATION }) return false
        return tank === player || !player.alive ||
            player.position.dst2(position) >= TANK_SEPARATION * TANK_SEPARATION
    }

    private fun circleIntersectsRectangle(center: Vector2, radius: Float, rect: Rectangle): Boolean {
        val closestX = MathUtils.clamp(center.x, rect.x, rect.x + rect.width)
        val closestY = MathUtils.clamp(center.y, rect.y, rect.y + rect.height)
        val dx = center.x - closestX
        val dy = center.y - closestY
        return dx * dx + dy * dy < radius * radius
    }

    private fun outOfBounds(p: Vector2) =
        p.x < TILE || p.y < TILE || p.x > WORLD_WIDTH - TILE || p.y > WORLD_HEIGHT - TILE

    private fun fireLaser(tank: Tank) {
        val rad = Math.toRadians(tank.angle.toDouble())
        val dir = Vector2(Math.cos(rad).toFloat(), Math.sin(rad).toFloat()).nor()
        val start = Vector2(tank.position).mulAdd(dir, TANK_RADIUS + 3f)
        lasers.add(Laser(start, dir, Color(tank.color), firedByPlayer = tank.isPlayer))
        tank.fireCooldown = tank.fireRate
        if (tank.isPlayer) FeedbackAudio.play(FeedbackAudio.Cue.LASER)
    }

    private fun checkLaserCollisions() {
        val toRemove = mutableSetOf<Laser>()
        for (laser in lasers) {
            if (!laser.alive) continue
            for (wall in walls) {
                if (wall.alive && Intersector.intersectSegmentRectangle(laser.previousPosition, laser.position, wall.bounds)) {
                    wall.hit()
                    spawnBurst(laser.position, laser.color)
                    toRemove.add(laser)
                    break
                }
            }
            if (laser in toRemove) continue
            if (laser.firedByPlayer) {
                for (enemy in enemies) {
                    if (enemy.alive && segmentHitsCircle(laser.previousPosition, laser.position, enemy.position, TANK_RADIUS)) {
                        enemy.hit()
                        spawnBurst(laser.position, laser.color)
                        FeedbackAudio.play(if (enemy.alive) FeedbackAudio.Cue.HIT else FeedbackAudio.Cue.EXPLOSION)
                        toRemove.add(laser)
                        if (!enemy.alive) { score += 100; haptic(Input.VibrationType.MEDIUM) } else haptic(Input.VibrationType.LIGHT)
                        break
                    }
                }
            } else if (player.alive && segmentHitsCircle(laser.previousPosition, laser.position, player.position, TANK_RADIUS)) {
                player.hit()
                FeedbackAudio.play(FeedbackAudio.Cue.HIT)
                haptic(Input.VibrationType.MEDIUM)
                hitFlash = 0.28f
                screenShake = maxOf(screenShake, 0.12f)
                spawnBurst(laser.position, laser.color)
                toRemove.add(laser)
            }
        }
        lasers.removeAll(toRemove)
    }

    private fun segmentHitsCircle(start: Vector2, end: Vector2, center: Vector2, radius: Float): Boolean {
        val closest = Vector2()
        Intersector.nearestSegmentPoint(start, end, center, closest)
        return closest.dst2(center) <= radius * radius
    }

    private fun spawnPowerUp() {
        if (powerUps.any { it.alive }) return
        val type = if (MathUtils.randomBoolean()) PowerUpType.RAPID_FIRE else PowerUpType.SCORE_ORB
        repeat(8) {
            val position = Vector2(
                MathUtils.random(TILE * 2f, WORLD_WIDTH - TILE * 2f),
                MathUtils.random(TILE * 2f, WORLD_HEIGHT - TILE * 2f)
            )
            if (!collidesWithWalls(position) && position.dst(player.position) > 80f) {
                powerUps.add(PowerUp(position, type))
                return
            }
        }
    }

    private fun collidesWithWalls(point: Vector2) =
        walls.any { it.alive && it.bounds.contains(point.x, point.y) }

    private fun checkPowerUpPickups() {
        val collected = powerUps.filter { it.alive && it.position.dst(player.position) < 20f }
        for (powerUp in collected) {
            powerUp.alive = false
            domainBursts.add(Burst(Vector2(player.position), powerUp.color))
            when (powerUp.type) {
                PowerUpType.RAPID_FIRE -> player.grantRapidFire()
                PowerUpType.SCORE_ORB -> score += 250
            }
            FeedbackAudio.play(FeedbackAudio.Cue.POWER_UP)
            haptic(Input.VibrationType.LIGHT)
        }
        powerUps.removeAll { !it.alive }
    }

    private fun spawnBurst(position: Vector2, color: Color) {
        bursts.add(Burst(Vector2(position), Color(color)))
    }

    private fun centerCamera(instant: Boolean) {
        val halfW = camera.viewportWidth / 2f
        val halfH = camera.viewportHeight / 2f
        val targetX = player.position.x
        val targetY = player.position.y + 35f
        val x = targetX.coerceIn(halfW, WORLD_WIDTH - halfW)
        val y = targetY.coerceIn(halfH, WORLD_HEIGHT - halfH)
        if (instant) {
            camera.position.set(x, y, 0f)
        } else {
            camera.position.x = MathUtils.lerp(camera.position.x, x, 0.12f)
            camera.position.y = MathUtils.lerp(camera.position.y, y, 0.12f)
        }
        if (screenShake > 0f) {
            camera.position.x += MathUtils.random(-1f, 1f) * screenShake * 20f
            camera.position.y += MathUtils.random(-1f, 1f) * screenShake * 20f
        }
        camera.update()
    }

    private fun draw() {
        Gdx.gl.glClearColor(0.001f, 0.002f, 0.003f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        drawArenaBackdrop()
        drawWalls()
        if (player.alive) glow.drawTank(player.position, player.angle, player.color)
        enemies.forEach { glow.drawTank(it.position, it.angle, it.color) }
        lasers.forEach { glow.drawLaser(it) }
        bursts.forEach { glow.drawBurst(it.position, it.t, it.color) }
        powerUps.forEach { glow.drawPowerUp(it.position, it.pulse, it.color) }
        domainBursts.forEach { glow.drawDomainBurst(it.position, it.t, it.color) }
        Gdx.gl.glDisable(GL20.GL_BLEND)
        drawTouchControls()
    }

    private fun drawArenaBackdrop() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.003f, 0.005f, 0.008f, 1f)
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        shapeRenderer.color = Color(0.08f, 0.12f, 0.15f, 0.16f)
        for (x in 32 until WORLD_WIDTH.toInt() step 32) shapeRenderer.rect(x.toFloat(), 0f, 1f, WORLD_HEIGHT)
        for (y in 32 until WORLD_HEIGHT.toInt() step 32) shapeRenderer.rect(0f, y.toFloat(), WORLD_WIDTH, 1f)
        shapeRenderer.end()
    }

    private fun drawWorldIdle() {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        drawArenaBackdrop()
        drawWalls()
        glow.drawTank(Vector2(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f), 45f, Color(0.2f, 0.9f, 1f, 1f))
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private fun drawWalls() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (wall in walls) {
            shapeRenderer.color = if (wall.type == WallType.STEEL)
                Color(0.11f, 0.13f, 0.16f, 1f) else Color(0.18f, 0.075f, 0.045f, 1f)
            shapeRenderer.rect(wall.bounds.x, wall.bounds.y, wall.bounds.width, wall.bounds.height)
        }
        shapeRenderer.end()
    }

    private fun drawTouchControls() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val baseX = 118f
        val baseY = 118f
        val fireX = w - 118f
        val fireY = 118f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color(0.015f, 0.02f, 0.025f, 0.72f)
        shapeRenderer.circle(baseX, baseY, 74f, 40)
        shapeRenderer.color = Color(0.18f, 0.72f, 1f, 0.12f)
        shapeRenderer.circle(baseX, baseY, 68f, 40)
        val knob = if (joystick.active) joystick.knobForRender(h) else Vector2(baseX, baseY)
        shapeRenderer.color = Color(0.25f, 0.85f, 1f, if (joystick.active) 0.7f else 0.42f)
        shapeRenderer.circle(knob.x, knob.y, 30f, 28)

        shapeRenderer.color = Color(0.02f, 0.008f, 0.012f, 0.72f)
        shapeRenderer.circle(fireX, fireY, 74f, 40)
        shapeRenderer.color = if (firing) Color(1f, 0.22f, 0.3f, 0.76f) else Color(1f, 0.25f, 0.34f, 0.4f)
        shapeRenderer.circle(fireX, fireY, 58f, 40)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.65f, 0.9f, 1f, 0.55f)
        shapeRenderer.circle(baseX, baseY, 74f, 40)
        shapeRenderer.color = Color(1f, 0.65f, 0.7f, 0.65f)
        shapeRenderer.circle(fireX, fireY, 74f, 40)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.data.setScale(1.2f)
        font.color = Color(0.85f, 0.95f, 1f, 0.85f)
        font.draw(batch, "MOVE", baseX - 28f, baseY + 5f)
        font.color = Color(1f, 0.86f, 0.9f, 0.95f)
        font.draw(batch, "FIRE", fireX - 23f, fireY + 5f)
        batch.end()
    }

    private fun drawHud() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val panelW = 230f
        val panelH = 86f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.72f)
        shapeRenderer.rect(18f, h - panelH - 18f, panelW, panelH)
        shapeRenderer.color = Color(0.15f, 0.72f, 1f, 0.5f)
        shapeRenderer.rect(18f, h - 20f, panelW, 2f)
        shapeRenderer.color = Color(0.06f, 0.08f, 0.1f, 0.7f)
        shapeRenderer.rect(w - 118f, h - 58f, 88f, 38f)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.data.setScale(1.35f)
        drawShadowed("SPARKY WARFARE", 32f, h - 34f, Color(0.55f, 0.9f, 1f, 1f))
        font.data.setScale(1.08f)
        drawShadowed("SCORE  " + score, 32f, h - 62f, Color.WHITE)
        drawShadowed("WAVE   " + wave, 132f, h - 62f, Color(0.72f, 0.82f, 0.9f, 1f))
        drawRight("HP " + player.health, w - 42f, h - 34f, Color(0.95f, 0.35f, 0.42f, 1f))
        batch.end()

        if (hitFlash > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.projectionMatrix = hudCamera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(1f, 0.04f, 0.08f, hitFlash * 0.28f)
            shapeRenderer.rect(0f, 0f, w, h)
            shapeRenderer.end()
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }
    }

    private fun drawShadowed(text: String, x: Float, y: Float, color: Color) {
        font.color = Color(0f, 0f, 0f, 0.8f)
        font.draw(batch, text, x + 2f, y - 2f)
        font.color = color
        font.draw(batch, text, x, y)
    }

    private fun drawCentered(text: String, centerX: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, centerX - layout.width / 2f, y)
    }

    private fun drawRight(text: String, rightX: Float, y: Float, color: Color) {
        layout.setText(font, text)
        font.color = color
        font.draw(batch, text, rightX - layout.width, y)
    }

    private fun drawMenuOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val buttonW = (w * 0.64f).coerceIn(300f, 520f)
        val buttonH = (h * 0.13f).coerceIn(58f, 82f)
        val centerX = w / 2f
        singleButton.set(centerX - buttonW / 2f, h * 0.36f, buttonW, buttonH)
        multiButton.set(centerX - buttonW / 2f, h * 0.19f, buttonW, buttonH)

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.9f)
        shapeRenderer.rect(0f, 0f, w, h)
        shapeRenderer.color = Color(0.03f, 0.16f, 0.2f, 0.22f)
        shapeRenderer.rect(0f, h * 0.78f, w, h * 0.22f)
        drawButton(singleButton, Color(0.02f, 0.32f, 0.46f, 0.78f))
        drawButton(multiButton, Color(0.055f, 0.065f, 0.075f, 0.86f))
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.data.setScale(3.15f)
        drawShadowed("SPARKY WARFARE", centerX - 0f, h * 0.78f, Color(0.58f, 0.92f, 1f, 1f))
        font.data.setScale(1.05f)
        drawCentered("TACTICAL ENERGY COMBAT", centerX, h * 0.68f, Color(0.5f, 0.62f, 0.68f, 1f))
        font.data.setScale(1.25f)
        drawCentered("SINGLE PLAYER", centerX, singleButton.y + singleButton.height / 2f + 7f, Color.WHITE)
        drawCentered("MULTIPLAYER", centerX, multiButton.y + multiButton.height / 2f + 7f, Color(0.84f, 0.88f, 0.92f, 1f))
        font.data.setScale(0.8f)
        drawCentered("COMING SOON", centerX, multiButton.y + 14f, Color(0.38f, 0.46f, 0.5f, 1f))
        batch.end()
    }

    private fun drawButton(rect: Rectangle, color: Color) {
        shapeRenderer.color = color
        shapeRenderer.rect(rect.x, rect.y, rect.width, rect.height)
        shapeRenderer.color = Color(0.55f, 0.9f, 1f, 0.55f)
        shapeRenderer.rect(rect.x, rect.y + rect.height - 3f, rect.width, 3f)
    }

    private fun drawGameOverOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val panelW = (w * 0.72f).coerceIn(340f, 620f)
        val panelH = (h * 0.46f).coerceIn(220f, 340f)
        val left = (w - panelW) / 2f
        val bottom = (h - panelH) / 2f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.9f)
        shapeRenderer.rect(0f, 0f, w, h)
        shapeRenderer.color = Color(0.14f, 0.015f, 0.025f, 0.82f)
        shapeRenderer.rect(left, bottom, panelW, panelH)
        shapeRenderer.color = Color(1f, 0.12f, 0.2f, 0.72f)
        shapeRenderer.rect(left, bottom + panelH - 3f, panelW, 3f)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.data.setScale(2.45f)
        drawCentered("GAME OVER", w / 2f, bottom + panelH - 66f, Color(1f, 0.28f, 0.34f, 1f))
        font.data.setScale(1.12f)
        drawCentered("SCORE  " + score + "    WAVE  " + wave, w / 2f, bottom + panelH / 2f + 4f, Color.WHITE)
        font.data.setScale(0.98f)
        drawCentered("TAP ANYWHERE TO RETRY", w / 2f, bottom + 48f, Color(0.7f, 0.78f, 0.84f, 1f))
        batch.end()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (state == GameState.MENU) {
            if (toUiRect(screenX, screenY, singleButton)) startSinglePlayer()
            else if (toUiRect(screenX, screenY, multiButton)) FeedbackAudio.play(FeedbackAudio.Cue.UI)
            return true
        }
        if (state == GameState.GAME_OVER) {
            startSinglePlayer()
            return true
        }
        if (screenX < Gdx.graphics.width / 2) {
            joystick.tryActivate(screenX.toFloat(), screenY.toFloat(), pointer)
            joystick.drag(screenX.toFloat(), screenY.toFloat(), pointer)
        } else {
            firePointers.add(pointer)
            firing = true
        }
        return true
    }

    private fun toUiRect(screenX: Int, screenY: Int, rect: Rectangle): Boolean {
        val uiY = (Gdx.graphics.height - screenY).toFloat()
        return rect.contains(screenX.toFloat(), uiY)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        joystick.drag(screenX.toFloat(), screenY.toFloat(), pointer)
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        joystick.release(pointer)
        if (firePointers.remove(pointer)) {
            firing = firePointers.isNotEmpty()
        }
        return true
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int) = touchUp(screenX, screenY, pointer, button)

    private fun haptic(type: Input.VibrationType) {
        if (Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator)) Gdx.input.vibrate(type)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        hudCamera.setToOrtho(false, width.toFloat(), height.toFloat())
        centerCamera(true)
    }

    override fun pause() {
        firePointers.clear()
        firing = false
        joystick.reset()
    }
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        Gdx.input.inputProcessor = null
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
        FeedbackAudio.dispose()
    }
}
