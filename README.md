# Sparky Warfare

Sparky Warfare is an Android-only, landscape-first top-down tank combat game built with LibGDX 1.12.1 + Kotlin.

The game combines a dark cyber arena, a soft-neon arcade UI, glowing energy tanks, laser combat, destructible cover, wave-based enemies, upgrades, power-ups, particle debris, framebuffer bloom, and responsive Android multitouch controls.

## Current status

The single-player loop is playable and CI-validated:

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

The UI uses a **Soft Neon Arcade** direction: smooth rounded controls, pill-like cards, soft cyan/violet glow, large touch targets, and minimal text. It keeps the futuristic identity without the angular tactical-console look.

### Visual language
- Near-black tactical panels.
- Electric cyan structural accents.
- Magenta system highlights.
- Red/pink combat-danger states.
- Rounded surfaces and soft highlights instead of angular segmented panels.
- Subtle arena grid.
- Limited glow so important information remains readable.
- Large touch targets designed around landscape thumb zones.

### Typography

The UI now uses two deliberate typography tiers:
- Orbitron Medium for major tactical headings and command labels.
- Kenney Future for secondary descriptions, statistics, instructions, and explanatory copy.

This separation is intentional. Orbitron provides the futuristic identity while the less condensed body font prevents small explanatory text from becoming visually crowded.

Orbitron Medium is already bundled under the SIL Open Font License 1.1 and is documented in assets/licenses/THIRD_PARTY_ASSETS.md.

## Landscape and orientation

Android is explicitly configured for landscape-only presentation with both landscape rotations using sensorLandscape.

The launcher activity also handles orientation and screen-size configuration changes. GameScreen.resize() updates the gameplay viewport, HUD camera, bloom framebuffer, and shared UI safe-area geometry whenever the display changes.

The UI does not assume a single fixed landscape resolution. HUD text uses measured row spacing and dedicated regions for score, best, core, health, and pause controls.

## Screen layouts

### Main menu

The main menu is now explicitly landscape-oriented:
- Left side: game identity, system status, and persistent statistics.
- Right side: large DEPLOY, NETWORK, and SYSTEM command panels.
- The menu no longer relies on a tall portrait-style vertical stack on landscape devices.
- A compact fallback arrangement remains available for unusual window sizes.

### Combat HUD
- Combat/session status at upper left.
- Wave and score information in separated rows.
- Player health at upper right with the single player health bar.
- Pause control uses a larger touch target and icon.
- Score and Best are separated into distinct measured rows.
- Large MOVE and FIRE controls anchored to safe landscape thumb zones.

### Pause

Pause is a horizontal command console with RESUME and MAIN MENU actions separated into distinct large controls.

### Game over

The game-over screen uses a clear failure heading, separated score/best fields, a wave/kills/combo status row, and two horizontal actions: REDEPLOY and MAIN MENU.

### Settings

Settings uses a landscape control matrix:
- SFX and Haptics occupy separate control cards.
- Volume receives its own full-width slider.
- `SWAP: LEFT / RIGHT` explicitly moves the MOVE joystick and FIRE control to the opposite sides.
- Back occupies its own command card.
- Secondary descriptions use the body font instead of the condensed display font.

### Upgrade screen

Upgrade selection remains a three-card landscape layout with a large upgrade name, readable description, clear install action, and consistent cyber-neon framing.

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
- Single-player remains the only active mode today, with the existing enemy AI unchanged.
- Human-player slots are represented independently from AI enemies so a future local/LAN mode can reuse the same Tank, CombatSystem, CollisionSystem, WorldRenderer, and TankRenderer systems.
- No sockets, Bluetooth/Wi-Fi transport, lobby service, or multiplayer UI is included yet.

This keeps networking as a future session/input layer rather than requiring a second combat implementation.

## Architecture

The project intentionally remains Android-only.

Important responsibilities are separated into focused systems:
- GameScreen — lifecycle, state transitions, simulation coordination, persistence, resizing, and input routing.
- UiLayout — centralized safe-area geometry and reusable interactive rectangles.
- MenuRenderer — landscape tactical main console.
- GameScreen — combat HUD drawing, gameplay state, and input routing.
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

The visual system uses a near-black battlefield with soft neon energy accents and rounded UI surfaces.

TankRenderer owns the procedural tank geometry. Each tank uses a resolution-independent polygon silhouette with a separate turret aim, reactor, armor fins, and tier-specific weapon signature. Scout, assault, heavy, ranged, elite, and player tanks therefore remain visually distinguishable without sprite files.

GlowRenderer provides shared glow treatment for lasers, power-ups, and impact effects.

The glow layer is rendered into a framebuffer and processed through assets/shaders/glow.vert and assets/shaders/glow.frag. The composite uses a lightweight bright-pass/blur approach suitable for Android.

The arena also contains a subtle tactical grid. It is intentionally low contrast so it adds structure without competing with combat objects.

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
- Kenney Sci-fi Sounds — CC0.
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

The current foundation is intentionally stable before adding larger gameplay features. Recent hardening also covers HUD spacing, pause visibility, joystick label centering, explicit control-side labeling, and the first-run tutorial gate.

1. More arena layouts.
2. More enemy behavior and attack patterns.
3. Optional mini-boss encounters.
4. More upgrade effects.
5. Additional combat feedback and audio polish.
6. Expanded multiplayer only if a real networking architecture is introduced.

Multiplayer is currently presented as unavailable rather than exposing a non-functional fake mode.


## CI baseline

The repository's `main` branch is kept build-verified through GitHub Actions after each committed change.
