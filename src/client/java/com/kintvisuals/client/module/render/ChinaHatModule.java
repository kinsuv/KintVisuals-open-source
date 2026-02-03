package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;

public class ChinaHatModule extends Module {
    public float r = 0.0f;
    public float g = 0.6f;
    public float b = 1.0f;
    public float radius = 0.6f;
    public float height = 0.3f;
    public float alpha = 0.7f; // Новая переменная (прозрачность)

    public ChinaHatModule() {
        super("ChinaHat");
    }
}