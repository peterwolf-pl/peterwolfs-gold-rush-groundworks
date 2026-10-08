package com.piotrek.goldrushgroundworks.logic;

import java.util.Arrays;

public final class GradationRouting {
    private GradationRouting() {}

    public static Batch plan(
            int[] unitsByMaterial,
            int selectedMaterialId,
            int perOutputLimit
    ) {
        int[] source = unitsByMaterial == null ? new int[0] : unitsByMaterial;
        int limit = Math.max(0, perOutputLimit);

        int selectedUnits = 0;
        if (selectedMaterialId > 0 && selectedMaterialId < source.length && limit > 0) {
            selectedUnits = Math.min(Math.max(0, source[selectedMaterialId]), limit);
        }

        int[] remainder = new int[source.length];
        if (limit <= 0 || source.length <= 1) {
            return new Batch(selectedUnits, remainder);
        }

        long remainderTotal = 0L;
        for (int id = 1; id < source.length; id++) {
            if (id == selectedMaterialId) {
                continue;
            }
            remainderTotal += Math.max(0, source[id]);
        }

        if (remainderTotal <= 0L) {
            return new Batch(selectedUnits, remainder);
        }

        if (remainderTotal <= limit) {
            for (int id = 1; id < source.length; id++) {
                if (id != selectedMaterialId) {
                    remainder[id] = Math.max(0, source[id]);
                }
            }
            return new Batch(selectedUnits, remainder);
        }

        long[] fractionalRemainders = new long[source.length];
        int assigned = 0;

        for (int id = 1; id < source.length; id++) {
            if (id == selectedMaterialId) {
                continue;
            }

            int available = Math.max(0, source[id]);
            if (available <= 0) {
                continue;
            }

            long scaled = (long) limit * available;
            int base = (int) (scaled / remainderTotal);
            remainder[id] = Math.min(base, available);
            fractionalRemainders[id] = scaled % remainderTotal;
            assigned += remainder[id];
        }

        while (assigned < limit) {
            int bestId = 0;
            long bestRemainder = Long.MIN_VALUE;

            for (int id = 1; id < source.length; id++) {
                if (id == selectedMaterialId || remainder[id] >= Math.max(0, source[id])) {
                    continue;
                }

                long candidate = fractionalRemainders[id];
                if (candidate > bestRemainder) {
                    bestRemainder = candidate;
                    bestId = id;
                }
            }

            if (bestId == 0) {
                break;
            }

            remainder[bestId]++;
            fractionalRemainders[bestId] = Long.MIN_VALUE;
            assigned++;
        }

        return new Batch(selectedUnits, remainder);
    }

    public record Batch(int selectedUnits, int[] remainderUnits) {
        public Batch {
            remainderUnits = remainderUnits == null
                    ? new int[0]
                    : Arrays.copyOf(remainderUnits, remainderUnits.length);
        }

        @Override
        public int[] remainderUnits() {
            return Arrays.copyOf(remainderUnits, remainderUnits.length);
        }
    }
}
