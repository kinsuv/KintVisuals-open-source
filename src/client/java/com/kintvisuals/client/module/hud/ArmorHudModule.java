package com.kintvisuals.client.module.hud;

import com.kintvisuals.client.module.Module;
import com.kintvisuals.client.module.Setting;

public class ArmorHudModule extends Module {

    public Setting showBar = new Setting("Show Bar", 0.0, 1.0, 1.0); // Показывать полоску
    public Setting showIcons = new Setting("Show Icons", 0.0, 1.0, 1.0);
    public Setting hudScale = new Setting("HUD Scale", 0.5, 2.0, 1.0);
    public Setting posX = new Setting("Position X", 0.0, 1000.0, 10.0);
    public Setting posY = new Setting("Position Y", 0.0, 1000.0, 10.0);

    public ArmorHudModule() {
        super("ArmorHud");
    }

    public int getDurabilityColor(float percent) {
        if (percent > 0.7f) return 0xFF55FF55; // Зеленый
        if (percent > 0.35f) return 0xFFFFFF55; // Желтый
        return 0xFFFF5555; // Красный
    }
}