# Sparky Warfare

An Android-only top-down tank-combat game built with **LibGDX + Kotlin**. The game uses glowing energy tanks, laser fire, destructible walls, waves, power-ups, and Android touch controls.

## Status

The complete gameplay loop is playable, with a redesigned mobile presentation:

- Menu → play → game over → retry
- Player movement and continuous firing
- Enemy chase-and-fire AI
- Destructible brick, reinforced brick, concrete, and metal barriers plus indestructible steel walls
- Wave progression and scoring
- Rapid-fire and score power-ups
- Android multitouch movement/fire controls
- Dedicated translucent virtual joystick and fire button
- Full-screen immersive Android presentation
- Wide 1600×900 world with a camera that smoothly follows the player
- Redesigned main menu with **Single Player** and **Multiplayer** placeholder
- Upgraded HUD with aligned tactical typography, shadowed text, fixed-width HUD glyphs, and responsive panels
- Deep-black battlefield with clean open floor space and high-contrast combat lighting
- Layered glow, impact bursts, hit flash, and screen shake
- Bundled CC0 sci-fi SFX for lasers, hits, explosions, power-ups, and UI feedback
- Android haptic feedback for firing, hits, explosions, pickups, and menu actions
- Futuristic Orbitron typography generated from the bundled OFL-1.1 font at runtime through LibGDX FreeType
- Bundled CC0 Kenney sci-fi OGG effects for offline gameplay

Recent stability fixes harden the gameplay loop:

- Tank movement uses a circular footprint instead of a center-point wall test.
- Player and enemies are kept separated and clamped inside the arena.
- Enemy firing is timer-based instead of frame-rate-dependent random firing.
- Waves cap the enemy count while increasing speed, health, and firing difficulty.
- Enemy roles vary between fast scouts, assault tanks, heavy tanks, and ranged units.
- Arena cover uses mixed obstacle layouts with different materials and durability.
- Laser collision uses the travelled segment, preventing fast shots from skipping targets or walls.
- Restart clears joystick and all active fire pointers.
- Multiple Android fire touches are tracked independently.
- Large frame deltas are capped to avoid physics jumps after a stalled frame.
- Power-ups avoid walls, the player spawn area, and duplicate live pickups.
- Input is cleared when the screen is disposed.
- Touch-cancel and Android lifecycle pause paths clear active joystick/fire state.
- Audio initialization failures are isolated so unsupported audio devices do not stop gameplay.

## Android-only project layout

```
core/           # Shared LibGDX game logic and rendering
android/        # Only application target and Android launcher
assets/         # Shaders and particle resources
.github/        # Android-only GitHub Actions CI
```

The desktop/LWJGL module has been removed. There is no desktop launcher, desktop dependency, or desktop Gradle target.

Important gameplay files are under `core/src/main/kotlin/com/sparkywarfare/game/`.

## Android controls

- Use the dedicated **MOVE** virtual joystick on the lower-left.
- Hold the dedicated **FIRE** button on the lower-right.
- Multiple fire touches are handled safely.
- **Single Player** starts the current game mode.
- **Multiplayer** is a visual placeholder for a future mode.
- Tap after game over to retry.

There is no desktop keyboard-control path.

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

The workflow installs the required Android SDK packages directly and does not use the obsolete SDK `tools` package.

The generated `sparky-warfare-debug` artifact contains the debug APK.

## Presentation upgrade

The game no longer treats the device screen as the whole arena. The playable world is substantially wider and taller than the camera view, and the camera follows the player with smooth clamping at the world edges.

The Android presentation uses immersive full-screen mode. HUD and controls are rendered in a separate screen-space camera so they stay fixed while the battlefield scrolls underneath them.

## Rendering

`GlowRenderer` provides the shared layered glow style for tanks, lasers, power-ups and bursts. The current pass adds stronger bloom-like halos, brighter laser cores, multi-ring impacts, power-up pulses, varied obstacle materials, player hit flash, and subtle camera shake without adding a heavyweight rendering dependency.

## Future work

1. Real bloom via the included framebuffer shader.
2. Particle debris using the included LibGDX particle resource.
3. Tune the bundled sci-fi SFX mix and add additional CC0 UI/combat cues as needed.
4. Additional arena layouts and power-up types.
5. Persistent high scores.
6. More advanced enemy behavior.
\n<!-- CI validation marker -->
