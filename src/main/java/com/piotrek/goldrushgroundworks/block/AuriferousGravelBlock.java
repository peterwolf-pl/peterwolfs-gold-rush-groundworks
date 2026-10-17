package com.piotrek.goldrushgroundworks.block;

import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AuriferousGravelBlock extends ColoredFallingBlock {
    public static final ColorRGBA GRAVEL_DUST_COLOR = new ColorRGBA(-8356741);
    private final int goldPercentage;

    public AuriferousGravelBlock(int goldPercentage, BlockBehaviour.Properties properties) {
        super(GRAVEL_DUST_COLOR, properties);
        this.goldPercentage = goldPercentage;
    }

    public int getGoldPercentage() {
        return goldPercentage;
    }
}
