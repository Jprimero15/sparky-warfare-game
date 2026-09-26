# Sparky Warfare

## Changelog / this pass

This pass keeps the Android-only LibGDX architecture and Soft Neon Arcade identity while tightening the mobile loop:
- Kenney Future is now wired into body/data typography while Orbitron remains the display voice.
- The non-functional Multiplayer menu action was removed and the main menu was rebalanced around two real actions.
- The settings volume slider now tracks touch-drag continuously and flushes persistence on release/pause instead of every drag frame.
- GameScreen responsibilities were split into ArenaBuilder, UpgradeManager, and UiFontSet.
- WallSpatialGrid narrows hot collision and line-of-sight queries to nearby tile cells.
- Three authored arena variants are selected per run.
- Enemy movement now adds lightweight role-based strafing/spacing while preserving the existing per-frame AI architecture.
- Fire-rate upgrade stacking was softened and the combo grace window was increased slightly.
- UI buttons now breathe subtly when idle and darken on press; lifecycle guards protect resize/dispose before initialization.


Sparky Warfare is an Android-only, landscape-first top-down tank combat game built with LibGDX 1.12.1 + Kotlin.

The game combines a deep atmospheric arena, a glassmorphic soft-neon arcade UI, glowing energy tanks, laser combat, destructible cover, wave-based enemies, upgrades, power-ups, particle debris, framebuffer bloom, and responsive Android multitouch controls.

## Current status

The single-player loop is playable and covered by the Android CI workflow:

Main Menu → Combat → Wave Clear → Upgrade → Combat → Game Over → Retry / Main Menu

Current gameplay systems include:
- Single-player tank combat.
- Continuous player firing.
- Responsive virtual joystick movement.
- Independent multitouch fire tracking.
- Left/right control-side swapping.
- Enemy chase-and-fire AI with delayed turret tracking and independent hull/turret rotation.
- Wall-aware line-of-sight checks.
- Destructible brick, reinforced brick, concrete, and metal cover.
- Indestructible steel arena boundaries.
- Centralized wave scaling and elite-wave rules.
- Enemy health bars.
- Fast scouts, assault tanks, heavy tanks, and ranged units.
- Stacking upgrades with per-upgrade limits.
- Rapid-fire, shield, spread-shot, score, repair, and overdrive effects.
- Persistent high score, best wave, and total-kill statistics.
- Game-over retry and Main Menu flows.
- First-run combat briefing.
- Android immersive presentation.
- Real framebuffer bloom and additive glow.
- Pooled transient combat entities and bounded particle debris.

## UI redesign

The UI follows a **glassmorphic Soft Neon Arcade** direction inspired by polished mobile trivia/arcade interfaces: deep atmospheric navy, frosted translucent surfaces, layered depth, cyan↔violet gradients, soft bloom, and large rounded controls.

### Visual language
- Translucent dark-glass surfaces with visible depth.
- Soft cyan and violet/magenta edge glow.
- Cyan-to-violet gradients used for primary actions, health, sliders, and live announcements.
- Rounded cards with luminous highlights and restrained shadows.
- Layered background cards on the main menu for depth.
- Large, airy typography with measured fitting so labels do not clip or overlap.
- Atmospheric cyan/violet pools in the arena instead of a tactical grid.
- Large translucent MOVE and FIRE touch controls with shared cyan/magenta rims and alpha blending.

The shared primitives live in UiShapes.kt: glassPanel, glassCard, softButton, glassCircle, gradientRoundedRect, ambientGlow, plus centralized begin/end helpers for alpha blending. Screen renderers and gameplay HUD passes consume the same primitives so translucency, gradients, glow, and geometry remain consistent across the entire app.

### Typography

The UI uses two deliberate typography tiers:
- Orbitron Medium for major headings and large command labels.
- Kenney Future for statistics, descriptions, instructions, and supporting copy.

The typography is still constrained through the shared UiText fitting helpers so compact landscape screens do not force text outside its intended surface.

## Landscape and orientation

Android is explicitly configured for landscape-only presentation with both landscape rotations using sensorLandscape.

The launcher activity also handles orientation and screen-size configuration changes. GameScreen.resize() updates the gameplay viewport, HUD camera, bloom framebuffer, and shared UI safe-area geometry whenever the display changes.

The UI does not assume a single fixed landscape resolution. HUD text uses measured row spacing and dedicated regions for score, best, core, health, and pause controls.

## Screen layouts

### Main menu

