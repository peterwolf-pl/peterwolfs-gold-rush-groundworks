# Peterwolf's Gold Rush Groundworks

Early-release gold recovery expansion for Minecraft Java 26.3 / Fabric and Peterwolf's Groundworks.

## What it does

- Adds a dedicated Gold Rush gradation screen based on the Groundworks excavated-material screen.
- Defaults to dedicated **Gold screening mode** (`Złoto` / `Gold`).
- Keeps normal Groundworks material sorting and conveyor outputs.
- Gravel processed through the wash plant produces native `minecraft:gold_nugget` items.
- Default yield is exactly 2 gold nuggets per 10 full Groundworks blocks of processed gravel.
- Yield is deterministic and based only on gravel that actually leaves the machine.
- Partial gold-yield progress is saved with the screen entity and survives world reloads.
- No custom gold item is used. The output is the vanilla Minecraft gold nugget.

## Material selection & Gold screening mode

Right-click the machine to choose the screened material:

- **Empty hand:** cycles through: `Złoto` (Gold) → `Ziemia` (Dirt) → `Piasek` (Sand) → `Żwir` (Gravel) → `Bruk` (Cobblestone) → `Złoto` (Gold).
- **Gold items:** right-clicking with any gold item (gold nugget, raw gold, gold ingot, gold ore, gold block, raw gold block) immediately switches to **Złoto** (Gold screening mode).
- **Block items:** right-clicking with dirt, sand, gravel, or cobblestone sets the machine to sort that specific material.

When set to **Złoto (Gold)**:
- Gold-bearing paydirt (gravel) is routed down the side wash conveyor directly over the gold recovery sluice box.
- Native `minecraft:gold_nugget` items drop into the collection point beneath the sluice.
- Tailings and oversize waste (dirt, cobblestone) are discharged out the rear conveyor.

## Hopper collection

Gold nuggets drop from a dedicated collection point below the side conveyor, outside the machine collision box.

Place a vanilla hopper under that part of the conveyor and point the hopper into a chest. Nuggets are normal item entities, so the hopper collects them with standard Minecraft mechanics and the chest receives native `minecraft:gold_nugget` stacks.

The granular gravel stream stays separate from the nugget collection point, so it does not need to pass through the hopper.

## Yield command

Current setting:

    /goldrush nuggets-per-10-blocks

Set the amount of gold nuggets produced per 10 full blocks of processed gravel:

    /goldrush nuggets-per-10-blocks <amount>

Example:

    /goldrush nuggets-per-10-blocks 2

Default: `2`.

Allowed range: `0-1024`.

Setting `0` disables new gold recovery. Changing the rate is persistent in:

    config/pw_gold_rush_groundworks.properties

Changing the value requires moderator command permission. Reading the current value does not.

## Gold accounting

Groundworks uses 512 integer material units per full block. Gold Rush uses a weighted carry accumulator, so fractional progress is never lost.

At the default rate:

- 5 processed gravel blocks produce 1 nugget.
- 10 processed gravel blocks produce 2 nuggets.
- 100 processed gravel blocks produce 20 nuggets.

If a receiver is blocked and gravel does not actually leave the screen, the gold counter does not advance.

## Crafting and placement

The Gold Rush screen uses the same early-release recipe layout as the gradation screen:

    I I I
    R H R
    I P I

- I = iron ingot
- R = redstone
- H = hopper
- P = piston

Right-click a block with the item to place the machine. Shift + right-click the empty machine with an empty hand retrieves it.

## Build

Requirements:

- Minecraft 26.3
- Fabric Loader 0.19.5+
- Fabric API 0.160.7+26.3
- Java 25
- Peterwolf's Groundworks 0.1.0+26.3

Publish Groundworks to Maven Local first:

    cd ../peterwolfs-groundworks
    ./gradlew publishToMavenLocal

Then build Gold Rush Groundworks:

    ./gradlew clean test build

The resulting jar is written to `build/libs`.
