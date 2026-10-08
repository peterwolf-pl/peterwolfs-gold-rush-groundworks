package com.piotrek.goldrushgroundworks.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GoldYieldLogicTest {

    @Test
    void defaultRateProducesTwoNuggetsPerTenBlocks() {
        GoldYieldLogic.YieldResult result = GoldYieldLogic.accrue(
                0L,
                10 * GoldYieldLogic.UNITS_PER_BLOCK,
                2
        );

        assertEquals(2, result.nuggets());
        assertEquals(0L, result.weightedCarry());
    }

    @Test
    void defaultRateProducesOneNuggetAfterFiveBlocks() {
        GoldYieldLogic.YieldResult result = GoldYieldLogic.accrue(
                0L,
                5 * GoldYieldLogic.UNITS_PER_BLOCK,
                2
        );

        assertEquals(1, result.nuggets());
        assertEquals(0L, result.weightedCarry());
    }

    @Test
    void partialProgressCarriesAcrossTransfers() {
        GoldYieldLogic.YieldResult first = GoldYieldLogic.accrue(
                0L,
                2 * GoldYieldLogic.UNITS_PER_BLOCK,
                2
        );
        GoldYieldLogic.YieldResult second = GoldYieldLogic.accrue(
                first.weightedCarry(),
                3 * GoldYieldLogic.UNITS_PER_BLOCK,
                2
        );

        assertEquals(0, first.nuggets());
        assertEquals(1, second.nuggets());
        assertEquals(0L, second.weightedCarry());
    }

    @Test
    void zeroRateDisablesNewGoldWithoutCorruptingCarry() {
        GoldYieldLogic.YieldResult beforeDisable = GoldYieldLogic.accrue(
                0L,
                GoldYieldLogic.UNITS_PER_BLOCK,
                2
        );
        GoldYieldLogic.YieldResult disabled = GoldYieldLogic.accrue(
                beforeDisable.weightedCarry(),
                20 * GoldYieldLogic.UNITS_PER_BLOCK,
                0
        );

        assertEquals(0, disabled.nuggets());
        assertEquals(beforeDisable.weightedCarry(), disabled.weightedCarry());
    }
}
