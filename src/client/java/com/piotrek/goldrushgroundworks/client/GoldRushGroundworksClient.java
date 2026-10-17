package com.piotrek.goldrushgroundworks.client;

import com.piotrek.goldrushgroundworks.GoldRushGroundworksMod;
import com.piotrek.goldrushgroundworks.client.model.GoldRushScreenModel;
import com.piotrek.goldrushgroundworks.client.render.GoldRushScreenRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

public class GoldRushGroundworksClient implements ClientModInitializer {

    public static final ModelLayerLocation GOLD_RUSH_SCREEN_LAYER =
            new ModelLayerLocation(
                    GoldRushGroundworksMod.id("gold_rush_screen"),
                    "main"
            );

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(
                GOLD_RUSH_SCREEN_LAYER,
                GoldRushScreenModel::createBodyLayer
        );
        EntityRendererRegistry.register(
                GoldRushGroundworksMod.GOLD_RUSH_SCREEN,
                GoldRushScreenRenderer::new
        );
    }
}
