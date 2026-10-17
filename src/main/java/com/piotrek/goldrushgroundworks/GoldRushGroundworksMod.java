package com.piotrek.goldrushgroundworks;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.MapCodec;
import com.piotrek.goldrushgroundworks.block.AuriferousGravelBlock;
import com.piotrek.goldrushgroundworks.config.GoldRushConfig;
import com.piotrek.goldrushgroundworks.entity.GoldRushScreenEntity;
import com.piotrek.goldrushgroundworks.item.AuriferousGravelItem;
import com.piotrek.goldrushgroundworks.item.GoldRushScreenItem;
import com.piotrek.goldrushgroundworks.worldgen.feature.GoldMinePaleochannelFeature;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.levelgen.feature.Feature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GoldRushGroundworksMod implements ModInitializer {
    public static final String MOD_ID = "pw_gold_rush_groundworks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static final ResourceKey<EntityType<?>> GOLD_RUSH_SCREEN_KEY =
            ResourceKey.create(Registries.ENTITY_TYPE, id("gold_rush_screen"));

    public static final EntityType<GoldRushScreenEntity> GOLD_RUSH_SCREEN = Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            GOLD_RUSH_SCREEN_KEY,
            EntityType.Builder.of(GoldRushScreenEntity::new, MobCategory.MISC)
                    .sized(2.25F, 3.25F)
                    .clientTrackingRange(14)
                    .build(GOLD_RUSH_SCREEN_KEY)
    );

    public static final ResourceKey<Item> GOLD_RUSH_SCREEN_ITEM_KEY =
            ResourceKey.create(Registries.ITEM, id("gold_rush_screen"));

    public static final GoldRushScreenItem GOLD_RUSH_SCREEN_ITEM = Registry.register(
            BuiltInRegistries.ITEM,
            GOLD_RUSH_SCREEN_ITEM_KEY,
            new GoldRushScreenItem(
                    new Item.Properties()
                            .setId(GOLD_RUSH_SCREEN_ITEM_KEY)
                            .stacksTo(1)
            )
    );

    public static final ResourceKey<CreativeModeTab> TOOLS_AND_UTILITIES_TAB =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.withDefaultNamespace("tools_and_utilities")
            );

    public static final ResourceKey<CreativeModeTab> NATURAL_BLOCKS_TAB =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.withDefaultNamespace("natural_blocks")
            );

    /** Same key as {@code GroundworksItems} tab {@code pw_groundworks:main}. */
    public static final ResourceKey<CreativeModeTab> GROUNDWORKS_TAB =
            ResourceKey.create(
                    Registries.CREATIVE_MODE_TAB,
                    Identifier.fromNamespaceAndPath("pw_groundworks", "main")
            );

    public static final ResourceKey<Biome> GOLD_MINE_KEY =
            ResourceKey.create(Registries.BIOME, id("gold_mine"));

    public static final ResourceKey<MapCodec<? extends Feature>> PALEOCHANNEL_FEATURE_TYPE_KEY =
            ResourceKey.create(Registries.FEATURE_TYPE, id("paleochannel"));

    public static final GoldMinePaleochannelFeature PALEOCHANNEL_FEATURE =
            new GoldMinePaleochannelFeature();

    public static final MapCodec<GoldMinePaleochannelFeature> PALEOCHANNEL_CODEC =
            Registry.register(
                    BuiltInRegistries.FEATURE_TYPE,
                    PALEOCHANNEL_FEATURE_TYPE_KEY,
                    GoldMinePaleochannelFeature.CODEC
            );

    public record AuriferousGravelEntry(
            String name,
            int goldPercentage,
            ResourceKey<Block> blockKey,
            ResourceKey<Item> itemKey,
            AuriferousGravelBlock block,
            AuriferousGravelItem item
    ) {}

    public static final AuriferousGravelEntry AURIFEROUS_GRAVEL_10_ENTRY =
            registerGravel("auriferous_gravel_10", 10);
    public static final AuriferousGravelEntry AURIFEROUS_GRAVEL_25_ENTRY =
            registerGravel("auriferous_gravel_25", 25);
    public static final AuriferousGravelEntry AURIFEROUS_GRAVEL_50_ENTRY =
            registerGravel("auriferous_gravel_50", 50);
    public static final AuriferousGravelEntry AURIFEROUS_GRAVEL_80_ENTRY =
            registerGravel("auriferous_gravel_80", 80);

    public static final List<AuriferousGravelEntry> AURIFEROUS_GRAVELS = List.of(
            AURIFEROUS_GRAVEL_10_ENTRY,
            AURIFEROUS_GRAVEL_25_ENTRY,
            AURIFEROUS_GRAVEL_50_ENTRY,
            AURIFEROUS_GRAVEL_80_ENTRY
    );

    public static final AuriferousGravelBlock AURIFEROUS_GRAVEL_10 = AURIFEROUS_GRAVEL_10_ENTRY.block();
    public static final AuriferousGravelBlock AURIFEROUS_GRAVEL_25 = AURIFEROUS_GRAVEL_25_ENTRY.block();
    public static final AuriferousGravelBlock AURIFEROUS_GRAVEL_50 = AURIFEROUS_GRAVEL_50_ENTRY.block();
    public static final AuriferousGravelBlock AURIFEROUS_GRAVEL_80 = AURIFEROUS_GRAVEL_80_ENTRY.block();

    private static AuriferousGravelEntry registerGravel(String name, int percentage) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, id(name));
        AuriferousGravelBlock block = Registry.register(
                BuiltInRegistries.BLOCK,
                blockKey,
                new AuriferousGravelBlock(
                        percentage,
                        BlockBehaviour.Properties.ofFullCopy(Blocks.GRAVEL)
                                .setId(blockKey)
                )
        );

        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id(name));
        AuriferousGravelItem item = Registry.register(
                BuiltInRegistries.ITEM,
                itemKey,
                new AuriferousGravelItem(
                        block,
                        percentage,
                        new Item.Properties()
                                .setId(itemKey)
                                .useBlockDescriptionPrefix()
                )
        );

        return new AuriferousGravelEntry(name, percentage, blockKey, itemKey, block, item);
    }

    @Override
    public void onInitialize() {
        GoldRushConfig.load();

        LOGGER.info(
                "Initializing Peterwolf's Gold Rush Groundworks for MC 26.3. Gold yield: {} nuggets / 10 gravel blocks.",
                GoldRushConfig.getNuggetsPerTenBlocks()
        );

        CreativeModeTabEvents.modifyOutputEvent(GROUNDWORKS_TAB).register(output -> {
            output.accept(GOLD_RUSH_SCREEN_ITEM);
            for (AuriferousGravelEntry entry : AURIFEROUS_GRAVELS) {
                output.accept(entry.item());
            }
        });
        CreativeModeTabEvents.modifyOutputEvent(TOOLS_AND_UTILITIES_TAB).register(
                output -> output.accept(GOLD_RUSH_SCREEN_ITEM)
        );
        CreativeModeTabEvents.modifyOutputEvent(NATURAL_BLOCKS_TAB).register(output -> {
            for (AuriferousGravelEntry entry : AURIFEROUS_GRAVELS) {
                output.accept(entry.item());
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
                dispatcher.register(
                        Commands.literal("goldrush")
                                .then(Commands.literal("nuggets-per-10-blocks")
                                        .executes(context -> {
                                            int current = GoldRushConfig.getNuggetsPerTenBlocks();
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Gold Rush yield: " + current + " gold nuggets per 10 gravel blocks."
                                                    ),
                                                    false
                                            );
                                            return current;
                                        })
                                        .then(Commands.argument(
                                                        "amount",
                                                        IntegerArgumentType.integer(
                                                                GoldRushConfig.MIN_NUGGETS_PER_TEN_BLOCKS,
                                                                GoldRushConfig.MAX_NUGGETS_PER_TEN_BLOCKS
                                                        )
                                                )
                                                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                                                .executes(context -> {
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");
                                                    GoldRushConfig.setNuggetsPerTenBlocks(amount);
                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(
                                                                    "Gold Rush yield set to " + amount + " gold nuggets per 10 gravel blocks."
                                                            ),
                                                            true
                                                    );
                                                    return amount;
                                                }))
                                )
                )
        );
    }
}
