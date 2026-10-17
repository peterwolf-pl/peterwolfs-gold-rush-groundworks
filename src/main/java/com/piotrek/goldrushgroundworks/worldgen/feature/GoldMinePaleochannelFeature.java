package com.piotrek.goldrushgroundworks.worldgen.feature;

import com.mojang.serialization.MapCodec;
import com.piotrek.goldrushgroundworks.GoldRushGroundworksMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;

public class GoldMinePaleochannelFeature implements Feature {
    public static final MapCodec<GoldMinePaleochannelFeature> CODEC =
            MapCodec.unit(GoldMinePaleochannelFeature::new);

    public GoldMinePaleochannelFeature() {}

    @Override
    public MapCodec<? extends Feature> codec() {
        return CODEC;
    }

    @Override
    public boolean place(WorldGenLevel level, ChunkGenerator generator, RandomSource random, BlockPos origin) {
        int chunkOriginX = origin.getX() & ~15;
        int chunkOriginZ = origin.getZ() & ~15;
        boolean placedAny = false;

        for (int dx = 0; dx < 16; dx++) {
            for (int dz = 0; dz < 16; dz++) {
                int worldX = chunkOriginX + dx;
                int worldZ = chunkOriginZ + dz;

                int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, worldX, worldZ);
                if (surfaceY <= level.getMinY() + 10 || surfaceY >= level.getMaxY() - 10) {
                    continue;
                }

                BlockPos samplePos = new BlockPos(worldX, surfaceY - 1, worldZ);
                Holder<Biome> biome = level.getBiome(samplePos);
                if (!biome.is(GoldRushGroundworksMod.GOLD_MINE_KEY)) {
                    continue;
                }

                // Continuous, seamless flat terrace height (around Y=69-71, dry and flat)
                int targetY = 69 + (int) Math.round(
                        Math.sin(worldX * 0.003D) * 1.5D + Math.cos(worldZ * 0.003D) * 1.5D
                );

                // Flatten the terrain: remove bumps above targetY, fill dips below targetY, drain water
                for (int y = Math.max(surfaceY + 2, targetY + 3); y > targetY; y--) {
                    BlockPos p = new BlockPos(worldX, y, worldZ);
                    BlockState s = level.getBlockState(p);
                    if (!s.isAir()) {
                        level.setBlock(p, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
                for (int y = Math.min(surfaceY, targetY); y < targetY; y++) {
                    BlockPos p = new BlockPos(worldX, y, worldZ);
                    level.setBlock(p, Blocks.DIRT.defaultBlockState(), 2);
                }

                // Meandering paleochannel formula: distance from the center of the ancient riverbed
                double channelOffset = Math.sin(worldX * 0.015D) * 35.0D + Math.cos(worldX * 0.006D) * 50.0D;
                double channelPeriod = 180.0D;
                double relZ = Math.floorMod((long) Math.floor(worldZ - channelOffset), (long) channelPeriod);
                if (relZ > channelPeriod / 2.0D) {
                    relZ -= channelPeriod;
                }
                double dist = Math.abs(relZ);

                if (dist <= 1.5D) {
                    // STARE KORYTO RZEKI (3 rzedy szerokosci: dist <= 1.5D)
                    // Lekkie zaglebienie (1 blok nizej niz plaska rownina)
                    int bedTopY = targetY - 1;
                    level.setBlock(new BlockPos(worldX, targetY, worldZ), Blocks.AIR.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(worldX, bedTopY, worldZ), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    level.setBlock(
                            new BlockPos(worldX, bedTopY - 1, worldZ),
                            random.nextBoolean() ? Blocks.SAND.defaultBlockState() : GoldRushGroundworksMod.AURIFEROUS_GRAVEL_10.defaultBlockState(),
                            2
                    );

                    // 3 warstwy najbogatszego zwiru (paystreak 3x3 w korycie)
                    for (int y = bedTopY - 2; y >= bedTopY - 4; y--) {
                        BlockState richState = random.nextFloat() < 0.65F
                                ? GoldRushGroundworksMod.AURIFEROUS_GRAVEL_80.defaultBlockState()
                                : GoldRushGroundworksMod.AURIFEROUS_GRAVEL_50.defaultBlockState();
                        level.setBlock(new BlockPos(worldX, y, worldZ), richState, 2);
                    }

                    // Ponizej lita skala (bedrock bench podloza aluwialnego)
                    level.setBlock(new BlockPos(worldX, bedTopY - 5, worldZ), Blocks.STONE.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(worldX, bedTopY - 6, worldZ), Blocks.STONE.defaultBlockState(), 2);

                    // Reszta to dirt
                    for (int y = bedTopY - 7; y >= bedTopY - 9; y--) {
                        level.setBlock(new BlockPos(worldX, y, worldZ), Blocks.DIRT.defaultBlockState(), 2);
                    }
                    placedAny = true;
                } else if (dist <= 6.5D) {
                    // Dookola koryta: mieszanka blokow mniej wartosciowego zwiru i piasku
                    level.setBlock(new BlockPos(worldX, targetY, worldZ), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(worldX, targetY - 1, worldZ), Blocks.DIRT.defaultBlockState(), 2);

                    // 2-3 warstwy mieszanki zwiru 25%, 10%, zwyklego gravelu i piasku
                    for (int y = targetY - 2; y >= targetY - 4; y--) {
                        float roll = random.nextFloat();
                        BlockState mix;
                        if (roll < 0.35F) {
                            mix = GoldRushGroundworksMod.AURIFEROUS_GRAVEL_25.defaultBlockState();
                        } else if (roll < 0.65F) {
                            mix = GoldRushGroundworksMod.AURIFEROUS_GRAVEL_10.defaultBlockState();
                        } else if (roll < 0.85F) {
                            mix = Blocks.GRAVEL.defaultBlockState();
                        } else {
                            mix = Blocks.SAND.defaultBlockState();
                        }
                        level.setBlock(new BlockPos(worldX, y, worldZ), mix, 2);
                    }

                    // Ponizej lita skala i dirt
                    level.setBlock(new BlockPos(worldX, targetY - 5, worldZ), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(worldX, targetY - 6, worldZ), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(worldX, targetY - 7, worldZ), Blocks.STONE.defaultBlockState(), 2);
                    placedAny = true;
                } else {
                    // Reszta biomu: 1 do 3 warstw narzutowych (dirt z trawa), ponizej 1-2 warstwy piasku ze zwirem o malym % zlota
                    int overburden = 1 + (Math.abs(worldX * 31 + worldZ * 17) % 3);
                    level.setBlock(new BlockPos(worldX, targetY, worldZ), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
                    for (int o = 1; o < overburden; o++) {
                        level.setBlock(new BlockPos(worldX, targetY - o, worldZ), Blocks.DIRT.defaultBlockState(), 2);
                    }

                    // 1 do 2 warstw piasku ze zwirem o malym prawdopodobienstwie zlota
                    int lowGravelLayers = 1 + (Math.abs(worldX * 13 + worldZ * 47) % 2);
                    int startGravelY = targetY - overburden;
                    for (int g = 0; g < lowGravelLayers; g++) {
                        BlockState lowMix = random.nextFloat() < 0.60F
                                ? GoldRushGroundworksMod.AURIFEROUS_GRAVEL_10.defaultBlockState()
                                : Blocks.SAND.defaultBlockState();
                        level.setBlock(new BlockPos(worldX, startGravelY - g, worldZ), lowMix, 2);
                    }

                    // Reszta to dirt, a ponizej lita skala
                    int dirtBottomY = startGravelY - lowGravelLayers;
                    level.setBlock(new BlockPos(worldX, dirtBottomY, worldZ), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(worldX, dirtBottomY - 1, worldZ), Blocks.DIRT.defaultBlockState(), 2);
                    level.setBlock(new BlockPos(worldX, dirtBottomY - 2, worldZ), Blocks.STONE.defaultBlockState(), 2);
                    placedAny = true;
                }
            }
        }

        return placedAny;
    }
}
