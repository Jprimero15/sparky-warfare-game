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

enum class UpgradeType(val title: String, val description: String, val maxStacks: Int) {
    OVERCLOCK("OVERCLOCK", "25% FASTER FIRE", 3),
    THRUSTERS("THRUSTERS", "15% MORE SPEED", 3),
    REPAIR("REPAIR CORE", "+1 HP", 3),
    SCORE_CORE("SCORE CORE", "+1 SCORE MULTIPLIER", 2),
    ARMOR("ARMOR PLATING", "+1 MAX HP", 2),
    COOLING("COOLING ARRAY", "10% FASTER FIRE", 3),
    ENERGY_CELL("ENERGY CELL", "START WITH SHIELD", 2),
    OVERDRIVE_CORE("OVERDRIVE CORE", "FASTER MOVE + FIRE", 2)
}

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

    private val router = GameStateRouter()
    private val state get() = router.state
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
    private val upgradeLevels = mutableMapOf<UpgradeType, Int>()
    private val persistence = GamePersistence()
    private var screenShake = 0f
    private var hitFlash = 0f

    private val input = InputController()
    private val enemySpawner = EnemySpawner()
    private val combat = CombatSystem()
    private val powerUpManager = PowerUpManager()
    private val waveManager = WaveManager()
    private val pools = EntityPools()
    private lateinit var particles: ParticleDebris
    private lateinit var uiText: UiText
    private lateinit var bloom: BloomRenderer
    private lateinit var worldRenderer: WorldRenderer
    private lateinit var menuRenderer: MenuRenderer
    private lateinit var settingsRenderer: SettingsRenderer
    private lateinit var gameOverRenderer: GameOverRenderer
    private lateinit var upgradeRenderer: UpgradeRenderer
    private lateinit var pauseRenderer: PauseRenderer
    private val ui = UiLayout()
    private lateinit var bodyFont: BitmapFont
    private lateinit var captionFont: BitmapFont
    private val safeArea get() = ui.safeArea
    private val singleButton get() = ui.singleButton
    private val multiButton get() = ui.multiButton
    private val settingsButton get() = ui.settingsButton
    private val pauseButton get() = ui.pauseButton
    private val resumeButton get() = ui.resumeButton
    private val menuButton get() = ui.menuButton
    private val toggleSfxButton get() = ui.toggleSfxButton
    private val toggleHapticsButton get() = ui.toggleHapticsButton
    private val volumeSlider get() = ui.volumeSlider
    private val swapControlsButton get() = ui.swapControlsButton
    private val tutorialButton get() = ui.tutorialButton
    private var controlsSwapped = false
    private var tutorialVisible = false
    private var waveBannerTimer = 0f
    private var waveBannerElite = false
    private var hapticsMuted = false
    private val transition get() = router.transition
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
        particles = ParticleDebris(batch)
        bloom = BloomRenderer()
        val titleGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Medium.ttf"))
        val titleParameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 64
            color = Color.WHITE
            borderWidth = 1.2f
            borderColor = Color(0f, 0f, 0f, 0.85f)
            shadowOffsetX = 1
            shadowOffsetY = 1
            shadowColor = Color(0f, 0f, 0f, 0.75f)
            characters = FreeTypeFontGenerator.DEFAULT_CHARS + "0123456789:-/+%"
            kerning = true
            genMipMaps = false
            minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        }
        font = titleGenerator.generateFont(titleParameter)
        titleGenerator.dispose()

        val bodyGenerator = FreeTypeFontGenerator(Gdx.files.internal("fonts/Kenney-Future.ttf"))
        val bodyParameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 38
            color = Color.WHITE
            borderWidth = 0.8f
            borderColor = Color(0f, 0f, 0f, 0.8f)
            shadowOffsetX = 1
            shadowOffsetY = 1
            shadowColor = Color(0f, 0f, 0f, 0.72f)
            characters = FreeTypeFontGenerator.DEFAULT_CHARS + "0123456789:-/+%"
            kerning = true
            genMipMaps = false
            minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        }
        bodyFont = bodyGenerator.generateFont(bodyParameter)

        val captionParameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = 26
            color = Color.WHITE
            borderWidth = 0.6f
            borderColor = Color(0f, 0f, 0f, 0.75f)
            shadowOffsetX = 1
            shadowOffsetY = 1
            shadowColor = Color(0f, 0f, 0f, 0.65f)
            characters = FreeTypeFontGenerator.DEFAULT_CHARS + "0123456789:-/+%"
            kerning = true
            genMipMaps = false
            minFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
            magFilter = com.badlogic.gdx.graphics.Texture.TextureFilter.Linear
        }
        captionFont = bodyGenerator.generateFont(captionParameter)
        bodyGenerator.dispose()
        uiText = UiText(batch)
        hudCamera = OrthographicCamera()
        hudCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        worldRenderer = WorldRenderer(camera, hudCamera, shapeRenderer, batch, glow, bloom, particles, font, bodyFont, input, ui, uiText)
        menuRenderer = MenuRenderer(shapeRenderer, batch, font, bodyFont, uiText)
        settingsRenderer = SettingsRenderer(shapeRenderer, batch, font, bodyFont, uiText)
        gameOverRenderer = GameOverRenderer(shapeRenderer, batch, font, bodyFont, uiText)
        upgradeRenderer = UpgradeRenderer(shapeRenderer, batch, font, bodyFont, uiText)
        pauseRenderer = PauseRenderer(shapeRenderer, batch, font, bodyFont, uiText)
        val savedStats = persistence.stats()
        val savedSettings = persistence.settings()
        highScore = savedStats.highScore
        bestWave = savedStats.bestWave
        totalKills = savedStats.totalKills
        FeedbackAudio.setMuted(savedSettings.muteSfx)
        hapticsMuted = savedSettings.muteHaptics
        controlsSwapped = savedSettings.controlsSwapped
        FeedbackAudio.setMasterVolume(savedSettings.sfxVolume)
        tutorialVisible = !savedSettings.tutorialSeen
        FeedbackAudio.init()
        ui.update(Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        bloom.resize(Gdx.graphics.width, Gdx.graphics.height)
        // Start with the fade overlay opaque, then fade into the menu instead of staying black.
        router.beginFadeIn()
        Gdx.input.inputProcessor = this
        buildArena()
        player = Tank(
            position = Vector2(WORLD_WIDTH / 2f, 120f),
            isPlayer = true,
            color = Color(0.2f, 0.9f, 1f, 1f),
            speed = GameConfig.Player.SPEED,
            health = GameConfig.Player.START_HP,
            fireRate = GameConfig.Player.FIRE_RATE,
            radius = GameConfig.Player.RADIUS
        )
        tutorialVisible = false
        centerCameraForIdle()
        router.goTo(GameState.MENU)
    }

    private fun resetGame() {
        enemies.clear()
        walls.clear()
        powerUps.clear()
        lasers.forEach { pools.freeLaser(it) }
        lasers.clear()
        bursts.forEach { pools.freeBurst(it) }
        bursts.clear()
        particles.clear()
        domainBursts.forEach { pools.freeBurst(it) }
        domainBursts.clear()
        particles.clear()
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
        upgradeLevels.clear()
        input.clearTransientInput()
        waveBannerTimer = 0f

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
        waveBannerTimer = 2.2f
        waveBannerElite = waveManager.isEliteWave(wave)
        score += waveManager.eliteBonus(wave)
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
        router.update(delta)
        when (state) {
            GameState.MENU -> {
                drawWorldIdle()
                drawMenuOverlay()
            }
            GameState.PLAYING -> {
                if (!input.paused && !tutorialVisible) update(delta.coerceIn(0f, 0.05f))
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
        drawTransition()
    }

    private fun drawTransition() {
        if (transition <= 0.01f) return
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, transition.coerceIn(0f, 1f))
        shapeRenderer.rect(0f, 0f, w, h)
        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)
    }

    private fun startSinglePlayer() {
        // A run is created only after the player explicitly presses Single Player.
        resetGame()
        input.setPaused(false)
        tutorialVisible = !persistence.settings().tutorialSeen
        router.goTo(GameState.PLAYING, fadeIn = true)
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

        particles.update(delta)
        for (burst in bursts) burst.t += delta * 1.6f
        bursts.filter { it.t >= 1f }.forEach { pools.freeBurst(it) }
        bursts.removeAll { it.t >= 1f }
        screenShake = (screenShake - delta * 2.8f).coerceAtLeast(0f)
        hitFlash = (hitFlash - delta * 2.5f).coerceAtLeast(0f)
        waveBannerTimer = (waveBannerTimer - delta).coerceAtLeast(0f)
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
            router.goTo(GameState.GAME_OVER)
            FeedbackAudio.play(FeedbackAudio.Cue.EXPLOSION)
            haptic(Input.VibrationType.HEAVY)
        }
        else if (enemies.isEmpty()) {
            spawnPowerUp()
            if (waveManager.isUpgradeWave(wave)) {
                prepareUpgradeChoices()
                router.goTo(GameState.UPGRADE)
                input.clearTransientInput()
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

    private fun outOfBounds(position: Vector2): Boolean =
        position.x < TILE || position.y < TILE ||
            position.x > WORLD_WIDTH - TILE || position.y > WORLD_HEIGHT - TILE

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
                            combo = (combo + 1).coerceAtMost(GameConfig.Combat.MAX_COMBO)
                            comboBest = maxOf(comboBest, combo)
                            comboTimer = GameConfig.Combat.COMBO_TIMEOUT
                            score += combat.scoreForKill(enemy, combo, scoreMultiplier)
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
        val type = powerUpManager.chooseType(wave)
        repeat(12) {
            scratchSpawn.set(
                MathUtils.random(TILE * 2f, WORLD_WIDTH - TILE * 2f),
                MathUtils.random(TILE * 2f, WORLD_HEIGHT - TILE * 2f)
            )
            if (powerUpManager.isSafe(scratchSpawn, player, walls, powerUps)) {
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
        particles.spawn(position)
    }

    private fun centerCamera(instant: Boolean) {
        val halfW = camera.viewportWidth / 2f
        val halfH = camera.viewportHeight / 2f
        val targetX = player.position.x + if (input.joystick.active) input.joystick.direction.x * 24f else 0f
        val targetY = player.position.y + 35f + if (input.joystick.active) input.joystick.direction.y * 18f else 0f
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
        worldRenderer.renderCombat(player, enemies, lasers, walls, bursts, powerUps, domainBursts)
        worldRenderer.drawTouchControls(controlsSwapped)
    }

    private fun drawWorldIdle() {
        centerCameraForIdle()
        worldRenderer.renderIdle(walls)
    }

    private fun centerCameraForIdle() {
        camera.position.set(WORLD_WIDTH / 2f, WORLD_HEIGHT / 2f, 0f)
        camera.update()
    }

    private fun persistProgress() {
        if (score > highScore) highScore = score
        if (wave > bestWave) bestWave = wave
        persistence.saveProgress(highScore, bestWave, totalKills)
    }

    private fun drawHud() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        ui.update(w, h)
        ui.hud()

        val left = safeArea.left
        val top = safeArea.top
        val panelW = (w * 0.44f).coerceIn(300f, 470f)
        val panelH = 88f

        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        shapeRenderer.color = Color(0.004f, 0.014f, 0.026f, 0.94f)
        shapeRenderer.rect(left, top - panelH, panelW, panelH)
        shapeRenderer.color = Color(0.18f, 0.9f, 1f, 0.9f)
        shapeRenderer.rect(left, top - 3f, panelW, 3f)
        shapeRenderer.color = Color(0.78f, 0.24f, 1f, 0.55f)
        shapeRenderer.rect(left + panelW - 4f, top - 33f, 4f, 30f)

        // Compact stat cells.
        val statY = top - 74f
        shapeRenderer.color = Color(0.18f, 0.9f, 1f, 0.12f)
        shapeRenderer.rect(left + 10f, statY - 4f, panelW - 20f, 2f)

        shapeRenderer.color = Color(0.015f, 0.035f, 0.055f, 0.96f)
        shapeRenderer.rect(pauseButton.x, pauseButton.y, pauseButton.width, pauseButton.height)
        shapeRenderer.color = Color(0.18f, 0.9f, 1f, 0.22f)
        shapeRenderer.rect(pauseButton.x, pauseButton.y + pauseButton.height - 3f, pauseButton.width, 3f)

        shapeRenderer.end()
        Gdx.gl.glDisable(GL20.GL_BLEND)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()

        fitFont("SPARKY // COMBAT LINK", panelW - 90f, 0.82f, 0.52f)
        drawShadowed("SPARKY // COMBAT LINK", left + 14f, top - 24f, Color(0.55f, 0.94f, 1f, 1f))

        fitBodyFont("SCORE " + score, panelW * 0.48f, 0.95f, 0.62f)
        drawBodyShadowed("SCORE " + score, left + 14f, top - 55f, Color.WHITE)

        fitBodyFont("WAVE " + wave, panelW * 0.24f, 0.82f, 0.56f)
        drawBodyRight("WAVE " + wave, left + panelW - 14f, top - 55f, Color(0.72f, 0.86f, 0.93f, 1f))

        fitBodyFont("BEST " + highScore, panelW * 0.43f, 0.72f, 0.5f)
        drawBodyShadowed("BEST " + highScore, left + 14f, top - 77f, Color(0.42f, 0.72f, 0.82f, 1f))

        fitBodyFont("COMBO x" + combo, panelW * 0.31f, 0.72f, 0.5f)
        drawBodyRight("COMBO x" + combo, left + panelW - 14f, top - 77f,
            if (combo >= 3) Color(1f, 0.72f, 0.2f, 1f) else Color(0.46f, 0.6f, 0.68f, 1f))

        // Player integrity readout. Text stays in the sprite batch; geometry is drawn
        // only after the batch closes to keep GL state transitions deterministic.
        fitBodyFont("CORE " + player.health + "/" + player.maxHealth, 150f, 0.78f, 0.54f)
        drawBodyRight("CORE " + player.health + "/" + player.maxHealth, safeArea.right - 12f, safeArea.top - 22f,
            Color(1f, 0.42f, 0.52f, 1f))
        batch.end()

        val coreW = 104f
        val coreX = safeArea.right - coreW - 12f
        val coreY = safeArea.top - 46f
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.68f)
        shapeRenderer.rect(coreX, coreY, coreW, 7f)
        shapeRenderer.color = if (player.health <= 1) Color(1f, 0.18f, 0.3f, 0.95f) else Color(0.18f, 0.9f, 1f, 0.9f)
        shapeRenderer.rect(coreX, coreY,
            coreW * (player.health.toFloat() / player.maxHealth.coerceAtLeast(1)).coerceIn(0f, 1f), 7f)
        shapeRenderer.end()
        drawPauseIcon(pauseButton)

        if (waveBannerTimer > 0f) drawWaveBanner()
        if (tutorialVisible) drawTutorialOverlay()
        if (hitFlash > 0f) {
            Gdx.gl.glEnable(GL20.GL_BLEND)
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
            shapeRenderer.projectionMatrix = hudCamera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color = Color(1f, 0.04f, 0.08f, hitFlash * 0.20f)
            shapeRenderer.rect(0f, 0f, w, h)
            shapeRenderer.end()
            Gdx.gl.glDisable(GL20.GL_BLEND)
        }
    }

    private fun drawPauseIcon(rect: Rectangle) {
        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.7f,0.88f,0.95f,1f)
        shapeRenderer.rect(rect.x+16f,rect.y+10f,4f,18f)
        shapeRenderer.rect(rect.x+28f,rect.y+10f,4f,18f)
        shapeRenderer.end()
    }

    private fun drawWaveBanner() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val cx = (safeArea.left + safeArea.right) / 2f
        val alpha = ((waveBannerTimer / 0.45f).coerceAtMost(1f) *
            ((2.2f - waveBannerTimer) / 0.7f).coerceIn(0f, 1f)).coerceIn(0f, 1f)
        val bannerW = (w * 0.46f).coerceIn(320f, 560f)
        val bannerH = 62f
        val x = cx - bannerW / 2f
        val y = h * 0.68f

        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0.004f, 0.016f, 0.028f, 0.86f * alpha)
        shapeRenderer.rect(x, y, bannerW, bannerH)
        shapeRenderer.color = if (waveBannerElite) Color(1f, 0.65f, 0.15f, 0.95f * alpha) else Color(0.18f, 0.9f, 1f, 0.92f * alpha)
        shapeRenderer.rect(x, y + bannerH - 3f, bannerW, 3f)
        shapeRenderer.rect(x, y, 4f, bannerH)
        shapeRenderer.color = Color(0.78f, 0.24f, 1f, 0.5f * alpha)
        shapeRenderer.rect(x + bannerW - 4f, y, 4f, bannerH)
        shapeRenderer.end()

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        val title = if (waveBannerElite) "ELITE WAVE" else "WAVE " + wave
        fitFont(title, bannerW - 38f, 1.0f, 0.62f)
        drawCentered(title, cx, y + 36f, Color(0.74f, 0.97f, 1f, alpha))
        fitFont(
            if (waveBannerElite) "HEAVY CONTACT DETECTED" else "HOSTILES INBOUND",
            bannerW - 42f, 0.42f, 0.28f
        )
        drawCentered(
            if (waveBannerElite) "HEAVY CONTACT DETECTED" else "HOSTILES INBOUND",
            cx, y + 15f, Color(0.55f, 0.7f, 0.78f, alpha)
        )
        batch.end()
    }

    private fun drawTutorialOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val cx = (safeArea.left + safeArea.right) / 2f
        val safeH = (safeArea.top - safeArea.bottom).coerceAtLeast(300f)
        val panelW = (safeArea.right - safeArea.left).coerceIn(400f, 760f) * 0.86f
        val panelH = safeH.coerceIn(340f, 610f) * 0.74f
        val panel = Rectangle(cx - panelW / 2f, (safeArea.bottom + safeArea.top) / 2f - panelH / 2f, panelW, panelH)
        tutorialButton.set(
            panel.x + 26f,
            panel.y + 22f,
            panel.width - 52f,
            (panel.height * 0.17f).coerceIn(58f, 76f)
        )

        shapeRenderer.projectionMatrix = hudCamera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.86f)
        shapeRenderer.rect(0f, 0f, w, h)
        shapeRenderer.color = Color(0.006f, 0.018f, 0.032f, 0.98f)
        shapeRenderer.rect(panel.x, panel.y, panel.width, panel.height)
        shapeRenderer.color = Color(0.18f, 0.9f, 1f, 0.92f)
        shapeRenderer.rect(panel.x, panel.y + panel.height - 3f, panel.width, 3f)
        shapeRenderer.color = Color(0.78f, 0.24f, 1f, 0.7f)
        shapeRenderer.rect(panel.x + panel.width - 4f, panel.y + panel.height - 34f, 4f, 31f)
        shapeRenderer.color = Color(0.18f, 0.9f, 1f, 0.15f)
        shapeRenderer.rect(panel.x + 26f, panel.y + panel.height - 112f, panel.width - 52f, 2f)
        drawButton(tutorialButton, Color(0.02f, 0.22f, 0.34f, 0.98f))
        shapeRenderer.end()

        batch.projectionMatrix = hudCamera.combined
        batch.begin()
        fitFont("FIELD BRIEFING // COMBAT BASICS", panel.width - 50f, 0.92f, 0.56f)
        drawCentered("FIELD BRIEFING // COMBAT BASICS", cx, panel.y + panel.height - 50f, Color(0.64f, 0.95f, 1f, 1f))

        val textX = panel.x + 36f
        val textW = panel.width - 72f
        val lines = arrayOf(
            "MOVE   //   DRAG THE JOYSTICK",
            "FIRE   //   HOLD THE FIRE CONTROL",
            "CLEAR  //   DESTROY EVERY HOSTILE",
            "UPGRADE // EVERY THIRD WAVE",
            "COMBO  //   CHAIN KILLS BEFORE TIMER EXPIRES"
        )
        val top = panel.y + panel.height - 116f
        for (i in lines.indices) {
            uiText.fit(captionFont, lines[i], textW, 0.92f, 0.48f)
            captionFont.color = if (i % 2 == 0) Color(0.78f, 0.9f, 0.94f, 1f) else Color(0.5f, 0.72f, 0.8f, 1f)
            captionFont.draw(batch, lines[i], textX, top - i * 34f)
        }

        fitFont("ACKNOWLEDGE", tutorialButton.width - 30f, 0.66f, 0.46f)
        drawCentered("ACKNOWLEDGE", cx, tutorialButton.y + tutorialButton.height * 0.62f, Color.WHITE)
        batch.end()
    }

    private fun fitBodyFont(text: String, maxWidth: Float, preferred: Float, minimum: Float): Float {
        bodyFont.data.setScale(1f)
        layout.setText(bodyFont, text)
        if (layout.width <= 0f) {
            bodyFont.data.setScale(preferred)
            return preferred
        }
        val widthScale = maxWidth / layout.width
        bodyFont.data.setScale(minOf(preferred, widthScale.coerceAtLeast(minimum), widthScale))
        layout.setText(bodyFont, text)
        return bodyFont.data.scaleX
    }

    private fun drawBodyShadowed(text: String, x: Float, y: Float, color: Color) {
        bodyFont.color = Color(0f, 0f, 0f, 0.8f)
        bodyFont.draw(batch, text, x + 2f, y - 2f)
        bodyFont.color = color
        bodyFont.draw(batch, text, x, y)
    }

    private fun drawBodyRight(text: String, rightX: Float, y: Float, color: Color) {
        layout.setText(bodyFont, text)
        bodyFont.color = color
        bodyFont.draw(batch, text, rightX - layout.width, y)
    }

    private fun fitFont(text: String, maxWidth: Float, preferred: Float, minimum: Float): Float {
        font.data.setScale(1f)
        layout.setText(font, text)
        if (layout.width <= 0f) {
            font.data.setScale(preferred)
            return preferred
        }
        // Never let the readability floor override the available width.
        // This prevents the Orbitron glyphs from escaping panels on short screens.
        val widthScale = (maxWidth / layout.width).coerceAtLeast(0.05f)
        val scale = minOf(preferred, maxOf(minimum, widthScale))
        font.data.setScale(minOf(scale, widthScale))
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
        ui.update(w, h)
        ui.menu(w, h)

        // Menu/settings overlays live in HUD screen coordinates. Always restore
        // the HUD projection after the world/idle renderer has drawn.
        shapeRenderer.projectionMatrix = hudCamera.combined
        batch.projectionMatrix = hudCamera.combined

        menuRenderer.draw(
            w, h, safeArea, singleButton, multiButton, settingsButton,
            highScore, bestWave, totalKills, ::drawButton
        )
    }

    private fun drawButton(rect: Rectangle, color: Color) {
        val x = rect.x
        val y = rect.y
        val w = rect.width
        val h = rect.height
        shapeRenderer.color = color
        shapeRenderer.rect(x, y, w, h)
        // Angular corner cuts make every command button read as one cyber UI family.
        shapeRenderer.color = Color(0.006f, 0.014f, 0.024f, 0.9f)
        shapeRenderer.triangle(x, y + h, x + 16f, y + h, x, y + h - 16f)
        shapeRenderer.triangle(x + w, y, x + w - 16f, y, x + w, y + 16f)
        shapeRenderer.color = Color(0.18f, 0.9f, 1f, 0.68f)
        shapeRenderer.rect(x + 3f, y + h - 4f, w - 6f, 4f)
        shapeRenderer.rect(x + 3f, y + 3f, 42f, 2f)
        shapeRenderer.color = Color(0.78f, 0.24f, 1f, 0.42f)
        shapeRenderer.rect(x + w - 44f, y + 3f, 41f, 2f)
    }

    private fun drawGameOverOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        ui.update(w, h)
        ui.gameOver(w, h)
        shapeRenderer.projectionMatrix = hudCamera.combined
        batch.projectionMatrix = hudCamera.combined
        gameOverRenderer.draw(
            w, h, safeArea, singleButton, menuButton, score, highScore, wave,
            totalKills, comboBest, ::drawButton
        )
    }

    private fun prepareUpgradeChoices() {
        upgradeChoices.clear()
        val pool = UpgradeType.values().filter {
            (upgradeLevels[it] ?: 0) < it.maxStacks
        }.toMutableList()
        while (upgradeChoices.size < 3 && pool.isNotEmpty()) {
            val index = MathUtils.random(pool.size - 1)
            upgradeChoices.add(pool.removeAt(index))
        }
    }

    private fun applyUpgrade(type: UpgradeType) {
        upgradeLevels[type] = (upgradeLevels[type] ?: 0) + 1
        when (type) {
            UpgradeType.OVERCLOCK -> player.fireRate = (player.fireRate * 0.75f).coerceAtLeast(0.11f)
            UpgradeType.THRUSTERS -> player.speed *= 1.15f
            UpgradeType.REPAIR -> player.health = (player.health + 1).coerceAtMost(player.maxHealth)
            UpgradeType.SCORE_CORE -> scoreMultiplier = (scoreMultiplier + 1).coerceAtMost(3)
            UpgradeType.ARMOR -> {
                player.maxHealth = (player.maxHealth + 1).coerceAtMost(GameConfig.Player.MAX_HP + 2)
                player.health = (player.health + 1).coerceAtMost(player.maxHealth)
            }
            UpgradeType.COOLING -> player.fireRate = (player.fireRate * 0.9f).coerceAtLeast(0.09f)
            UpgradeType.ENERGY_CELL -> player.grantShield(4.5f)
            UpgradeType.OVERDRIVE_CORE -> {
                player.grantOverdrive(5.5f)
                player.grantRapidFire(5.5f)
            }
        }
    }

    private fun drawUpgradeOverlay() {
        shapeRenderer.projectionMatrix = hudCamera.combined
        batch.projectionMatrix = hudCamera.combined
        upgradeRenderer.draw(
            Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(),
            wave, upgradeChoices, ui.upgradeButtons
        )
    }

    private fun drawPauseOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        ui.pause(w, h)
        shapeRenderer.projectionMatrix = hudCamera.combined
        batch.projectionMatrix = hudCamera.combined
        pauseRenderer.draw(w, h, safeArea, resumeButton, menuButton, ::drawButton)
    }

    private fun drawSettingsOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        ui.update(w, h)
        ui.settings(w, h)
        shapeRenderer.projectionMatrix = hudCamera.combined
        batch.projectionMatrix = hudCamera.combined
        settingsRenderer.draw(
            w, h, safeArea, toggleSfxButton, toggleHapticsButton, volumeSlider,
            swapControlsButton, menuButton, FeedbackAudio.isMuted(), hapticsMuted,
            FeedbackAudio.masterVolume(), ::drawButton
        )
    }

    private fun toUiRect(screenX: Int, screenY: Int, rect: Rectangle): Boolean {
        val uiY = (Gdx.graphics.height - screenY).toFloat()
        return rect.contains(screenX.toFloat(), uiY)
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT) return true

        when (state) {
            GameState.MENU -> {
                if (toUiRect(screenX, screenY, singleButton)) {
                    startSinglePlayer()
                } else if (toUiRect(screenX, screenY, multiButton)) {
                    // Multiplayer is intentionally unavailable; keep the button non-destructive.
                    FeedbackAudio.play(FeedbackAudio.Cue.UI)
                } else if (toUiRect(screenX, screenY, settingsButton)) {
                    router.goTo(GameState.SETTINGS)
                    FeedbackAudio.play(FeedbackAudio.Cue.UI)
                }
                return true
            }

            GameState.SETTINGS -> {
                when {
                    toUiRect(screenX, screenY, toggleSfxButton) -> {
                        val mutedNow = !FeedbackAudio.isMuted()
                        FeedbackAudio.setMuted(mutedNow)
                        persistence.setMuteSfx(mutedNow)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, toggleHapticsButton) -> {
                        hapticsMuted = !hapticsMuted
                        persistence.setMuteHaptics(hapticsMuted)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, volumeSlider) -> {
                        val min = volumeSlider.x
                        val max = volumeSlider.x + volumeSlider.width
                        val value = ((screenX.toFloat() - min) / (max - min)).coerceIn(0f, 1f)
                        FeedbackAudio.setMasterVolume(value)
                        persistence.setSfxVolume(value)
                    }
                    toUiRect(screenX, screenY, swapControlsButton) -> {
                        controlsSwapped = !controlsSwapped
                        persistence.setControlsSwapped(controlsSwapped)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, menuButton) -> {
                        router.goTo(GameState.MENU)
                                                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                }
                return true
            }

            GameState.PAUSED -> {
                when {
                    toUiRect(screenX, screenY, resumeButton) -> {
                        input.setPaused(false)
                        router.goTo(GameState.PLAYING)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, menuButton) -> {
                        input.setPaused(false)
                        router.goTo(GameState.MENU)
                                                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                }
                return true
            }

            GameState.GAME_OVER -> {
                when {
                    toUiRect(screenX, screenY, singleButton) -> startOrRestart()
                    toUiRect(screenX, screenY, menuButton) -> {
                        input.clearTransientInput()
                        router.goTo(GameState.MENU)
                                                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                }
                return true
            }

            GameState.UPGRADE -> {
                for (index in ui.upgradeButtons.indices) {
                    if (toUiRect(screenX, screenY, ui.upgradeButtons[index]) && index < upgradeChoices.size) {
                        applyUpgrade(upgradeChoices[index])
                        router.goTo(GameState.PLAYING)
                        input.setPaused(false)
                                                        FeedbackAudio.play(FeedbackAudio.Cue.POWER_UP)
                        haptic(Input.VibrationType.MEDIUM)
                        break
                    }
                }
                return true
            }

            GameState.PLAYING -> {
                if (tutorialVisible) {
                    if (toUiRect(screenX, screenY, tutorialButton)) {
                        tutorialVisible = false
                        persistence.setTutorialSeen(true)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    // Tutorial is a hard input gate: no movement, firing, pause, or gameplay
                    // actions are accepted until the player explicitly taps GOT IT.
                    return true
                }

                if (toUiRect(screenX, screenY, pauseButton)) {
                    input.clearTransientInput()
                    router.goTo(GameState.PAUSED)
                    input.setPaused(true)
                    FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    return true
                }

                val w = Gdx.graphics.width.toFloat()
                val h = Gdx.graphics.height.toFloat()
                ui.touchControls(w, h, controlsSwapped)
                input.setTouchRadius(ui.touchControls.radius)
                val touch = ui.touchControls
                if (touch.moveHit.contains(screenX.toFloat(), screenY.toFloat())) {
                    input.joystick.tryActivate(
                        touch.move.x,
                        touch.move.y,
                        screenX.toFloat(),
                        screenY.toFloat(),
                        pointer
                    )
                    return true
                }
                if (touch.fireHit.contains(screenX.toFloat(), screenY.toFloat())) {
                    input.pressFire(pointer)
                    return true
                }
                return true
            }
        }
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
        bloom.resize(width, height)
        ui.update(width.toFloat(), height.toFloat())
        ui.touchControls(width.toFloat(), height.toFloat(), controlsSwapped)
        input.setTouchRadius(ui.touchControls.radius)
        if (state == GameState.MENU || state == GameState.SETTINGS) {
            centerCameraForIdle()
        } else {
            centerCamera(true)
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode != Input.Keys.BACK) return false
        val next = router.goBack() ?: return false
        when (next) {
            GameState.PAUSED -> input.clearTransientInput()
            GameState.PLAYING -> input.setPaused(false)
            GameState.MENU -> input.clearTransientInput()
            else -> Unit
        }
        FeedbackAudio.play(FeedbackAudio.Cue.UI)
        return true
    }

    override fun pause() {
        input.clearTransientInput()
        if (state == GameState.PLAYING) {
            input.setPaused(true)
            router.goTo(GameState.PAUSED)
        }
    }

    override fun resume() {
        if (state == GameState.PAUSED) {
            input.setPaused(false)
            router.goTo(GameState.PLAYING)
        }
    }

    override fun hide() {}

    override fun dispose() {
        Gdx.input.inputProcessor = null
        shapeRenderer.dispose()
        batch.dispose()
        font.dispose()
        bodyFont.dispose()
        captionFont.dispose()
        pools.clear()
        particles.dispose()
        bloom.dispose()
        FeedbackAudio.dispose()
    }
}