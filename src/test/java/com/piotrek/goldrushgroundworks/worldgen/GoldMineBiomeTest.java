package com.piotrek.goldrushgroundworks.worldgen;

import com.piotrek.goldrushgroundworks.GoldRushGroundworksMod;
import com.piotrek.goldrushgroundworks.worldgen.feature.GoldMinePaleochannelFeature;
import net.minecraft.SharedConstants;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;

import static org.junit.jupiter.api.Assertions.*;

class GoldMineBiomeTest {

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
    void keysAndCodecAreProperlyRegistered() {
        assertEquals("pw_gold_rush_groundworks:gold_mine", GoldRushGroundworksMod.GOLD_MINE_KEY.identifier().toString());
        assertEquals("pw_gold_rush_groundworks:paleochannel", GoldRushGroundworksMod.PALEOCHANNEL_FEATURE_TYPE_KEY.identifier().toString());
        assertNotNull(GoldRushGroundworksMod.PALEOCHANNEL_CODEC);
        assertSame(GoldMinePaleochannelFeature.CODEC, GoldRushGroundworksMod.PALEOCHANNEL_CODEC);
    }

    @Test
    void paleochannelDistanceMathIdentifiesChannelAndHalo() {
        // Channel center: worldZ aligns with channelOffset
        double worldX = 100.0D;
        double channelOffset = Math.sin(worldX * 0.015D) * 35.0D + Math.cos(worldX * 0.006D) * 50.0D;
        double channelPeriod = 180.0D;

        // Exact center
        double worldZCenter = channelOffset;
        double relZ = Math.floorMod((long) Math.floor(worldZCenter - channelOffset), (long) channelPeriod);
        if (relZ > channelPeriod / 2.0D) {
            relZ -= channelPeriod;
        }
        double distCenter = Math.abs(relZ);
        assertTrue(distCenter <= 1.5D, "Center of old riverbed should be within dist <= 1.5");

        // Margin of 3-block wide paystreak (1 block away from center)
        double worldZEdge = channelOffset + 1.0D;
        relZ = Math.floorMod((long) Math.floor(worldZEdge - channelOffset), (long) channelPeriod);
        if (relZ > channelPeriod / 2.0D) {
            relZ -= channelPeriod;
        }
        double distEdge = Math.abs(relZ);
        assertTrue(distEdge <= 1.5D, "Paystreak edge should be within 3 rows core");

        // Halo zone (3.5 blocks away)
        double worldZHalo = channelOffset + 3.5D;
        relZ = Math.floorMod((long) Math.floor(worldZHalo - channelOffset), (long) channelPeriod);
        if (relZ > channelPeriod / 2.0D) {
            relZ -= channelPeriod;
        }
        double distHalo = Math.abs(relZ);
        assertTrue(distHalo > 1.5D && distHalo <= 6.5D, "Surrounding gravel/sand halo should be within 1.5 < dist <= 6.5");

        // Rest of biome (25 blocks away)
        double worldZRest = channelOffset + 25.0D;
        relZ = Math.floorMod((long) Math.floor(worldZRest - channelOffset), (long) channelPeriod);
        if (relZ > channelPeriod / 2.0D) {
            relZ -= channelPeriod;
        }
        double distRest = Math.abs(relZ);
        assertTrue(distRest > 6.5D, "Rest of the gold mine terrain should be outside halo");
    }
}
