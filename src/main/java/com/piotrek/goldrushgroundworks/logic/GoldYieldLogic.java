package com.piotrek.goldrushgroundworks.logic;

public final class GoldYieldLogic {
    public static final int UNITS_PER_BLOCK = 512;
    public static final int REFERENCE_BLOCKS = 10;
    public static final long REFERENCE_UNITS = (long) UNITS_PER_BLOCK * REFERENCE_BLOCKS;

    private GoldYieldLogic() {}

    public static YieldResult accrue(
            long weightedCarry,
            int processedGravelUnits,
            int nuggetsPerTenBlocks
    ) {
        long carry = Math.max(0L, weightedCarry) % REFERENCE_UNITS;
        int units = Math.max(0, processedGravelUnits);
        int rate = Math.max(0, nuggetsPerTenBlocks);

        long total = carry + (long) units * rate;
        int nuggets = (int) (total / REFERENCE_UNITS);
        long remainder = total % REFERENCE_UNITS;

        return new YieldResult(nuggets, remainder);
    }

    public record YieldResult(int nuggets, long weightedCarry) {}
}
