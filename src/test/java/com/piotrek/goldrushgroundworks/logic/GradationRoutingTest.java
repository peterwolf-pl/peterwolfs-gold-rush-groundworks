package com.piotrek.goldrushgroundworks.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GradationRoutingTest {

    @Test
    void selectedMaterialNeverLeaksIntoRemainderOutput() {
        int[] units = new int[] {0, 20, 60, 40, 12};

        GradationRouting.Batch batch = GradationRouting.plan(units, 3, 32);

        assertEquals(32, batch.selectedUnits());
        assertArrayEquals(new int[] {0, 7, 21, 0, 4}, batch.remainderUnits());
    }

    @Test
    void outputBudgetsAreIndependent() {
        int[] units = new int[] {0, 80, 80, 80};

        GradationRouting.Batch batch = GradationRouting.plan(units, 2, 32);

        assertEquals(32, batch.selectedUnits());
        assertArrayEquals(new int[] {0, 16, 0, 16}, batch.remainderUnits());
    }

    @Test
    void missingSelectedMaterialDoesNotStopRemainderStream() {
        int[] units = new int[] {0, 10, 0, 18};

        GradationRouting.Batch batch = GradationRouting.plan(units, 2, 32);

        assertEquals(0, batch.selectedUnits());
        assertArrayEquals(new int[] {0, 10, 0, 18}, batch.remainderUnits());
    }

    @Test
    void continuousLowIdMaterialCannotStarveHigherIdMaterial() {
        int[] units = new int[] {0, 1000, 1000, 1000, 1000};

        GradationRouting.Batch batch = GradationRouting.plan(units, 4, 32);

        int[] remainder = batch.remainderUnits();
        assertEquals(32, remainder[1] + remainder[2] + remainder[3]);
        assertTrue(remainder[1] > 0);
        assertTrue(remainder[2] > 0);
        assertTrue(remainder[3] > 0);
        assertEquals(0, remainder[4]);
    }
}