The main menu is explicitly landscape-oriented:
- A large central glass panel anchors the screen.
- Layered glass cards sit behind the main panel to create depth.
- The left side carries the title and persistent stats.
- The right side contains large SINGLE PLAYER and SETTINGS glass buttons.
- A compact fallback arrangement remains available for unusual window sizes.

### Combat HUD
- Combat/session status at upper left.
- Wave and score information in separated rows.
- Player health at upper right with the single player health bar.
- Pause control uses a larger touch target and icon.
- Score and Best are separated into distinct measured rows.
- Large MOVE and FIRE controls anchored to safe landscape thumb zones.

### Pause

Pause is a centered glass card with soft-neon RESUME and MAIN MENU actions.

### Game over

The game-over screen uses a centered glass result card, separated score/best cards, a compact wave/kills/combo row, and two large actions: REDEPLOY and MAIN MENU.

### Settings

Settings uses a landscape glass control matrix:
- SFX and Haptics occupy separate control cards.
- Volume receives its own full-width slider.
- `SWAP: LEFT / RIGHT` explicitly moves the MOVE joystick and FIRE control to the opposite sides.
- Back occupies its own command card.
- Secondary descriptions use the shared futuristic display font and measured text fitting.

### Upgrade screen

Upgrade selection remains a three-card landscape layout with glass surfaces, luminous accents, readable descriptions, and a clear INSTALL cue.

## Touch controls

The combat controls are designed for landscape mobile play:
- Large MOVE joystick.
- Large FIRE control.
- Independent multitouch pointer tracking.
- Safe-area-aware placement.
- Enlarged hit zones.
- Visual joystick travel clamped to its actual movement radius.
- MOVE/FIRE sides can be swapped for left-handed play.
- Control positions are recalculated when the display changes size or rotation.

## Game-session foundation

The runtime now has an explicit game-session boundary before networking is introduced:
- GameMode identifies the current simulation mode without adding multiplayer behavior.
- GameSession owns mode/participant metadata and the local player slot.
- Single-player is the only active mode in this build; networking remains intentionally out of scope.
- The session boundary remains available for a future network/input layer without shipping a fake multiplayer entry point.
- No sockets, Bluetooth/Wi-Fi transport, lobby service, or multiplayer UI is included yet.

This keeps networking as a future session/input layer rather than requiring a second combat implementation.

## Architecture

The project intentionally remains Android-only.

Important responsibilities are separated into focused systems:
- GameScreen — lifecycle, state transitions, simulation coordination, persistence, resizing, and input routing.
- UiLayout — centralized safe-area geometry and reusable interactive rectangles.
- MenuRenderer — landscape tactical main console.
- GameScreen — lifecycle, input routing, combat orchestration, and HUD coordination.
- ArenaBuilder — authored arena variants and collision geometry construction.
- UpgradeManager — upgrade choice filtering, stack limits, and player effects.
- UiFontSet — centralized Orbitron/Kenney Future font loading and disposal.
- WallSpatialGrid — tile-based wall indexing for movement and line-of-sight queries.
- PauseRenderer — pause console.
- GameOverRenderer — combat-result screen.
- SettingsRenderer — device/control configuration matrix.
- UpgradeRenderer — three-card upgrade selection.
- WorldRenderer — arena, walls, enemy bars, and touch controls.
- TankRenderer — procedural polygon hulls, independent turrets, reactor cores, and tier-specific weapon silhouettes.
- InputController — multitouch state and joystick lifecycle.
- CollisionSystem — tank movement, separation, wall tests, laser intersections, and line-of-sight checks.
- CombatSystem — damage routing and kill scoring.
- EnemySpawner — validated enemy placement and composition.
- WaveManager — wave, elite, upgrade, and bonus rules.
- PowerUpManager — pickup selection and safe placement.
- EntityPools — reusable transient laser/burst objects.
- BloomRenderer — framebuffer glow pass.
- ParticleDebris — bounded runtime debris effects.

## Rendering

The visual system uses a deep atmospheric battlefield with soft neon energy accents and glassmorphic rounded UI surfaces.

TankRenderer owns the procedural tank geometry. Each tank uses a resolution-independent polygon silhouette with a separate turret aim, reactor, armor fins, and tier-specific weapon signature. Scout, assault, heavy, ranged, elite, and player tanks therefore remain visually distinguishable without sprite files.

GlowRenderer provides shared glow treatment for lasers, power-ups, and impact effects.

The glow layer is rendered into a framebuffer and processed through assets/shaders/glow.vert and assets/shaders/glow.frag. The composite uses a lightweight bright-pass/blur approach suitable for Android.

