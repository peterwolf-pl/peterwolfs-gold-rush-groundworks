package com.piotrek.goldrushgroundworks.block;

import com.piotrek.goldrushgroundworks.item.AuriferousGravelItem;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuriferousGravelTest {

    @BeforeAll
    static void setUp() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        enableIntrusiveHolders(BuiltInRegistries.ENTITY_TYPE);
        enableIntrusiveHolders(BuiltInRegistries.BLOCK);
        enableIntrusiveHolders(BuiltInRegistries.ITEM);
        unfreeze(BuiltInRegistries.FEATURE_TYPE);
    }

    private static void unfreeze(Object registry) {
        try {
            Field frozen = MappedRegistry.class.getDeclaredField("frozen");
            frozen.setAccessible(true);
            frozen.set(registry, false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void enableIntrusiveHolders(Object registry) {
        try {
            unfreeze(registry);
            Field intrusive = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
            intrusive.setAccessible(true);
            if (intrusive.get(registry) == null) {
                intrusive.set(registry, new IdentityHashMap<>());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void blocksAndItemsRetainConfiguredPercentages() {
        int[] tiers = {10, 25, 50, 80};

        for (int pct : tiers) {
            ResourceKey<Block> blockKey = ResourceKey.create(
                    Registries.BLOCK,
                    Identifier.fromNamespaceAndPath("pw_gold_rush_groundworks", "test_gravel_" + pct)
            );
            AuriferousGravelBlock block = new AuriferousGravelBlock(
                    pct,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)
                            .setId(blockKey)
            );
            assertEquals(pct, block.getGoldPercentage());
            assertEquals(-8356741, AuriferousGravelBlock.GRAVEL_DUST_COLOR.rgba());

            ResourceKey<Item> itemKey = ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("pw_gold_rush_groundworks", "test_gravel_" + pct)
            );
            AuriferousGravelItem item = new AuriferousGravelItem(
                    block,
                    pct,
                    new Item.Properties()
                            .setId(itemKey)
                            .useBlockDescriptionPrefix()
            );
            assertEquals(pct, item.getGoldPercentage());
            assertSame(block, item.getBlock());
        }
    }

    @Test
    void itemTooltipAppendsGoldContentAndProbability() {
        int[] tiers = {10, 25, 50, 80};

        for (int pct : tiers) {
            ResourceKey<Block> blockKey = ResourceKey.create(
                    Registries.BLOCK,
                    Identifier.fromNamespaceAndPath("pw_gold_rush_groundworks", "test_gravel_" + pct)
            );
            AuriferousGravelBlock block = new AuriferousGravelBlock(
                    pct,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)
                            .setId(blockKey)
            );
            ResourceKey<Item> itemKey = ResourceKey.create(
                    Registries.ITEM,
                    Identifier.fromNamespaceAndPath("pw_gold_rush_groundworks", "test_gravel_" + pct)
            );
            AuriferousGravelItem item = Registry.register(
                    BuiltInRegistries.ITEM,
                    itemKey,
                    new AuriferousGravelItem(
                            block,
                            pct,
                            new Item.Properties()
                                    .setId(itemKey)
                                    .useBlockDescriptionPrefix()
                    )
            );

            ItemStack stack = ItemStack.EMPTY;
            List<Component> tooltip = new ArrayList<>();

            item.appendHoverText(
                    stack,
                    null,
                    TooltipDisplay.DEFAULT,
                    tooltip::add,
                    TooltipFlag.NORMAL
            );

            assertEquals(2, tooltip.size());

            Component contentComp = tooltip.get(0);
            Component probComp = tooltip.get(1);

            assertInstanceOf(TranslatableContents.class, contentComp.getContents());
            assertInstanceOf(TranslatableContents.class, probComp.getContents());

            TranslatableContents contentTrans = (TranslatableContents) contentComp.getContents();
            TranslatableContents probTrans = (TranslatableContents) probComp.getContents();

            assertEquals("tooltip.pw_gold_rush_groundworks.gold_content", contentTrans.getKey());
            assertEquals("tooltip.pw_gold_rush_groundworks.gold_probability", probTrans.getKey());

            assertEquals(1, contentTrans.getArgs().length);
            assertEquals(pct + "%", contentTrans.getArgs()[0]);

            assertEquals(1, probTrans.getArgs().length);
            assertEquals(pct + "%", probTrans.getArgs()[0]);
        }
    }
}
