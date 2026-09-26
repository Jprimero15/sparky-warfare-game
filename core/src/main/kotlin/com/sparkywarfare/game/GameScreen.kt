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

const val WORLD_WIDTH = GameConfig.WORLD_WIDTH
const val WORLD_HEIGHT = GameConfig.WORLD_HEIGHT
const val TILE = GameConfig.TILE
const val VIEW_WIDTH = GameConfig.VIEW_WIDTH
const val VIEW_HEIGHT = GameConfig.VIEW_HEIGHT

enum class GameState { MENU, PLAYING, GAME_OVER, UPGRADE, PAUSED, SETTINGS }

enum class UpgradeType(val title: String, val description: String, val maxStacks: Int) {
    OVERCLOCK("OVERCLOCK", "18% FASTER FIRE", 3),
    THRUSTERS("THRUSTERS", "15% MORE SPEED", 3),
    REPAIR("REPAIR CORE", "+1 HP", 3),
    SCORE_CORE("SCORE CORE", "+1 SCORE MULTIPLIER", 2),
    ARMOR("ARMOR PLATING", "+1 MAX HP", 2),
    COOLING("COOLING ARRAY", "6% FASTER FIRE", 3),
    ENERGY_CELL("ENERGY CELL", "START WITH SHIELD", 2),
    OVERDRIVE_CORE("OVERDRIVE CORE", "FASTER MOVE + FIRE", 2)
}

class GameScreen : Screen, InputAdapter() {
    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var glow: GlowRenderer
    private lateinit var tankRenderer: TankRenderer
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont
    private lateinit var fonts: UiFontSet
    private lateinit var layout: GlyphLayout
    private lateinit var hudCamera: OrthographicCamera

    private lateinit var player: Tank
    /** Session boundary keeps game-mode ownership separate from rendering and UI. */
    private val session = GameSession()
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
    private val arenaBuilder = ArenaBuilder()
    private val wallGrid = WallSpatialGrid()
    private val upgradeManager = UpgradeManager()
    private lateinit var bodyFont: BitmapFont
    private lateinit var captionFont: BitmapFont
    private var initialized = false
    private var sliderPointer = -1
    private var pressedUiButton: Rectangle? = null
    private var uiPulseTime = 0f
    private var arenaVariant = 0
    private val drawButtonColor = Color()
    private val safeArea get() = ui.safeArea
    private val singleButton get() = ui.singleButton
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
    private val transitionColor = Color()
    private val tutorialLines = arrayOf(
        "MOVE   //   DRAG THE JOYSTICK",
        "FIRE   //   HOLD THE FIRE CONTROL",
        "CLEAR  //   DESTROY EVERY HOSTILE",
        "UPGRADE // EVERY THIRD WAVE",
        "COMBO  //   CHAIN KILLS BEFORE TIMER EXPIRES"
    )
    private val transition get() = router.transition
    private val scratchDirection = Vector2()
    private val scratchMove = Vector2()
    private val scratchSpawn = Vector2()
    private val scratchUi = Vector2()
    private val laserRemove = mutableListOf<Laser>()