The arena intentionally has no tactical grid. Subtle cyan/violet atmospheric pools provide depth without adding line noise.

## Stability and performance

The runtime has been hardened around common mobile failure points:
- Circular tank footprints for collision.
- Player/enemy separation with post-separation bounds correction.
- Enemy firing requires line of sight.
- Laser collision uses travelled segments to reduce fast-shot tunnelling.
- Player damage has invulnerability handling.
- Restart clears transient input.
- Large frame deltas are capped.
- Transient lasers and bursts are pooled.
- Particle effects are bounded and cleaned up.
- Audio initialization failures do not terminate gameplay.
- Idle/menu camera positioning is deterministic.
- Android back handling is state-aware.
- Renderer hot paths reuse common objects where practical.
- UI geometry is centralized instead of duplicated across screens.
- Text fitting never allows the readability minimum to exceed the actual available width.

## Safe-area and resize rules

UiLayout is the single source of truth for interactive screen geometry.

It uses Android safe insets and recalculates main-menu controls, settings controls, pause controls, game-over actions, HUD pause placement, upgrade hit rectangles, and tutorial action geometry.

The HUD camera is rebuilt to the actual Android pixel dimensions during GameScreen.resize(). The pause target and touch-control geometry are recalculated with the same safe-area data.

## Assets and licensing

Third-party assets are documented in assets/licenses/THIRD_PARTY_ASSETS.md.

Current documented assets include:
- Orbitron Medium — SIL Open Font License 1.1.
- Kenney Future — CC0.
- Kenney Sci-fi Sounds — CC0.
- Kenney Interface Sounds — CC0.
- Launcher icon graphics — project-authored vector XML.

No proprietary game franchise assets are required by the project.

## Building locally

With Java 17, Android SDK, and Gradle available:

    gradle :android:assembleDebug --no-daemon

The debug APK is produced under:

    android/build/outputs/apk/debug/

## GitHub Actions

The Android workflow is defined in .github/workflows/build.yml.

CI validates the Android project and builds the debug APK.

Current build environment:
- JDK 17
- Android platform API 34
- Android build-tools 34.0.0
- Gradle 8.7
- Android Gradle Plugin 8.5.0
- Kotlin 1.9.24
- LibGDX 1.12.1
- minSdk 24
- targetSdk 34

The workflow verifies the resulting APK and its LibGDX native ABI packaging before uploading the debug APK artifact.

## Repository structure

    android/                         Android launcher and packaging
    core/                            Game logic and LibGDX rendering
    assets/                          Fonts, shaders, audio, particles
    assets/licenses/                 Third-party asset attribution
    .github/workflows/build.yml      Android CI
    README.md                        Project documentation

## Development priorities

The current foundation is intentionally stable before adding larger gameplay features. The Soft Neon pass also replaces the remaining wave-announcement strip treatment with a rounded gradient card. Recent hardening also covers HUD spacing, pause visibility, joystick label centering, explicit control-side labeling, the first-run tutorial gate, and the shared glassmorphic UI treatment.

1. More arena layouts.
2. More enemy behavior and attack patterns.
3. Optional mini-boss encounters.
4. More upgrade effects.
5. Additional combat feedback and audio polish.
6. Expanded multiplayer only if a real networking architecture is introduced.

The main menu no longer exposes a dead-end multiplayer button. Networking is deferred rather than represented by a non-functional control.


## CI baseline

The repository's `main` branch is configured to run the Android build workflow on every push. The workflow validates the Android-only structure, builds the debug APK, and checks LibGDX native ABI packaging.


## UI design system

Sparky Warfare uses a shared **Soft Neon Arcade** UI system so screens remain visually consistent and inexpensive to maintain.

- `UiTheme.kt` — semantic colors, text roles, and shared geometry metrics.
- `UiShapes.kt` — reusable rounded panels, overlays, gradients, and soft-neon buttons.
- `UiText.kt` — shared text fitting and alignment helpers that protect against clipping on compact landscape screens.
- `UiLayout.kt` — safe-area-aware touch targets and enlarged multitouch hit regions.

New UI should consume these shared primitives instead of introducing screen-specific colors, corner radii, or button treatments.


## Known limitations / not yet implemented

- Multiplayer networking, lobbies, LAN transport, and matchmaking are not implemented.
- Enemy AI remains deliberately lightweight; it now adds role-aware strafing/spacing but does not perform full pathfinding or coordinated squad tactics.
- Arena variety is limited to three authored layouts.
- No new third-party assets were introduced in this pass.
