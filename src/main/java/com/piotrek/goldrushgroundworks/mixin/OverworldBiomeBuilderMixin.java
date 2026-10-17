package com.piotrek.goldrushgroundworks.mixin;

import com.mojang.datafixers.util.Pair;
import com.piotrek.goldrushgroundworks.GoldRushGroundworksMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(OverworldBiomeBuilder.class)
public class OverworldBiomeBuilderMixin {
    @Inject(method = "addBiomes", at = @At("TAIL"))
    private void addGoldMineBiome(
            Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer,
            CallbackInfo ci
    ) {
        // Gold Mine placer basin: flat inland valley, high erosion (very flat terrain), dry temperate climate
        consumer.accept(Pair.of(
                Climate.parameters(
                        Climate.Parameter.span(-0.10F, 0.25F), // temperature
                        Climate.Parameter.span(-0.25F, 0.15F), // humidity
                        Climate.Parameter.span(0.15F, 0.45F),  // continentalness (flat inland valley)
                        Climate.Parameter.span(0.55F, 0.85F),  // erosion (very flat plains / alluvial terrace)
                        Climate.Parameter.point(0.0F),         // depth (surface)
                        Climate.Parameter.span(-0.10F, 0.15F), // weirdness (flat valley floor)
                        0.0F
                ),
                GoldRushGroundworksMod.GOLD_MINE_KEY
        ));
    }
}
