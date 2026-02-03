package com.kintvisuals.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("kintvisuals.json");

    /* ====== НАСТРОЙКИ ====== */

    // Modules enabled by default
    public boolean espEnabled = true;
    public boolean hudEnabled = true;
    public boolean targetHudEnabled = true;

    // Colors (ARGB)
    public int espColor = 0xFFFFFFFF;      // white
    public int hudTextColor = 0xFFFFFFFF;  // white
    public int targetHudColor = 0xFF5555FF; // blue-ish

    /* ====================== */

    private static ModConfig INSTANCE;

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        try {
            if (Files.exists(CONFIG_PATH)) {
                INSTANCE = GSON.fromJson(Files.readString(CONFIG_PATH), ModConfig.class);
            } else {
                INSTANCE = new ModConfig();
                save();
            }
        } catch (Exception e) {
            System.err.println("[KintVisuals] Failed to load config, using defaults");
            e.printStackTrace();
            INSTANCE = new ModConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(INSTANCE));
        } catch (IOException e) {
            System.err.println("[KintVisuals] Failed to save config");
            e.printStackTrace();
        }
    }
}
