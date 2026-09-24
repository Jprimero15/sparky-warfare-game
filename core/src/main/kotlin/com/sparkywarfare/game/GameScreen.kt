package com.sparkywarfare.game

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.viewport.ExtendViewport
import com.badlogic.gdx.utils.viewport.Viewport

const val WORLD_WIDTH = GameConfig.WORLD_WIDTH
const val WORLD_HEIGHT = GameConfig.WORLD_HEIGHT
const val TILE = GameConfig.TILE
const val VIEW_WIDTH = GameConfig.VIEW_WIDTH
const val VIEW_HEIGHT = GameConfig.VIEW_HEIGHT

enum class GameState { MENU, PLAYING, GAME_OVER, UPGRADE, PAUSED, SETTINGS }

private enum class UpgradeType(val title: String, val description: String) {
    OVERCLOCK("OVERCLOCK", "25% FASTER FIRE"),
    THRUSTERS("THRUSTERS", "15% MORE SPEED"),
    REPAIR("REPAIR CORE", "+1 HP"),
    SCORE_CORE("SCORE CORE", "DOUBLE COMBO VALUE")
}

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
    private var highScore = 0
    private var bestWave = 0
    private var totalKills = 0
    private var combo = 0
    private var comboTimer = 0f
    private var comboBest = 0
    private var scoreMultiplier = 1
    private val upgradeChoices = mutableListOf<UpgradeType>()
    private val upgradeButtons = Array(3) { Rectangle() }
    private val prefs by lazy { Gdx.app.getPreferences("Sparky Warfare") }
    private var screenShake = 0f
    private var hitFlash = 0f

    private val input = InputController()
    private val enemySpawner = EnemySpawner()
    private val pools = EntityPools()
    private lateinit var hud: HudRenderer
    private lateinit var safeArea: SafeArea
    private val singleButton = Rectangle()
    private val multiButton = Rectangle()
    private val settingsButton = Rectangle()
    private val pauseButton = Rectangle()
    private val resumeButton = Rectangle()
    private val menuButton = Rectangle()
    private val toggleSfxButton = Rectangle()
    private val toggleHapticsButton = Rectangle()
    private var hapticsMuted = false
    private var transition = 1f
    private var transitionTarget = 1f
    private val scratchDirection = Vector2()
    private val scratchSpawn = Vector2()
    private val scratchUi = Vector2()
    private val laserRemove = mutableListOf<Laser>()

    override fun show() {
        camera = OrthographicCamera()
        viewport = ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, camera)
        shapeRenderer = ShapeRenderer()
        glow = GlowRenderer(shapeRenderer)
        batch = SpriteBatch()
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Medium.ttf"))
        val fontParameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 30
            color = Color.WHITE
            borderWidth = 1.2f
            borderColor = Color(0f, 0f, 0f, 0.85f)
            shadowOffsetX = 1
            shadowOffsetY = 1
            shadowColor = Color(0f, 0f, 0f, 0.75f)
            characters = FreeTypeFontGenerator.DEFAULT_CHARS + "0123456789:-"
            kerning = true
            genMipMaps = true
            minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        }
        font = fontGenerator.generateFont(fontParameter)
        fontGenerator.dispose()
        layout = GlyphLayout()
        hud = HudRenderer(shapeRenderer, batch, font, layout, hudCamera)
        highScore = prefs.getInteger("highScore", 0)
        bestWave = prefs.getInteger("bestWave", 0)
        totalKills = prefs.getInteger("totalKills", 0)
        FeedbackAudio.setMuted(prefs.getBoolean("muteSfx", false))
        hapticsMuted = prefs.getBoolean("muteHaptics", false)
        FeedbackAudio.init()
        hudCamera = OrthographicCamera()
        hudCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        safeArea = hud.safeArea(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        Gdx.input.inputProcessor = this
        resetGame()
    }

    private fun resetGame() {
        enemies.clear()
        walls.clear()
        powerUps.clear()
        lasers.forEach { pools.freeLaser(it) }
        lasers.clear()
        bursts.forEach { pools.freeBurst(it) }
        bursts.clear()
        domainBursts.forEach { pools.freeBurst(it) }
        domainBursts.clear()
        pools.clear()
        score = 0
        wave = 0
        screenShake = 0f
        hitFlash = 0f
        combo = 0
        comboTimer = 0f
        comboBest = 0
        scoreMultiplier = 1
        upgradeChoices.clear()
        input.clearTransientInput()

        player = Tank(
            position = Vector2(WORLD_WIDTH / 2f, 120f),
            isPlayer = true,
            color = Color(0.2f, 0.9f, 1f, 1f),
            speed = GameConfig.Player.SPEED,
            health = GameConfig.Player.START_HP,
            fireRate = GameConfig.Player.FIRE_RATE,
            radius = GameConfig.Player.RADIUS
        )
        buildArena()
        nextWave()
        centerCamera(true)
    }

    private fun nextWave() {
        wave += 1
        enemySpawner.spawnWave(wave, player, walls, enemies)
        if (wave % 5 == 0) score += 500
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

        // Mixed cover: brick clusters, reinforced brick, concrete blocks and metal barriers.
        addWallBlock(5, 5, 4, 1, WallType.BRICK)
        addWallBlock(5, 6, 1, 3, WallType.RED_BRICK)
        addWallBlock(8, 8, 3, 1, WallType.CONCRETE)

        addWallBlock(cols - 9, 5, 4, 1, WallType.RED_BRICK)
        addWallBlock(cols - 6, 6, 1, 3, WallType.BRICK)
        addWallBlock(cols - 11, 8, 3, 1, WallType.METAL)

        addWallBlock(17, 10, 2, 3, WallType.CONCRETE)
        addWallBlock(19, 12, 3, 1, WallType.BRICK)
        addWallBlock(cols - 22, 10, 2, 3, WallType.CONCRETE)
        addWallBlock(cols - 21, 12, 3, 1, WallType.RED_BRICK)

        addWallBlock(28, 15, 4, 1, WallType.METAL)
        addWallBlock(30, 16, 1, 2, WallType.BRICK)
        addWallBlock(cols - 32, 15, 4, 1, WallType.METAL)
        addWallBlock(cols - 31, 16, 1, 2, WallType.BRICK)

        // Keep the player's starting lane open.
        walls.removeAll { it.bounds.overlaps(Rectangle(WORLD_WIDTH / 2f - 90f, 80f, 180f, 120f)) }
    }

    private fun addWallBlock(col: Int, row: Int, width: Int, height: Int, type: WallType) {
        for (x in col until col + width) {
            for (y in row until row + height) {
                if (x in 1 until (WORLD_WIDTH / TILE).toInt() - 1 &&
                    y in 1 until (WORLD_HEIGHT / TILE).toInt() - 1
                ) {
                    walls.add(wallAt(x, y, type))
                }
            }
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
                if (!input.paused) update(delta.coerceIn(0f, 0.05f))
                draw()
                drawHud()
            }
            GameState.GAME_OVER -> {
                draw()
                drawGameOverOverlay()
            }
            GameState.UPGRADE -> {
                draw()
                drawUpgradeOverlay()
            }
            GameState.PAUSED -> {
                draw()
                drawHud()
                drawPauseOverlay()
            }
            GameState.SETTINGS -> {
                drawWorldIdle()
                drawSettingsOverlay()
            }
        }
    }

    private fun startSinglePlayer() {
        resetGame()
        input.setPaused(false)
        state = GameState.PLAYING
        transition = 1f
        transitionTarget = 0f
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
        laserRemove.clear()
        for (laser in lasers) if (!laser.alive || outOfBounds(laser.position)) laserRemove.add(laser)
        laserRemove.forEach { pools.freeLaser(it) }
        lasers.removeAll(laserRemove)

        for (burst in bursts) burst.t += delta * 1.6f
        laserRemove.clear()
        for (burst in bursts) if (burst.t >= 1f) {
            pools.freeBurst(burst)
            // reused list below
        }
        bursts.removeAll { it.t >= 1f }
        screenShake = (screenShake - delta * 2.8f).coerceAtLeast(0f)
        hitFlash = (hitFlash - delta * 2.5f).coerceAtLeast(0f)
        if (comboTimer > 0f) {
            comboTimer -= delta
            if (comboTimer <= 0f) combo = 0
        }
        centerCamera(false)
        for (burst in domainBursts) burst.t += delta * 0.9f
        domainBursts.forEach { if (it.t >= 1f) pools.freeBurst(it) }
        domainBursts.removeAll { it.t >= 1f }

        checkLaserCollisions()
        checkPowerUpPickups()
        enemies.removeAll { !it.alive }
        walls.removeAll { !it.alive }

        if (!player.alive) {
            persistProgress()
            state = GameState.GAME_OVER
            FeedbackAudio.play(FeedbackAudio.Cue.EXPLOSION)
            haptic(Input.VibrationType.HEAVY)
        }
        else if (enemies.isEmpty()) {
            spawnPowerUp()
            if (wave % 3 == 0) {
                prepareUpgradeChoices()
                state = GameState.UPGRADE
                firePointers.clear()
                firing = false
                joystick.reset()
                FeedbackAudio.play(FeedbackAudio.Cue.POWER_UP)
                haptic(Input.VibrationType.MEDIUM)
            } else {
                nextWave()
            }
        }
    }

    private fun updateEnemyAi(delta: Float) {
        for (enemy in enemies) {
            if (!enemy.alive) continue
            scratchDirection.set(player.position).sub(enemy.position)
            if (scratchDirection.len2() > 1f) {
                val distance = scratchDirection.len()
                scratchDirection.nor()
                enemy.angle = scratchDirection.angleDeg()
                val factor = if (distance > 95f) 0.65f else 0.28f
                CollisionSystem.tryMoveTank(enemy, scratchDirection, enemy.speed * factor * delta, player, enemies, walls)
            }
            enemy.aiFireTimer -= delta
            if (enemy.aiFireTimer <= 0f && enemy.canFire() &&
                CollisionSystem.hasLineOfSight(enemy.position, player.position, walls)) {
                fireLaser(enemy)
                enemy.aiFireTimer = MathUtils.random(GameConfig.Enemy.AI_MIN_FIRE_DELAY, GameConfig.Enemy.AI_MAX_FIRE_DELAY)
            }
        }
    }

    private fun handleInput(delta: Float) {
        val joystick = input.joystick
        if (input.joystick.active) {
            scratchDirection.set(joystick.direction)
            if (scratchDirection.len2() > 0.0001f) {
                scratchDirection.nor()
                player.angle = scratchDirection.angleDeg()
                CollisionSystem.tryMoveTank(player, scratchDirection, player.speed * delta, player, enemies, walls)
            }
        }
        if (input.firing && player.canFire()) fireLaser(player)
    }

    private fun fireLaser(tank: Tank) {
        val rad = Math.toRadians(tank.angle.toDouble())
        scratchDirection.set(Math.cos(rad).toFloat(), Math.sin(rad).toFloat()).nor()
        scratchSpawn.set(tank.position).mulAdd(scratchDirection, tank.radius + 3f)
        lasers.add(pools.obtainLaser(scratchSpawn, scratchDirection, tank.color, tank.isPlayer))
        if (tank.hasSpreadShot()) {
            val base = tank.angle
            repeat(2) { index ->
                val offset = if (index == 0) -14f else 14f
                val a = Math.toRadians((base + offset).toDouble())
                scratchDirection.set(Math.cos(a).toFloat(), Math.sin(a).toFloat()).nor()
                scratchSpawn.set(tank.position).mulAdd(scratchDirection, tank.radius + 3f)
                lasers.add(pools.obtainLaser(scratchSpawn, scratchDirection, tank.color, tank.isPlayer))
            }
        }
        tank.fireCooldown = tank.fireRate
        if (tank.isPlayer) FeedbackAudio.play(FeedbackAudio.Cue.LASER)
    }

    private fun checkLaserCollisions() {
        laserRemove.clear()
        for (laser in lasers) {
            if (!laser.alive) continue
            var hitWall = false
            for (wall in walls) {
                if (wall.alive && Intersector.intersectSegmentRectangle(laser.previousPosition, laser.position, wall.bounds)) {
                    wall.hit()
                    spawnBurst(laser.position, laser.color)
                    hitWall = true
                    break
                }
            }
            if (hitWall) {
                laser.clear()
                laserRemove.add(laser)
                continue
            }
            if (laser.firedByPlayer) {
                for (enemy in enemies) {
                    if (enemy.alive && CollisionSystem.segmentHitsCircle(laser.previousPosition, laser.position, enemy.position, enemy.radius)) {
                        enemy.hit()
                        spawnBurst(laser.position, laser.color)
                        FeedbackAudio.play(if (enemy.alive) FeedbackAudio.Cue.HIT else FeedbackAudio.Cue.EXPLOSION)
                        laser.clear()
                        laserRemove.add(laser)
                        if (!enemy.alive) {
                            val baseScore = when {
                                enemy.radius >= 22f -> 500
                                enemy.radius >= 18f -> 250
                                enemy.radius <= 11f -> 125
                                else -> 175
                            }
                            combo = (combo + 1).coerceAtMost(GameConfig.Combat.MAX_COMBO)
                            comboBest = maxOf(comboBest, combo)
                            comboTimer = GameConfig.Combat.COMBO_TIMEOUT
                            score += baseScore * (1 + (combo - 1) / 2) * scoreMultiplier
                            totalKills += 1
                            haptic(Input.VibrationType.MEDIUM)
                        } else haptic(Input.VibrationType.LIGHT)
                        break
                    }
                }
            } else if (player.alive && CollisionSystem.segmentHitsCircle(laser.previousPosition, laser.position, player.position, player.radius)) {
                if (player.hit()) {
                    FeedbackAudio.play(FeedbackAudio.Cue.HIT)
                    haptic(Input.VibrationType.MEDIUM)
                    hitFlash = 0.28f
                    screenShake = maxOf(screenShake, 0.12f)
                    spawnBurst(laser.position, laser.color)
                }
                laser.clear()
                laserRemove.add(laser)
            }
        }
        laserRemove.forEach { pools.freeLaser(it) }
        lasers.removeAll(laserRemove)
    }

    private fun spawnPowerUp() {
        if (powerUps.any { it.alive }) return
        val types = PowerUpType.values()
        val type = types[MathUtils.random(types.size - 1)]
        repeat(12) {
            scratchSpawn.set(
                MathUtils.random(TILE * 2f, WORLD_WIDTH - TILE * 2f),
                MathUtils.random(TILE * 2f, WORLD_HEIGHT - TILE * 2f)
            )
            if (!walls.any { it.alive && it.bounds.contains(scratchSpawn.x, scratchSpawn.y) } &&
                scratchSpawn.dst2(player.position) > 80f * 80f) {
                powerUps.add(PowerUp(Vector2(scratchSpawn), type))
                return
            }
        }
    }

    private fun checkPowerUpPickups() {
        for (powerUp in powerUps) {
            if (!powerUp.alive || powerUp.position.dst2(player.position) >= GameConfig.PowerUps.PICKUP_RADIUS * GameConfig.PowerUps.PICKUP_RADIUS) continue
            powerUp.alive = false
            val burst = pools.obtainBurst(player.position, powerUp.color)
            domainBursts.add(burst)
            when (powerUp.type) {
                PowerUpType.RAPID_FIRE -> player.grantRapidFire()
                PowerUpType.SCORE_ORB -> score += 250
                PowerUpType.SHIELD -> player.grantShield()
                PowerUpType.SPREAD_SHOT -> player.grantSpreadShot()
                PowerUpType.OVERDRIVE -> player.grantOverdrive()
            }
            FeedbackAudio.play(FeedbackAudio.Cue.POWER_UP)
            haptic(Input.VibrationType.LIGHT)
        }
        powerUps.removeAll { !it.alive }
    }

    private fun spawnBurst(position: Vector2, color: Color) {
        bursts.add(pools.obtainBurst(position, color))
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
        glow.beginAdditive()
        if (player.alive) glow.drawTank(player.position, player.angle, player.color, player.radius)
        enemies.forEach { glow.drawTank(it.position, it.angle, it.color, it.radius) }
        lasers.forEach { glow.drawLaser(it) }
        bursts.forEach { glow.drawBurst(it.position, it.t, it.color) }
        powerUps.forEach { glow.drawPowerUp(it.position, it.pulse, it.color) }
        domainBursts.forEach { glow.drawDomainBurst(it.position, it.t, it.color) }
        glow.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
        drawTouchControls()
    }

    private fun drawArenaBackdrop() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.003f, 0.005f, 0.008f, 1f)
        shapeRenderer.rect(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT)
        shapeRenderer.color = Color(0.018f, 0.028f, 0.036f, 1f)
        shapeRenderer.rect(TILE, TILE, WORLD_WIDTH - TILE * 2f, 3f)
        shapeRenderer.end()
    }

    private fun drawWorldIdle() {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.06f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined
        drawArenaBackdrop()
        drawWalls()
        scratchSpawn.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f)
        glow.beginAdditive()
        glow.drawTank(scratchSpawn, 45f, Color(0.2f, 0.9f, 1f, 1f))
        glow.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private fun drawWalls() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        for (wall in walls) {
            shapeRenderer.color = wall.color
            shapeRenderer.rect(wall.bounds.x, wall.bounds.y, wall.bounds.width, wall.bounds.height)

            val x = wall.bounds.x
            val y = wall.bounds.y
            val w = wall.bounds.width
            val h = wall.bounds.height
            shapeRenderer.color = Color(1f, 1f, 1f, 0.045f)
            shapeRenderer.rect(x + 2f, y + h - 4f, w - 4f, 2f)
            shapeRenderer.color = Color(0f, 0f, 0f, 0.14f)
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

    private fun drawTouchControls() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val controlRadius = (h * 0.17f).coerceIn(58f, 74f)
        val baseX = controlRadius + 42f
        val baseY = controlRadius + 34f
        val fireX = w - controlRadius - 42f
        val fireY = baseY

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
        shapeRenderer.circle(knob.x, knob.y, 30f, 28)

        shapeRenderer.color = Color(0.02f, 0.008f, 0.012f, 0.72f)
        shapeRenderer.circle(fireX, fireY, controlRadius, 40)
        shapeRenderer.color = if (input.firing) Color(1f, 0.22f, 0.3f, 0.76f) else Color(1f, 0.25f, 0.34f, 0.4f)
        shapeRenderer.circle(fireX, fireY, controlRadius - 16f, 40)
        shapeRenderer.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.65f, 0.9f, 1f, 0.55f)
        shapeRenderer.circle(baseX, baseY, controlRadius, 40)
        shapeRenderer.color = Color(1f, 0.65f, 0.7f, 0.65f)
        shapeRenderer.circle(fireX, fireY, controlRadius, 40)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        font.data.setScale(0.9f)
        drawCentered("MOVE", baseX, baseY + 5f, Color(0.85f, 0.95f, 1f, 0.85f))
        drawCentered("FIRE", fireX, fireY + 5f, Color(1f, 0.86f, 0.9f, 0.95f))
        batch.end()
    }

    private fun persistProgress() {
        if (score > highScore) highScore = score
        if (wave > bestWave) bestWave = wave
        prefs.putInteger("highScore", highScore)
        prefs.putInteger("bestWave", bestWave)
        prefs.putInteger("totalKills", totalKills)
        prefs.flush()
    }

    private fun drawHud() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val panelW = (w * 0.43f).coerceIn(280f, 430f)
        val panelH = 92f
        val left = 18f
        val top = h - 18f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.72f)
        shapeRenderer.rect(left, top - panelH, panelW, panelH)
        shapeRenderer.color = Color(0.15f, 0.72f, 1f, 0.5f)
        shapeRenderer.rect(left, top - 2f, panelW, 2f)
        shapeRenderer.color = Color(0.06f, 0.08f, 0.1f, 0.7f)
        shapeRenderer.rect(w - 118f, h - 58f, 88f, 38f)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        fitFont("SPARKY WARFARE", panelW - 28f, 1.35f, 0.82f)
        drawShadowed("SPARKY WARFARE", left + 14f, top - 27f, Color(0.55f, 0.9f, 1f, 1f))
        font.data.setScale(0.94f)
        drawShadowed("SCORE " + score, left + 14f, top - 57f, Color.WHITE)
        drawRight("WAVE " + wave, left + panelW - 14f, top - 57f, Color(0.72f, 0.82f, 0.9f, 1f))
        font.data.setScale(0.68f)
        drawShadowed("BEST " + highScore, left + 14f, top - 79f, Color(0.46f, 0.72f, 0.8f, 1f))
        drawRight("COMBO x" + combo, left + panelW - 14f, top - 79f,
            if (combo >= 3) Color(1f, 0.78f, 0.2f, 1f) else Color(0.46f, 0.58f, 0.64f, 1f))
        font.data.setScale(0.82f)
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

    private fun fitFont(text: String, maxWidth: Float, preferred: Float, minimum: Float): Float {
        font.data.setScale(1f)
        layout.setText(font, text)
        val scale = if (layout.width > 0f) (maxWidth / layout.width).coerceAtMost(preferred) else preferred
        font.data.setScale(scale.coerceAtLeast(minimum))
        layout.setText(font, text)
        return font.data.scaleX
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
        singleButton.set(centerX - buttonW / 2f, h * 0.34f, buttonW, buttonH)
        multiButton.set(centerX - buttonW / 2f, h * 0.18f, buttonW, buttonH)

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
        fitFont("SPARKY WARFARE", w * 0.82f, 3.15f, 1.65f)
        drawShadowed("SPARKY WARFARE", centerX - layout.width / 2f, h * 0.80f, Color(0.58f, 0.92f, 1f, 1f))
        fitFont("TACTICAL ENERGY COMBAT", w * 0.78f, 1.05f, 0.72f)
        drawCentered("TACTICAL ENERGY COMBAT", centerX, h * 0.68f, Color(0.5f, 0.62f, 0.68f, 1f))
        fitFont("BEST 000000   •   WAVE 00   •   KILLS 0000", w * 0.82f, 0.78f, 0.56f)
        drawCentered("BEST " + highScore + "   •   WAVE " + bestWave + "   •   KILLS " + totalKills,
            centerX, h * 0.61f, Color(0.42f, 0.58f, 0.64f, 1f))
        fitFont("SINGLE PLAYER", singleButton.width - 24f, 1.25f, 0.78f)
        drawCentered("SINGLE PLAYER", centerX, singleButton.y + singleButton.height / 2f + 7f, Color.WHITE)
        fitFont("MULTIPLAYER", multiButton.width - 24f, 1.25f, 0.78f)
        drawCentered("MULTIPLAYER", centerX, multiButton.y + multiButton.height / 2f + 7f, Color(0.84f, 0.88f, 0.92f, 1f))
        font.data.setScale(0.68f)
        drawCentered("COMING SOON", centerX, multiButton.y + 12f, Color(0.38f, 0.46f, 0.5f, 1f))
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
        val panelH = (h * 0.46f).coerceIn(230f, 350f)
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
        fitFont("GAME OVER", panelW - 40f, 2.45f, 1.4f)
        drawCentered("GAME OVER", w / 2f, bottom + panelH - 58f, Color(1f, 0.28f, 0.34f, 1f))
        fitFont("SCORE 000000    BEST 000000", panelW - 40f, 1.12f, 0.78f)
        drawCentered("SCORE  " + score + "    BEST  " + highScore, w / 2f, bottom + panelH / 2f + 8f, Color.WHITE)
        fitFont("WAVE 00    KILLS 0000    COMBO x8", panelW - 40f, 0.78f, 0.56f)
        drawCentered("WAVE " + wave + "    KILLS " + totalKills + "    COMBO x" + comboBest,
            w / 2f, bottom + panelH / 2f - 20f, Color(0.56f, 0.66f, 0.72f, 1f))
        fitFont("TAP ANYWHERE TO RETRY", panelW - 40f, 0.98f, 0.68f)
        drawCentered("TAP ANYWHERE TO RETRY", w / 2f, bottom + 44f, Color(0.7f, 0.78f, 0.84f, 1f))
        batch.end()
    }

    private fun prepareUpgradeChoices() {
        upgradeChoices.clear()
        val pool = UpgradeType.values().toMutableList()
        while (upgradeChoices.size < 3 && pool.isNotEmpty()) {
            val index = MathUtils.random(pool.size - 1)
            upgradeChoices.add(pool.removeAt(index))
        }
    }

    private fun applyUpgrade(type: UpgradeType) {
        when (type) {
            UpgradeType.OVERCLOCK -> player.fireRate = (player.fireRate * 0.75f).coerceAtLeast(0.11f)
            UpgradeType.THRUSTERS -> player.speed *= 1.15f
            UpgradeType.REPAIR -> player.health = (player.health + 1).coerceAtMost(4)
            UpgradeType.SCORE_CORE -> scoreMultiplier = 2
        }
    }

    private fun drawUpgradeOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val centerX = w / 2f
        val cardW = (w * 0.27f).coerceIn(190f, 300f)
        val cardH = (h * 0.42f).coerceIn(210f, 310f)
        val gap = (w * 0.025f).coerceIn(12f, 28f)
        val totalW = cardW * 3f + gap * 2f
        val left = centerX - totalW / 2f
        val bottom = (h - cardH) / 2f - 4f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.92f)
        shapeRenderer.rect(0f, 0f, w, h)
        shapeRenderer.color = Color(0.03f, 0.16f, 0.2f, 0.2f)
        shapeRenderer.rect(0f, h * 0.72f, w, h * 0.28f)
        for (i in 0 until 3) {
            val x = left + i * (cardW + gap)
            upgradeButtons[i].set(x, bottom, cardW, cardH)
            shapeRenderer.color = Color(0.035f, 0.055f, 0.07f, 0.96f)
            shapeRenderer.rect(x, bottom, cardW, cardH)
            shapeRenderer.color = Color(0.15f, 0.72f, 1f, 0.7f)
            shapeRenderer.rect(x, bottom + cardH - 3f, cardW, 3f)
        }
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        fitFont("CHOOSE YOUR UPGRADE", w * 0.72f, 1.8f, 1.0f)
        drawCentered("CHOOSE YOUR UPGRADE", centerX, h * 0.86f, Color(0.58f, 0.92f, 1f, 1f))
        font.data.setScale(0.65f)
        drawCentered("WAVE " + wave + " COMPLETE", centerX, h * 0.79f, Color(0.46f, 0.62f, 0.68f, 1f))
        for (i in 0 until minOf(3, upgradeChoices.size)) {
            val rect = upgradeButtons[i]
            val choice = upgradeChoices[i]
            fitFont(choice.title, rect.width - 24f, 1.08f, 0.68f)
            drawCentered(choice.title, rect.x + rect.width / 2f, rect.y + rect.height - 52f, Color.WHITE)
            fitFont(choice.description, rect.width - 24f, 0.7f, 0.52f)
            drawCentered(choice.description, rect.x + rect.width / 2f, rect.y + rect.height / 2f + 6f,
                Color(0.45f, 0.72f, 0.8f, 1f))
            font.data.setScale(0.58f)
            drawCentered("TAP TO SELECT", rect.x + rect.width / 2f, rect.y + 26f, Color(0.38f, 0.52f, 0.58f, 1f))
        }
        batch.end()
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (state == GameState.MENU) {
            if (toUiRect(screenX, screenY, singleButton)) startSinglePlayer()
            else if (toUiRect(screenX, screenY, settingsButton)) state = GameState.SETTINGS
            else if (toUiRect(screenX, screenY, multiButton)) FeedbackAudio.play(FeedbackAudio.Cue.UI)
            return true
        }
        if (state == GameState.SETTINGS) {
            if (toUiRect(screenX, screenY, toggleSfxButton)) {
                FeedbackAudio.setMuted(!FeedbackAudio.isMuted())
                prefs.putBoolean("muteSfx", FeedbackAudio.isMuted()).flush()
            } else if (toUiRect(screenX, screenY, toggleHapticsButton)) {
                hapticsMuted = !hapticsMuted
                prefs.putBoolean("muteHaptics", hapticsMuted).flush()
            } else if (toUiRect(screenX, screenY, menuButton)) {
                state = GameState.MENU
            }
            return true
        }
        if (state == GameState.GAME_OVER) {
            if (toUiRect(screenX, screenY, menuButton)) state = GameState.MENU
            else if (toUiRect(screenX, screenY, singleButton)) startSinglePlayer()
            return true
        }
        if (state == GameState.UPGRADE) {
            for (i in 0 until minOf(3, upgradeChoices.size)) {
                if (toUiRect(screenX, screenY, upgradeButtons[i])) {
                    applyUpgrade(upgradeChoices[i])
                    FeedbackAudio.play(FeedbackAudio.Cue.POWER_UP)
                    haptic(Input.VibrationType.MEDIUM)
                    upgradeChoices.clear()
                    state = GameState.PLAYING
                    input.setPaused(false)
                    nextWave()
                    return true
                }
            }
            return true
        }
        if (state == GameState.PAUSED) {
            if (toUiRect(screenX, screenY, resumeButton)) {
                input.setPaused(false)
                state = GameState.PLAYING
            } else if (toUiRect(screenX, screenY, menuButton)) {
                input.setPaused(false)
                state = GameState.MENU
            }
            return true
        }
        if (state == GameState.PLAYING) {
            if (toUiRect(screenX, screenY, pauseButton)) {
                input.setPaused(true)
                state = GameState.PAUSED
                return true
            }
            if (screenX < Gdx.graphics.width / 2) {
                input.joystick.tryActivate(screenX.toFloat(), screenY.toFloat(), pointer)
                input.joystick.drag(screenX.toFloat(), screenY.toFloat(), pointer)
            } else {
                input.pressFire(pointer)
            }
            return true
        }
        return true
    }

    private fun toUiRect(screenX: Int, screenY: Int, rect: Rectangle): Boolean {
        val uiY = (Gdx.graphics.height - screenY).toFloat()
        return rect.contains(screenX.toFloat(), uiY)
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        input.drag(screenX, screenY, pointer)
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        input.release(pointer)
        return true
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean =
        touchUp(screenX, screenY, pointer, button)

    private fun haptic(type: Input.VibrationType) {
        if (!hapticsMuted && Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator)) Gdx.input.vibrate(type)
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
        hudCamera.setToOrtho(false, width.toFloat(), height.toFloat())
        safeArea = hud.safeArea(width.toFloat(), height.toFloat())
        centerCamera(true)
    }

    override fun pause() {
        input.clearTransientInput()
        if (state == GameState.PLAYING) {
            input.setPaused(true)
            state = GameState.PAUSED
        }
    }

    override fun resume() {
        if (state == GameState.PAUSED) {
            input.setPaused(false)
            state = GameState.PLAYING
        }
    }

    override fun hide() {}

    override fun dispose() {
        Gdx.input.inputProcessor = null
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
        pools.clear()
        FeedbackAudio.dispose()
    }
}
