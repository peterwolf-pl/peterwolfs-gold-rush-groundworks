# Changelog

All notable changes to **Peterwolf's Gold Rush Groundworks** are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.2.0+26.3] - 2026-10-17

### Added
- **4 New Auriferous Gravel Types** (`auriferous_gravel_10`, `auriferous_gravel_25`, `auriferous_gravel_50`, `auriferous_gravel_80`):
  - Different gold content tiers with identical native gravel textures, icons, sounds, and falling block physics.
  - Informative tooltips displaying gold content percentage and occurrence probability in both English and Polish.
  - Added to Groundworks creative tab (`pw_groundworks:main`) and natural blocks tab (`minecraft:natural_blocks`).
  - Drop themselves on break (supported by shovels under `#minecraft:mineable/shovel` and `#c:gravels`).
  - Integrated with Gold Rush screening plant (`GoldRushScreenEntity`), allowing direct material selection and granular conversion.
- **New Biome: Gold Mine** (`pw_gold_rush_groundworks:gold_mine` / *Kopalnia złota*):
  - Flat alluvial terrain featuring a continuous winding depression indicating an ancient paleochannel riverbed.
  - Planed and leveled terrace elevation (high erosion climate span + automated plateau leveling) eliminating jagged bumps.
  - **Stratigraphy & Geology**:
    - Overburden: 1 to 3 surface layers of grass and dirt populated by oak and spruce forest clumps (`gold_mine_forest_clumps`), bushes, and flowers.
    - Sub-surface: 1 to 2 layers of sand mixed with low-yield auriferous gravel (10%).
    - Old Riverbed Core (paystreak): concentrated 3-row wide by 3-layer deep paystreak of high-yield auriferous gravel (80% and 50%).
    - Surrounding alluvial halo: mix of medium gravel (25%), low gravel (10%), vanilla gravel, and sand.
    - Bedrock floor: solid stone bedrock bench directly underneath the paystreak where gold accumulated, with surrounding dirt fill.
  - Completely dry terrain: zero precipitation, downfall 0.0, removed surface/spring water generators, and automatic water drainage in the paleochannel bed.
  - Registered in Overworld generation via `OverworldBiomeBuilderMixin` and tagged under `#minecraft:is_overworld` and `#c:is_overworld`.

---

## [0.1.0+26.3] - 2026-10-08

### Added
- Initial release of **Peterwolf's Gold Rush Groundworks** for Minecraft 26.3 Fabric.
- Sluice box gold panning and trommel washing plant integrated with Groundworks granular soil.
- Alluvial gold deposit generation in gravel beds, rivers, and mountain valleys.
- Concentrated paydirt processing yielding gold flakes and raw nuggets.
