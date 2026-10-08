package com.piotrek.goldrushgroundworks.logic;

import com.piotrek.goldrushgroundworks.GoldRushGroundworksMod;
import com.piotrek.goldrushgroundworks.entity.GoldRushScreenEntity;
import com.piotrek.groundworks.api.material.GranularMaterialRegistry;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoldRushScreenOptionTest {

    @BeforeAll
    static void setUp() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        GranularMaterialRegistry.bootstrap();
    }

    @Test
    void defaultMaterialIsGold() {
        assertEquals(GoldRushScreenEntity.GOLD_MATERIAL_ID, GoldRushScreenEntity.DEFAULT_SELECTED_MATERIAL_ID);
        assertTrue(GoldRushScreenEntity.isGold(GoldRushScreenEntity.GOLD_MATERIAL_ID));
        assertFalse(GoldRushScreenEntity.isGold(1));
    }

    @Test
    void materialCycleIncludesGoldOption() {
        assertEquals(2, GoldRushScreenEntity.nextMaterialId(1)); // dirt -> sand
        assertEquals(3, GoldRushScreenEntity.nextMaterialId(2)); // sand -> gravel
        assertEquals(4, GoldRushScreenEntity.nextMaterialId(3)); // gravel -> cobblestone
        assertEquals(GoldRushScreenEntity.GOLD_MATERIAL_ID, GoldRushScreenEntity.nextMaterialId(4)); // cobblestone -> gold
        assertEquals(1, GoldRushScreenEntity.nextMaterialId(GoldRushScreenEntity.GOLD_MATERIAL_ID)); // gold -> dirt
    }

    @Test
    void materialComponentsUseProperTranslationKeys() {
        assertTranslationKey(1, "material.pw_gold_rush_groundworks.dirt");
        assertTranslationKey(2, "material.pw_gold_rush_groundworks.sand");
        assertTranslationKey(3, "material.pw_gold_rush_groundworks.gravel");
        assertTranslationKey(4, "material.pw_gold_rush_groundworks.cobblestone");
        assertTranslationKey(GoldRushScreenEntity.GOLD_MATERIAL_ID, "material.pw_gold_rush_groundworks.gold");
    }

    @Test
    void hopperCatchmentAreaExpandedByPointSixBlocks() {
        // isPointInHopperFunnel should accept points within 3.20 blocks horizontally
        net.minecraft.world.phys.Vec3 machinePos = new net.minecraft.world.phys.Vec3(100.0, 64.0, 100.0);
        float yRot = 0.0F;

        // At distance 3.00 (inside 3.20): should accept
        net.minecraft.world.phys.Vec3 inside = new net.minecraft.world.phys.Vec3(103.00, 66.0, 100.0);
        assertTrue(GoldRushScreenEntity.isPointInHopperFunnel(machinePos, yRot, inside));

        // At distance 3.35 (outside 3.20): should reject
        net.minecraft.world.phys.Vec3 outside = new net.minecraft.world.phys.Vec3(103.35, 66.0, 100.0);
        assertFalse(GoldRushScreenEntity.isPointInHopperFunnel(machinePos, yRot, outside));
    }

    private static void assertTranslationKey(int materialId, String expectedKey) {
        Component comp = GoldRushScreenEntity.getMaterialNameComponent(materialId);
        assertInstanceOf(TranslatableContents.class, comp.getContents());
        TranslatableContents translatable = (TranslatableContents) comp.getContents();
        assertEquals(expectedKey, translatable.getKey());
    }
}
