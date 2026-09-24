package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport

const val WORLD_WIDTH = 480f
const val WORLD_HEIGHT = 480f
const val TILE = 32f

enum class GameState { MENU, PLAYING, GAME_OVER }

class GameScreen : Screen, InputAdapter() {
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var glow: GlowRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont
    private lateinit var hudCamera: OrthographicCamera

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

    private val joystick = VirtualJoystick()
    private var firePointer = -1
    private var firing = false

    private companion object {
        const val TANK_RADIUS = 14f
        const val TANK_SEPARATION = 28f
    }

    private data class Burst(val position: Vector2, val color: Color, var t: Float = 0f)

    override fun show() {
        camera = OrthographicCamera()
        viewport = FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera)
        shapeRenderer = ShapeRenderer()
        glow = GlowRenderer(shapeRenderer)
        batch = SpriteBatch()
        font = BitmapFont().apply { data.setScale(2f) }
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
        firing = false
        firePointer = -1
        joystick.reset()

        player = Tank(
            position = Vector2(WORLD_WIDTH / 2f, 48f),
            isPlayer = true,
            color = Color(0.2f, 0.9f, 1f, 1f),
            speed = 105f,
            health = 3,
            fireRate = 0.28f
        )
        buildArena()
        nextWave()
    }

    private fun nextWave() {
        wave += 1
        val count = minOf(8, 2 + wave)
        val spacing = (WORLD_WIDTH - 96f) / count
        for (i in 0 until count) {
            val x = 48f + spacing * (i + 0.5f)
            val enemy = Tank(
                position = Vector2(x, WORLD_HEIGHT - 60f),
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
                handleMenuKeyboard()
                drawWorldIdle()
                drawMenuOverlay()
            }
            GameState.PLAYING -> {
                update(delta.coerceIn(0f, 0.05f))
                draw()
                drawHud()
            }
            GameState.GAME_OVER -> {
                handleMenuKeyboard()
                draw()
                drawGameOverOverlay()
            }
        }
    }

    private fun handleMenuKeyboard() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            startOrRestart()
        }
    }

    private fun startOrRestart() {
        if (state == GameState.GAME_OVER) resetGame()
        state = GameState.PLAYING
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
        domainBursts.forEach { it.t += delta * 0.9f }
        domainBursts.removeAll { it.t >= 1f }

        checkLaserCollisions()
        checkPowerUpPickups()
        enemies.removeAll { !it.alive }
        walls.removeAll { !it.alive }

        if (!player.alive) state = GameState.GAME_OVER
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
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) moveX -= 1f
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) moveX += 1f
            if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) moveY += 1f
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) moveY -= 1f
        }
        if (moveX != 0f || moveY != 0f) {
            val dir = Vector2(moveX, moveY).nor()
            player.angle = dir.angleDeg()
            tryMoveTank(player, dir, player.speed * delta)
        }
        val wantsFire = firing || Gdx.input.isKeyPressed(Input.Keys.SPACE)
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
                        toRemove.add(laser)
                        if (!enemy.alive) score += 100
                        break
                    }
                }
            } else if (player.alive && segmentHitsCircle(laser.previousPosition, laser.position, player.position, TANK_RADIUS)) {
                player.hit()
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
        }
        powerUps.removeAll { !it.alive }
    }

    private fun spawnBurst(position: Vector2, color: Color) {
        bursts.add(Burst(Vector2(position), Color(color)))
    }

    private fun draw() {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
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

    private fun drawWorldIdle() {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        drawWalls()
        glow.drawTank(Vector2(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f), 90f, Color(0.2f, 0.9f, 1f, 1f))
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private fun drawWalls() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (wall in walls) {
            shapeRenderer.color = if (wall.type == WallType.STEEL)
                Color(0.5f, 0.55f, 0.65f, 1f) else Color(0.6f, 0.3f, 0.15f, 1f)
            shapeRenderer.rect(wall.bounds.x, wall.bounds.y, wall.bounds.width, wall.bounds.height)
        }
        shapeRenderer.end()
    }

    private fun drawTouchControls() {
        val h = Gdx.graphics.height.toFloat()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        if (joystick.active) {
            val c = joystick.centerForRender(h)
            val k = joystick.knobForRender(h)
            shapeRenderer.color = Color(0.3f, 0.8f, 1f, 0.25f)
            shapeRenderer.circle(c.x, c.y, 60f, 24)
            shapeRenderer.color = Color(0.3f, 0.8f, 1f, 0.55f)
            shapeRenderer.circle(k.x, k.y, 24f, 20)
        }
        val fireX = Gdx.graphics.width - 90f
        val fireY = 100f
        shapeRenderer.color = if (firing) Color(1f, 0.4f, 0.4f, 0.6f) else Color(1f, 0.4f, 0.4f, 0.3f)
        shapeRenderer.circle(fireX, fireY, 55f, 24)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private fun drawHud() {
        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.color = Color.WHITE
        font.draw(batch, "Score: " + score, 20f, Gdx.graphics.height - 20f)
        font.draw(batch, "Wave: " + wave, 20f, Gdx.graphics.height - 55f)
        font.draw(batch, "Health: " + player.health, 20f, Gdx.graphics.height - 90f)
        batch.end()
    }

    private fun drawMenuOverlay() {
        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.color = Color.WHITE
        font.data.setScale(3f)
        font.draw(batch, "SPARKY WARFARE", Gdx.graphics.width / 2f - 220f, Gdx.graphics.height / 2f + 60f)
        font.data.setScale(2f)
        font.draw(batch, "Tap anywhere to start", Gdx.graphics.width / 2f - 220f, Gdx.graphics.height / 2f)
        batch.end()
    }

    private fun drawGameOverOverlay() {
        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.color = Color.RED
        font.data.setScale(3f)
        font.draw(batch, "GAME OVER", Gdx.graphics.width / 2f - 150f, Gdx.graphics.height / 2f + 60f)
        font.color = Color.WHITE
        font.data.setScale(2f)
        font.draw(batch, "Score: " + score + "  (Wave " + wave + ")", Gdx.graphics.width / 2f - 150f, Gdx.graphics.height / 2f)
        font.draw(batch, "Tap anywhere to retry", Gdx.graphics.width / 2f - 220f, Gdx.graphics.height / 2f - 40f)
        batch.end()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (state != GameState.PLAYING) {
            startOrRestart()
            return true
        }
        if (screenX < Gdx.graphics.width / 2) joystick.tryActivate(screenX.toFloat(), screenY.toFloat(), pointer)
        else {
            firePointer = pointer
            firing = true
        }
        return true
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        joystick.drag(screenX.toFloat(), screenY.toFloat(), pointer)
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        joystick.release(pointer)
        if (pointer == firePointer) {
            firing = false
            firePointer = -1
        }
        return true
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        hudCamera.setToOrtho(false, width.toFloat(), height.toFloat())
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        Gdx.input.inputProcessor = null
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
    }
}
