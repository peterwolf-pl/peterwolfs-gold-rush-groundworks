package com.piotrek.goldrushgroundworks.config;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class GoldRushConfig {
    public static final int DEFAULT_NUGGETS_PER_TEN_BLOCKS = 2;
    public static final int MIN_NUGGETS_PER_TEN_BLOCKS = 0;
    public static final int MAX_NUGGETS_PER_TEN_BLOCKS = 1024;

    private static final String KEY = "nuggetsPer10Blocks";
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("pw_gold_rush_groundworks.properties");

    private static volatile int nuggetsPerTenBlocks = DEFAULT_NUGGETS_PER_TEN_BLOCKS;

    private GoldRushConfig() {}

    public static synchronized void load() {
        Properties properties = new Properties();
        if (Files.isRegularFile(CONFIG_PATH)) {
            try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
                properties.load(input);
            } catch (IOException ignored) {
                nuggetsPerTenBlocks = DEFAULT_NUGGETS_PER_TEN_BLOCKS;
            }
        }

        nuggetsPerTenBlocks = clamp(parseInt(
                properties.getProperty(KEY),
                DEFAULT_NUGGETS_PER_TEN_BLOCKS
        ));

        save();
    }

    public static int getNuggetsPerTenBlocks() {
        return nuggetsPerTenBlocks;
    }

    public static synchronized void setNuggetsPerTenBlocks(int value) {
        nuggetsPerTenBlocks = clamp(value);
        save();
    }

    private static void save() {
        Properties properties = new Properties();
        properties.setProperty(KEY, Integer.toString(nuggetsPerTenBlocks));

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream output = Files.newOutputStream(CONFIG_PATH)) {
                properties.store(output, "Peterwolf's Gold Rush Groundworks");
            }
        } catch (IOException ignored) {
            // Runtime setting still works even if the config file cannot be written.
        }
    }

    private static int parseInt(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static int clamp(int value) {
        return Math.max(
                MIN_NUGGETS_PER_TEN_BLOCKS,
                Math.min(MAX_NUGGETS_PER_TEN_BLOCKS, value)
        );
    }
}