    override fun show() {
        camera = OrthographicCamera()
        viewport = ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, camera)
        shapeRenderer = ShapeRenderer()
        glow = GlowRenderer(shapeRenderer)
        tankRenderer = TankRenderer(shapeRenderer)
        batch = SpriteBatch()
        particles = ParticleDebris(batch)
        bloom = BloomRenderer()
        fonts = UiFontSet.load()
        font = fonts.title
        bodyFont = fonts.body
        captionFont = fonts.caption
        uiText = UiText(batch)
        layout = GlyphLayout()
        hudCamera = OrthographicCamera()
        hudCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        worldRenderer = WorldRenderer(camera, hudCamera, shapeRenderer, batch, glow, tankRenderer, bloom, particles, font, bodyFont, input, ui, uiText)
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
            color = Color(UiTheme.CYAN),
            speed = GameConfig.Player.SPEED,
            health = GameConfig.Player.START_HP,
            fireRate = GameConfig.Player.FIRE_RATE,
            radius = GameConfig.Player.RADIUS
        )
        session.beginSinglePlayer(player)
        centerCameraForIdle()
        router.goTo(GameState.MENU)
        initialized = true
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
        upgradeManager.clear()
        arenaVariant = MathUtils.random(0, 2)
        input.clearTransientInput()
        waveBannerTimer = 0f

        player = Tank(
            position = Vector2(WORLD_WIDTH / 2f, 120f),
            isPlayer = true,
            color = Color(UiTheme.CYAN),
            speed = GameConfig.Player.SPEED,
            health = GameConfig.Player.START_HP,
            fireRate = GameConfig.Player.FIRE_RATE,
            radius = GameConfig.Player.RADIUS
        )
        session.beginSinglePlayer(player)
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
        arenaBuilder.build(walls, arenaVariant)
        wallGrid.rebuild(walls)
    }

    override fun render(delta: Float) {
        if (!initialized) return
        uiPulseTime += delta
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
        shapeRenderer.projectionMatrix = hudCamera.combined
        UiShapes.begin(shapeRenderer)
        transitionColor.set(0f, 0f, 0f, transition.coerceIn(0f, 1f))
        shapeRenderer.color = transitionColor
        shapeRenderer.rect(0f, 0f, w, h)
        UiShapes.end(shapeRenderer)
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
        for (index in bursts.indices.reversed()) {
            val burst = bursts[index]
            burst.t += delta * 1.6f
            if (burst.t >= 1f) {
                bursts.removeAt(index)
                pools.freeBurst(burst)
            }
        }
        screenShake = (screenShake - delta * 2.8f).coerceAtLeast(0f)
        hitFlash = (hitFlash - delta * 2.5f).coerceAtLeast(0f)
        waveBannerTimer = (waveBannerTimer - delta).coerceAtLeast(0f)
        if (comboTimer > 0f) {
            comboTimer -= delta
            if (comboTimer <= 0f) combo = 0
        }
        centerCamera(false)
        for (index in domainBursts.indices.reversed()) {
            val burst = domainBursts[index]
            burst.t += delta * 0.9f
            if (burst.t >= 1f) {
                domainBursts.removeAt(index)
                pools.freeBurst(burst)
            }
        }

        checkLaserCollisions()
        checkPowerUpPickups()
        enemies.removeAll { !it.alive }
        val wallCountBeforeCleanup = walls.size
        walls.removeAll { !it.alive }
        if (walls.size != wallCountBeforeCleanup) wallGrid.rebuild(walls)

        if (!player.alive) {
            persistProgress()
            router.goTo(GameState.GAME_OVER)
            FeedbackAudio.play(FeedbackAudio.Cue.EXPLOSION)
            haptic(Input.VibrationType.HEAVY)
        }
        else if (enemies.isEmpty()) {
            spawnPowerUp()
            if (waveManager.isUpgradeWave(wave)) {
                upgradeManager.prepareChoices()
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
        if (!session.aiEnabled) return
        for ((index, enemy) in enemies.withIndex()) {
            if (!enemy.alive) continue
            scratchDirection.set(player.position).sub(enemy.position)
            if (scratchDirection.len2() > 1f) {
                val distance = scratchDirection.len()
                scratchDirection.nor()
                val targetAngle = scratchDirection.angleDeg()
                val turn = GameConfig.Enemy.AI_MAX_AIM_TURN_SPEED * delta
                enemy.aiAimAngle = MathUtils.lerpAngleDeg(
                    enemy.aiAimAngle,
                    targetAngle,
                    (turn / 180f).coerceIn(0f, 1f)
                )
                enemy.turretAngle = enemy.aiAimAngle
                enemy.angle = targetAngle

                scratchMove.set(scratchDirection)
                val strafeSign = if ((index and 1) == 0) 1f else -1f
                when (enemy.enemyTier) {
                    EnemyTier.SCOUT -> scratchMove.add(-scratchDirection.y * 0.50f * strafeSign, scratchDirection.x * 0.50f * strafeSign)
                    EnemyTier.ASSAULT -> scratchMove.add(-scratchDirection.y * 0.28f * strafeSign, scratchDirection.x * 0.28f * strafeSign)
                    EnemyTier.RANGED -> {
                        if (distance < 220f) scratchMove.scl(-0.70f)
                        scratchMove.add(-scratchDirection.y * 0.38f * strafeSign, scratchDirection.x * 0.38f * strafeSign)
                    }
                    EnemyTier.HEAVY -> scratchMove.add(-scratchDirection.y * 0.12f * strafeSign, scratchDirection.x * 0.12f * strafeSign)
                    EnemyTier.ELITE -> scratchMove.add(-scratchDirection.y * 0.18f * strafeSign, scratchDirection.x * 0.18f * strafeSign)
                    null -> Unit
                }
                scratchMove.nor()

                val factor = when (enemy.enemyTier) {
                    EnemyTier.RANGED -> if (distance > 280f) 0.58f else 0.22f
                    EnemyTier.HEAVY -> if (distance > 110f) 0.46f else 0.22f
                    else -> if (distance > 95f) 0.65f else 0.28f
                }
                CollisionSystem.tryMoveTank(
                    enemy, scratchMove, enemy.speed * factor * delta, player, enemies, walls, wallGrid
                )
            }
            enemy.aiReactionTimer = (enemy.aiReactionTimer - delta).coerceAtLeast(0f)
            enemy.aiFireTimer -= delta
            val angleDelta = ((enemy.aiAimAngle - scratchDirection.angleDeg() + 540f) % 360f) - 180f
            val aimError = Math.abs(angleDelta)
            if (enemy.aiFireTimer <= 0f && enemy.aiReactionTimer <= 0f &&
                enemy.canFire() && aimError <= GameConfig.Enemy.AI_FIRE_ANGLE_TOLERANCE &&
                CollisionSystem.hasLineOfSight(enemy.position, player.position, wallGrid)) {
                fireLaser(enemy)
                enemy.aiFireTimer = MathUtils.random(GameConfig.Enemy.AI_MIN_FIRE_DELAY, GameConfig.Enemy.AI_MAX_FIRE_DELAY)
                enemy.aiReactionTimer = MathUtils.random(GameConfig.Enemy.AI_MIN_REACTION_DELAY, GameConfig.Enemy.AI_MAX_REACTION_DELAY)
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
                // Player aiming is intentionally coupled to movement for fast, predictable touch control.
                player.turretAngle = player.angle
                CollisionSystem.tryMoveTank(player, scratchDirection, player.speed * delta, player, enemies, walls)
            }
        }
        if (input.firing && player.canFire()) fireLaser(player)
    }

    private fun outOfBounds(position: Vector2): Boolean =
        position.x < TILE || position.y < TILE ||
            position.x > WORLD_WIDTH - TILE || position.y > WORLD_HEIGHT - TILE

    private fun fireLaser(tank: Tank) {
        val spread = if (tank.isPlayer) 0f else when (tank.enemyTier) {
            EnemyTier.SCOUT -> GameConfig.Enemy.AI_SCOUT_SPREAD
            EnemyTier.ASSAULT -> GameConfig.Enemy.AI_ASSAULT_SPREAD
            EnemyTier.HEAVY -> GameConfig.Enemy.AI_HEAVY_SPREAD
            EnemyTier.RANGED -> GameConfig.Enemy.AI_RANGED_SPREAD
            EnemyTier.ELITE -> GameConfig.Enemy.AI_ELITE_SPREAD
            null -> GameConfig.Enemy.AI_ASSAULT_SPREAD
        }
        val shotAngle = tank.turretAngle + if (spread > 0f) MathUtils.random(-spread, spread) else 0f
        val rad = Math.toRadians(shotAngle.toDouble())
        scratchDirection.set(Math.cos(rad).toFloat(), Math.sin(rad).toFloat()).nor()
        scratchSpawn.set(tank.position).mulAdd(scratchDirection, tank.radius + 3f)
        lasers.add(pools.obtainLaser(scratchSpawn, scratchDirection, tank.color, tank.isPlayer))
        if (tank.hasSpreadShot()) {
            val base = tank.turretAngle
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
        val right = safeArea.right
        val top = safeArea.top
        val cardsGap = 10f
        val pauseGap = 12f
        val cardAreaRight = pauseButton.x - pauseGap
        val available = (cardAreaRight - left).coerceAtLeast(1f)
        val healthW = minOf(232f, (available - cardsGap).coerceAtLeast(1f) * 0.36f)
        val statsW = (available - healthW - cardsGap).coerceAtLeast(1f)
        val cardH = minOf(108f, (safeArea.top - safeArea.bottom) * 0.18f).coerceAtLeast(84f)
        val cardY = top - cardH
        val healthX = cardAreaRight - healthW
        val healthBarX = healthX + 14f
        val healthBarW = (healthW - 28f).coerceAtLeast(1f)
        val healthBarY = cardY + 22f
        val healthRatio = (player.health.toFloat() / player.maxHealth.coerceAtLeast(1)).coerceIn(0f, 1f)
        val healthColor = if (healthRatio <= 0.34f) UiTheme.DANGER else UiTheme.CYAN

        shapeRenderer.projectionMatrix = hudCamera.combined
        UiShapes.begin(shapeRenderer)

        UiShapes.glassPanel(shapeRenderer, left, cardY, statsW, cardH, 22f)
        UiShapes.glassPanel(shapeRenderer, healthX, cardY, healthW, cardH, 22f)
        UiShapes.glassPanel(
            shapeRenderer,
            pauseButton.x,
            pauseButton.y,
            pauseButton.width,
            pauseButton.height,
            UiTheme.Metrics.BUTTON_RADIUS
        )

        UiShapes.roundedRect(
            shapeRenderer,
            healthBarX,
            healthBarY,
            healthBarW,
            13f,
            6.5f,
            UiTheme.PANEL_DARK
        )
        if (healthBarW * healthRatio > 1f) {
            UiShapes.gradientRoundedRect(
                shapeRenderer,
                healthBarX,
                healthBarY,
                healthBarW * healthRatio,
                13f,
                6.5f,
                healthColor,
                UiTheme.MAGENTA,
                7
            )
        }
        UiShapes.end(shapeRenderer)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()

        fitCaptionFont("SCORE  " + score, statsW * 0.46f, 0.84f, 0.46f)
        drawCaptionShadowed("SCORE  " + score, left + 16f, cardY + cardH - 27f, UiTheme.TEXT_PRIMARY)
        fitCaptionFont("BEST  " + highScore, statsW * 0.42f, 0.76f, 0.44f)
        drawCaptionRight("BEST  " + highScore, left + statsW - 16f, cardY + cardH - 27f, UiTheme.MAGENTA)

        fitCaptionFont("WAVE  " + wave, statsW * 0.42f, 0.72f, 0.42f)
        drawCaptionShadowed("WAVE  " + wave, left + 16f, cardY + cardH - 55f, UiTheme.TEXT_SECONDARY)
        fitCaptionFont("COMBO  x" + combo, statsW * 0.45f, 0.72f, 0.42f)
        drawCaptionRight(
            "COMBO  x" + combo,
            left + statsW - 16f,
            cardY + cardH - 55f,
            if (combo >= 3) UiTheme.GOLD else UiTheme.TEXT_SECONDARY
        )

        fitCaptionFont("HEALTH", healthW * 0.42f, 0.62f, 0.40f)
        drawCaptionShadowed("HEALTH", healthBarX, cardY + cardH - 27f, UiTheme.TEXT_SECONDARY)
        val healthText = player.health.toString() + "/" + player.maxHealth
        fitCaptionFont(healthText, healthW * 0.34f, 0.62f, 0.40f)
        drawCaptionRight(healthText, healthX + healthW - 14f, cardY + cardH - 27f, healthColor)

        batch.end()
        drawPauseIcon(pauseButton)

        if (waveBannerTimer > 0f && !tutorialVisible) drawWaveBanner()
        if (tutorialVisible) drawTutorialOverlay()
        if (hitFlash > 0f) {
            shapeRenderer.projectionMatrix = hudCamera.combined
            UiShapes.begin(shapeRenderer)
            shapeRenderer.color = Color(UiTheme.DANGER.r, UiTheme.DANGER.g, UiTheme.DANGER.b, hitFlash * 0.16f)
            shapeRenderer.rect(0f, 0f, w, h)
            UiShapes.end(shapeRenderer)
        }
    }

    private fun drawPauseIcon(rect: Rectangle) {
        shapeRenderer.projectionMatrix = hudCamera.combined
        UiShapes.begin(shapeRenderer)
        shapeRenderer.color = UiTheme.TEXT_PRIMARY
        val barW = (rect.width * 0.11f).coerceAtLeast(6f)
        val barH = (rect.height * 0.44f).coerceAtLeast(25f)
        val gap = (rect.width * 0.13f).coerceAtLeast(8f)
        val startX = rect.x + (rect.width - barW * 2f - gap) / 2f
        val startY = rect.y + (rect.height - barH) / 2f
        shapeRenderer.rect(startX, startY, barW, barH)
        shapeRenderer.rect(startX + barW + gap, startY, barW, barH)
        UiShapes.end(shapeRenderer)
    }

    private fun drawWaveBanner() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val cx = (safeArea.left + safeArea.right) / 2f
        val alpha = ((waveBannerTimer / 0.45f).coerceAtMost(1f) *
            ((2.2f - waveBannerTimer) / 0.90f).coerceIn(0f, 1f)).coerceIn(0f, 1f)

        val bannerW = minOf(w * 0.56f, 620f).coerceAtLeast(320f)
        val bannerH = 104f
        val x = cx - bannerW / 2f
        val y = h * 0.60f
        val accent = if (waveBannerElite) UiTheme.MAGENTA else UiTheme.CYAN
        val accentSoft = Color(accent.r, accent.g, accent.b, alpha * 0.28f)

        shapeRenderer.projectionMatrix = hudCamera.combined
        UiShapes.begin(shapeRenderer)
        UiShapes.glassPanel(shapeRenderer, x, y, bannerW, bannerH, 26f)
        UiShapes.roundedRect(
            shapeRenderer,
            x + 18f,
            y + bannerH - 5f,
            bannerW - 36f,
            2f,
            1f,
            accentSoft
        )
        UiShapes.roundedRect(
            shapeRenderer,
            x + 18f,
            y + 18f,
            5f,
            bannerH - 36f,
            2.5f,
            Color(accent.r, accent.g, accent.b, alpha * 0.30f)
        )
        UiShapes.end(shapeRenderer)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()

        val title = if (waveBannerElite) "ELITE WAVE" else "WAVE  " + wave.toString().padStart(2, '0')
        fitFont(title, bannerW - 52f, 0.82f, 0.52f)
        uiText.centeredVertically(
            font,
            title,
            cx,
            y + 69f,
            Color(accent.r, accent.g, accent.b, alpha)
        )

        val sub = if (waveBannerElite) "HEIGHTENED THREAT" else "NEXT ASSAULT"
        fitBodyFont(sub, bannerW - 56f, 0.44f, 0.32f)
        uiText.centeredVertically(
            bodyFont,
            sub,
            cx,
            y + 38f,
            Color(UiTheme.TEXT_SECONDARY.r, UiTheme.TEXT_SECONDARY.g, UiTheme.TEXT_SECONDARY.b, alpha)
        )

        uiText.reset(font, bodyFont, captionFont)
        batch.end()
    }

    private fun drawTutorialOverlay() {
        val w = Gdx.graphics.width.toFloat()
        val h = Gdx.graphics.height.toFloat()
        val safeW = (safeArea.right - safeArea.left).coerceAtLeast(1f)
        val safeH = (safeArea.top - safeArea.bottom).coerceAtLeast(1f)
        val cx = (safeArea.left + safeArea.right) / 2f

        val panelW = minOf(safeW * 0.90f, 900f)
            .coerceAtMost((safeW - 24f).coerceAtLeast(280f))
        val panelH = minOf(safeH * 0.90f, 620f)
            .coerceAtMost((safeH - 24f).coerceAtLeast(300f))
        val panel = Rectangle(
            cx - panelW / 2f,
            (safeArea.bottom + safeArea.top) / 2f - panelH / 2f,
            panelW,
            panelH
        )

        val buttonH = (panelH * 0.14f).coerceIn(50f, 68f)
        tutorialButton.set(
            panel.x + 24f,
            panel.y + 16f,
            panel.width - 48f,
            buttonH
        )

        val rowGap = 6f
        val contentTop = panel.y + panel.height - 84f
        val contentBottom = tutorialButton.y + tutorialButton.height + 14f
        val rowH = ((contentTop - contentBottom - rowGap * 4f) / tutorialLines.size)
            .coerceAtLeast(20f)
        val rowX = panel.x + 24f
        val rowW = panel.width - 48f

        shapeRenderer.projectionMatrix = hudCamera.combined
        UiShapes.begin(shapeRenderer)
        UiShapes.overlay(shapeRenderer, w, h)
        UiShapes.glassPanel(shapeRenderer, panel.x, panel.y, panel.width, panel.height, 28f)

        for (i in tutorialLines.indices) {
            val rowY = contentTop - rowH - i * (rowH + rowGap)
            UiShapes.glassCard(
                shapeRenderer,
                rowX,
                rowY,
                rowW,
                rowH,
                15f,
                if (i % 2 == 0) UiTheme.CYAN else UiTheme.MAGENTA
            )
        }
        UiShapes.softButton(shapeRenderer, tutorialButton, UiTheme.CYAN)
        UiShapes.end(shapeRenderer)

        batch.projectionMatrix = hudCamera.combined
        batch.begin()

        fitFont("HOW TO PLAY", panel.width - 52f, 0.88f, 0.58f)
        uiText.centeredVertically(
            font,
            "HOW TO PLAY",
            cx,
            panel.y + panel.height - 32f,
            UiTheme.TEXT_PRIMARY
        )

        fitBodyFont("MOVE • FIRE • SURVIVE", panel.width - 64f, 0.50f, 0.36f)
        uiText.centeredVertically(
            bodyFont,
            "MOVE • FIRE • SURVIVE",
            cx,
            panel.y + panel.height - 61f,
            UiTheme.CYAN
        )

        for (i in tutorialLines.indices) {
            val line = tutorialLines[i]
            val rowY = contentTop - rowH - i * (rowH + rowGap)
            uiText.fitWithin(
                captionFont,
                line,
                rowW - 24f,
                (rowH - 8f).coerceAtLeast(12f),
                0.50f,
                0.22f
            )
            uiText.centeredVertically(
                captionFont,
                line,
                cx,
                rowY + rowH / 2f,
                if (i % 2 == 0) UiTheme.TEXT_PRIMARY else UiTheme.TEXT_SECONDARY
            )
        }

        fitFont("GOT IT", tutorialButton.width - 36f, 0.64f, 0.46f)
        uiText.centeredVertically(
            font,
            "GOT IT",
            cx,
            tutorialButton.y + tutorialButton.height / 2f,
            UiTheme.TEXT_PRIMARY
        )

        uiText.reset(font, bodyFont, captionFont)
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

    private fun fitCaptionFont(text: String, maxWidth: Float, preferred: Float, minimum: Float): Float {
        captionFont.data.setScale(1f)
        layout.setText(captionFont, text)
        if (layout.width <= 0f) {
            captionFont.data.setScale(preferred)
            return preferred
        }
        val widthScale = maxWidth / layout.width
        captionFont.data.setScale(minOf(preferred, widthScale.coerceAtLeast(minimum), widthScale))
        layout.setText(captionFont, text)
        return captionFont.data.scaleX
    }

    private fun drawCaptionShadowed(text: String, x: Float, y: Float, color: Color) {
        captionFont.color = Color(0f, 0f, 0f, 0.78f)
        captionFont.draw(batch, text, x + 2f, y - 2f)
        captionFont.color = color
        captionFont.draw(batch, text, x, y)
    }

    private fun drawCaptionRight(text: String, rightX: Float, y: Float, color: Color) {
        layout.setText(captionFont, text)
        captionFont.color = color
        captionFont.draw(batch, text, rightX - layout.width, y)
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
            w, h, safeArea, singleButton, settingsButton,
            highScore, bestWave, totalKills, ::drawButton
        )
    }

    private fun drawButton(rect: Rectangle, color: Color) {
        drawButtonColor.set(color)
        if (pressedUiButton === rect) {
            drawButtonColor.a = (drawButtonColor.a * 0.68f).coerceAtLeast(0.18f)
        } else {
            val breathe = 0.92f + 0.08f * (0.5f + 0.5f * MathUtils.sin(uiPulseTime * 2.2f))
            drawButtonColor.a = (drawButtonColor.a * breathe).coerceAtMost(1f)
        }
        UiShapes.softButton(shapeRenderer, rect, drawButtonColor, UiTheme.Metrics.BUTTON_RADIUS)
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

    private fun drawUpgradeOverlay() {
        shapeRenderer.projectionMatrix = hudCamera.combined
        batch.projectionMatrix = hudCamera.combined
        upgradeRenderer.draw(
            Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat(),
            wave, upgradeManager.choices, ui.upgradeButtons
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
                when {
                    toUiRect(screenX, screenY, singleButton) -> {
                        pressedUiButton = singleButton
                        startSinglePlayer()
                    }
                    toUiRect(screenX, screenY, settingsButton) -> {
                        pressedUiButton = settingsButton
                        router.goTo(GameState.SETTINGS)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                }
                return true
            }

            GameState.SETTINGS -> {
                when {
                    toUiRect(screenX, screenY, toggleSfxButton) -> {
                        pressedUiButton = toggleSfxButton
                        val mutedNow = !FeedbackAudio.isMuted()
                        FeedbackAudio.setMuted(mutedNow)
                        persistence.setMuteSfx(mutedNow)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, toggleHapticsButton) -> {
                        pressedUiButton = toggleHapticsButton
                        hapticsMuted = !hapticsMuted
                        persistence.setMuteHaptics(hapticsMuted)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, volumeSlider) -> {
                        sliderPointer = pointer
                        setVolumeFromScreenX(screenX)
                    }
                    toUiRect(screenX, screenY, swapControlsButton) -> {
                        pressedUiButton = swapControlsButton
                        controlsSwapped = !controlsSwapped
                        persistence.setControlsSwapped(controlsSwapped)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, menuButton) -> {
                        pressedUiButton = menuButton
                        router.goTo(GameState.MENU)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                }
                return true
            }

            GameState.PAUSED -> {
                when {
                    toUiRect(screenX, screenY, resumeButton) -> {
                        pressedUiButton = resumeButton
                        input.setPaused(false)
                        router.goTo(GameState.PLAYING)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    toUiRect(screenX, screenY, menuButton) -> {
                        pressedUiButton = menuButton
                        input.setPaused(false)
                        router.goTo(GameState.MENU)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                }
                return true
            }

            GameState.GAME_OVER -> {
                when {
                    toUiRect(screenX, screenY, singleButton) -> {
                        pressedUiButton = singleButton
                        startOrRestart()
                    }
                    toUiRect(screenX, screenY, menuButton) -> {
                        pressedUiButton = menuButton
                        input.clearTransientInput()
                        router.goTo(GameState.MENU)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                }
                return true
            }

            GameState.UPGRADE -> {
                for (index in ui.upgradeButtons.indices) {
                    if (toUiRect(screenX, screenY, ui.upgradeButtons[index]) && index < upgradeManager.choices.size) {
                        pressedUiButton = ui.upgradeButtons[index]
                        scoreMultiplier = upgradeManager.apply(
                            upgradeManager.choices[index],
                            player,
                            scoreMultiplier
                        )
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
                        pressedUiButton = tutorialButton
                        tutorialVisible = false
                        persistence.setTutorialSeen(true)
                        FeedbackAudio.play(FeedbackAudio.Cue.UI)
                    }
                    return true
                }

                if (toUiRect(screenX, screenY, pauseButton)) {
                    pressedUiButton = pauseButton
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
        if (pointer == sliderPointer) {
            setVolumeFromScreenX(screenX)
            return true
        }
        input.drag(screenX, screenY, pointer)
        return true
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        if (pointer == sliderPointer) {
            setVolumeFromScreenX(screenX)
            persistence.flush()
            sliderPointer = -1
        }
        input.release(pointer)
        pressedUiButton = null
        return true
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean =
        touchUp(screenX, screenY, pointer, button)

    private fun setVolumeFromScreenX(screenX: Int) {
        val min = volumeSlider.x
        val max = (volumeSlider.x + volumeSlider.width).coerceAtLeast(min + 1f)
        val value = ((screenX.toFloat() - min) / (max - min)).coerceIn(0f, 1f)
        FeedbackAudio.setMasterVolume(value)
        persistence.setSfxVolume(value, flush = false)
    }

    private fun haptic(type: Input.VibrationType) {
        if (!hapticsMuted && Gdx.input.isPeripheralAvailable(Input.Peripheral.Vibrator)) Gdx.input.vibrate(type)
    }

    override fun resize(width: Int, height: Int) {
        if (!initialized) return
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
        persistence.flush()
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

    override fun hide() {
        persistence.flush()
        input.clearTransientInput()
        if (Gdx.input.inputProcessor === this) Gdx.input.inputProcessor = null
    }

    override fun dispose() {
        if (!initialized) return
        persistence.flush()
        Gdx.input.inputProcessor = null
        input.clearTransientInput()
        enemies.clear()
        walls.clear()
        powerUps.clear()
        lasers.clear()
        bursts.clear()
        domainBursts.clear()
        session.clear()
        pools.clear()
        shapeRenderer.dispose()
        batch.dispose()
        fonts.dispose()
        particles.dispose()
        bloom.dispose()
        FeedbackAudio.dispose()
        initialized = false
    }
}