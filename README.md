# Sparky Warfare

An Android-only top-down tank-combat game built with **LibGDX 1.12.1 + Kotlin**. The game uses glowing energy tanks, laser fire, destructible walls, wave-based enemies, power-ups, particle debris, real framebuffer bloom, and Android touch controls.

## Status

The complete single-player gameplay loop is playable and hardened for modern Android:

- Menu → play → upgrade → game over → retry
- First-run field briefing explaining movement, firing, waves, upgrades, and combos
- Player movement and continuous firing
- Enemy chase-and-fire AI with wall line-of-sight checks
- Destructible brick, reinforced brick, concrete, and metal barriers plus indestructible steel walls
- Centralized wave scaling with elite-wave rules and wave-start banners
- Visible enemy health bars, including elite tanks
- Expanded upgrade pool with stacking limits
- Rapid-fire, score, shield, spread-shot, and overdrive power-ups
- Android multitouch movement/fire controls
- Left-handed control-side swap
- Dedicated translucent virtual joystick and fire button
- SFX mute plus persistent master-volume slider
- Haptic feedback toggle
- Full-screen immersive Android presentation
- Wide 1600×900 world with predictive camera look-ahead
- Persistent high score, best wave, and kill statistics
- Game-over retry and Main Menu paths
- Adaptive Android launcher icon with vector fallback
- Real framebuffer-based bright-pass/blur bloom for the glow layer
- Programmatic pooled-style particle debris for combat impacts without a fragile text-emitter parser

## Architecture

The project remains intentionally Android-only with no desktop target. Gameplay responsibilities are separated into focused systems:

- `CollisionSystem` — circular tank movement, separation, wall tests, laser intersections, and LOS checks
- `CombatSystem` — damage routing and centralized kill scoring
- `EnemySpawner` — validated spawn placement and enemy composition
- `WaveManager` — single source of truth for wave counts, elite cadence, upgrade cadence, and bonuses
- `PowerUpManager` — pickup selection and safe placement
- `EntityPools` — reusable transient laser/burst entities
- `InputController` — multitouch fire tracking, joystick lifecycle, and pause-safe input
- `UiLayout` — safe-area geometry and reusable screen-space button rectangles
- `HudRenderer` — shared HUD typography/layout helpers
- `BloomRenderer` — framebuffer glow pass and shader composite
- `ParticleDebris` — lightweight runtime debris effects
- `GameScreen` — screen lifecycle, state transitions, simulation coordination, persistence, and input routing

The old unused `core/.../game/ui/` stub package has been removed so there is one UI layout source instead of duplicate renderer/safe-area classes.

## Visual direction

The visual system uses a near-black battlefield, cyan player energy, warm neon enemy accents, translucent tactical panels, bright laser cores, layered impact rings, and soft additive bloom. Orbitron remains the headline/tactical display font; the bundled LibGDX default bitmap font is used for compact body/briefing copy so small explanatory text does not require scaling the headline font.

## Android controls

- Use the **MOVE** virtual joystick.
- Hold the **FIRE** control to shoot continuously.
- The Settings screen can swap the MOVE/FIRE sides for left-handed play.
- Multiple fire touches are tracked independently.
- Tap **Single Player** to start.
- **Multiplayer** remains visibly unavailable rather than pretending to provide a working mode.
- Pause is available during active play.
- After game over, choose **RETRY** or **MAIN MENU**.

## Building

With Gradle installed:

```bash
gradle :android:assembleDebug --no-daemon
```

The APK is generated at:

```
android/build/outputs/apk/debug/
```

## GitHub Actions

`.github/workflows/build.yml` builds only the Android debug APK on pushes to `main`, pull requests, and manual runs.

CI uses:

- JDK 17
- Android platform API 34
- Android build-tools 34.0.0
- Gradle 8.7
- Android Gradle Plugin 8.5.0
- Kotlin 1.9.24
- LibGDX 1.12.1
- minSdk 24 / targetSdk 34

The workflow verifies that only `core` and `android` modules exist, rejects forbidden desktop-target references, builds the debug APK, verifies all four LibGDX native ABIs, and uploads the APK artifact.

## Presentation and safe areas

The playable world is substantially wider and taller than the camera view, and the camera follows the player with smooth clamping plus a small movement-direction look-ahead.

HUD, menus, settings, upgrade cards, pause controls, and touch controls use the shared `UiLayout` safe-area geometry. Android display-cutout and gesture insets are respected so important controls stay inside the usable region.

## Rendering

`GlowRenderer` draws the shared energy language for tanks, lasers, power-ups and impact rings. Those glow elements are rendered into a transparent framebuffer, then passed through the bundled `glow.vert` / `glow.frag` shader for a luminance threshold and small multi-tap blur before being composited additively over the battlefield. The post-process is deliberately lightweight for Android.

`ParticleDebris` uses the bundled `particle.png` directly and generates short-lived debris bursts in code. This avoids loading the previously malformed legacy `.p` emitter resource during screen startup.

## Stability and performance

- Tank movement uses a circular footprint instead of a center-point wall test.
- Player and enemies are kept separated and clamped inside the arena.
- Enemy firing is timer-based and requires line of sight.
- Waves use bounded, composition-driven scaling rather than unbounded linear stat growth.
- Enemy roles vary between fast scouts, assault tanks, heavy tanks, and ranged units.
- Elite tanks appear on the centralized elite cadence and expose their health through an on-screen bar.
- Laser collision uses the travelled segment, preventing fast shots from skipping targets or walls.
- Player damage has invulnerability frames.
- Restart clears joystick and all active fire pointers.
- Large frame deltas are capped to avoid physics jumps after a stalled frame.
- Transient lasers and bursts are pooled.
- Particle debris is bounded and removes expired effects backwards through its active list.
- Audio initialization failures are isolated so unsupported audio devices do not stop gameplay.
- Safe-area calculations are centralized rather than repeated across screens.

## Assets and licensing

Bundled third-party assets are documented in `assets/licenses/THIRD_PARTY_ASSETS.md`.

- Orbitron Medium — OFL-1.1
- Kenney Sci-fi Sounds — CC0
- All new launcher icon graphics are hand-authored vector XML in the project and require no third-party license.

## Future work

1. Additional arena layouts and deeper enemy behavior.
2. Optional mini-boss encounters as a stretch feature.
3. Additional distinct UI/power-up audio assets if the audio pack is expanded.
4. Multiplayer remains intentionally out of scope for this pass.


<!-- Menu touch validation -->
