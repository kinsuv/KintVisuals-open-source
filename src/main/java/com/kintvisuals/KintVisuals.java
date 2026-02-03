package com.kintvisuals;

import com.kintvisuals.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KintVisuals implements ModInitializer {

    public static final String MOD_ID = "kintvisuals";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Загрузка конфига
        ModConfig.load();

        LOGGER.info("[KintVisuals] Mod initialized");
    }
}
