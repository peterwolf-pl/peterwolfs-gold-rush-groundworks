package com.piotrek.goldrushgroundworks.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

import java.util.function.Consumer;

public class AuriferousGravelItem extends BlockItem {
    private final int goldPercentage;

    public AuriferousGravelItem(Block block, int goldPercentage, Item.Properties properties) {
        super(block, properties);
        this.goldPercentage = goldPercentage;
    }

    public int getGoldPercentage() {
        return goldPercentage;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay display,
            Consumer<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, display, tooltip, flag);

        tooltip.accept(
                Component.translatable(
                        "tooltip.pw_gold_rush_groundworks.gold_content",
                        goldPercentage + "%"
                ).withStyle(ChatFormatting.GOLD)
        );
        tooltip.accept(
                Component.translatable(
                        "tooltip.pw_gold_rush_groundworks.gold_probability",
                        goldPercentage + "%"
                ).withStyle(ChatFormatting.YELLOW)
        );
    }
}
