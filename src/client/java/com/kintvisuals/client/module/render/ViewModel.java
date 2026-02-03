package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import com.kintvisuals.client.module.Setting;

public class ViewModel extends Module {

    // --- Настройки для ПРАВОЙ руки ---
    public Setting rightX = new Setting("Right X", -2.0, 2.0, 0.0);
    public Setting rightY = new Setting("Right Y", -2.0, 2.0, 0.0);
    public Setting rightZ = new Setting("Right Z", -2.0, 2.0, 0.0);

    public Setting rightRotX = new Setting("Right Rot X", -180.0, 180.0, 0.0);
    public Setting rightRotY = new Setting("Right Rot Y", -180.0, 180.0, 0.0);
    public Setting rightRotZ = new Setting("Right Rot Z", -180.0, 180.0, 0.0);

    public Setting rightScale = new Setting("Right Scale", 0.1, 2.0, 1.0);

    // --- Настройки для ЛЕВОЙ руки ---
    public Setting leftX = new Setting("Left X", -2.0, 2.0, 0.0);
    public Setting leftY = new Setting("Left Y", -2.0, 2.0, 0.0);
    public Setting leftZ = new Setting("Left Z", -2.0, 2.0, 0.0);

    public Setting leftRotX = new Setting("Left Rot X", -180.0, 180.0, 0.0);
    public Setting leftRotY = new Setting("Left Rot Y", -180.0, 180.0, 0.0);
    public Setting leftRotZ = new Setting("Left Rot Z", -180.0, 180.0, 0.0);

    public Setting leftScale = new Setting("Left Scale", 0.1, 2.0, 1.0);
    public int selectedTab = 0; // 0 - Правая рука, 1 - Левая рука

    public ViewModel() {
        super("ViewModel");
    }
}