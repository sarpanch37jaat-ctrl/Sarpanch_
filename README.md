# RenderGuard

A small Fabric mod for Minecraft 1.21.x that demonstrates two real
client-side rendering optimizations, plus a HUD readout and a runtime
config command. It's meant as a working, hackable starting point rather
than a drop-in replacement for the big optimization mods below.

## What it actually does

1. **Distance-based entity culling** (`EntityRenderDispatcherMixin`)
   Vanilla frustum-culls entities but applies no distance cutoff, so a
   skeleton three chunks away still goes through full model rendering,
   texture binding, and buffer building even though it's a couple of
   pixels on screen. RenderGuard injects at the head of
   `EntityRenderDispatcher.render` and cancels the call for any entity
   beyond a configurable distance from the camera (default 48 blocks),
   while always exempting the camera entity, its vehicle/passengers, and
   other players.

2. **Distance-based particle culling** (`ParticleManagerMixin`)
   Injects at the head of `ParticleManager.addParticle` and skips
   constructing/tracking the particle entirely if it spawns beyond a
   configurable distance (default 32 blocks). This is cheaper than
   spawning the particle and culling it later during rendering.

3. **HUD + live config**
   A small green line in the top-left shows entities/particles culled in
   the last second. `/renderguard` (client-side command, works in
   singleplayer and on any server) lets you toggle the mod or change the
   distances without restarting:
   ```
   /renderguard                        - show current settings
   /renderguard toggle                 - enable/disable everything
   /renderguard hud <true|false>
   /renderguard entitydistance <blocks>
   /renderguard particledistance <blocks>
   ```
   Settings persist to `config/renderguard.json`.

## Research context (mid-2026 snapshot)

Fabric remains the go-to loader for pure performance work, since its
lightweight injection model lets optimization mods patch precisely the
methods they need instead of hooking a heavier event bus. The mods that
define the current state of the art, and that inspired the two
techniques above:

- **Sodium** - a full replacement of the vanilla chunk/terrain rendering
  pipeline (better batching, frustum culling, GPU-friendly meshing).
  This is the single biggest FPS win available and is not something a
  small mixin mod can reasonably reimplement.
- **Lithium** - rewrites vanilla game-logic hot paths (AI, block ticking,
  collision) without changing behavior; mostly matters for TPS/CPU load
  rather than FPS.
- **Starlight** - rewrites the lighting engine so chunk light updates
  stop being one of the game's classic lag sources.
- **Entity-culling-style mods** and **Krypton/FerriteCore/Clumps** round
  out the ecosystem: respectively, skipping rendering of
  occluded/off-screen things, optimizing the network stack, cutting
  memory overhead, and merging XP orbs to reduce entity counts.

If your actual goal is "make my game run better" rather than "build a
mod," installing Sodium + Lithium + Starlight (or a curated pack like
Fabulously Optimized) will outperform anything a custom mixin mod does
in an afternoon. RenderGuard is useful either as a teaching example for
how those mods hook into the game, or as a base to extend with your own
project-specific optimizations (e.g. culling a custom entity type your
own mod adds).

## Building

Requires JDK 21 and a network connection (Gradle needs to download the
Fabric toolchain and a deobfuscated Minecraft jar on first run).

This project ships `gradle/wrapper/gradle-wrapper.properties` but not the
wrapper jar/scripts themselves (they're binary and couldn't be fetched in
the sandbox this was generated in). If you have a local Gradle install,
generate them once with:

```bash
gradle wrapper --gradle-version 8.10
```

Then build as usual:

```bash
./gradlew build
```

The built mod jar appears in `build/libs/renderguard-1.0.0.jar`. Drop it
into `.minecraft/mods` alongside Fabric Loader and Fabric API for
Minecraft 1.21.11.

To launch a dev client directly:

```bash
./gradlew runClient
```

### Before you build: double-check the version pins

Minecraft/Fabric versions move fast. The numbers in `gradle.properties`
(`minecraft_version`, `yarn_mappings`, `loader_version`, `loom_version`,
`fabric_version`) were current for 1.21.11 as of when this project was
generated, but weren't verified against a live build in this
environment (no network access here). Before your first build, check
the recommended combination for your target 1.21.x build at
https://fabricmc.net/develop and adjust `gradle.properties` if anything
has moved on.

## Extending it

Ideas if you want to keep going:
- Exempt entities with a visible custom nameplate from culling so tamed
  pets/named mobs don't vanish.
- Scale `entityCullDistance` automatically off the player's configured
  render distance instead of a flat default.
- Add a Cloth Config screen instead of the chat command.
- Port the same culling idea to block entities (banners, signs, chests)
  via `BlockEntityRenderDispatcher`.
