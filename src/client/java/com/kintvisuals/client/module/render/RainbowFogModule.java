package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import java.awt.Color;

public class RainbowFogModule extends Module {
    public float r = 1.0f, g = 1.0f, b = 1.0f;
    public boolean rainbow = true;
    public float speed = 1.0f;

    // Эти значения теперь скрыты от пользователя, но работают в фоне
    public final float finalFogStart = 0.5f;
    public final float finalFogEnd = 4.0f;

    public RainbowFogModule() {
        super("CustomFog");
    }

    public float[] getFogColor() {
        if (rainbow) {
            float hue = (System.currentTimeMillis() % (int)(10000 / speed)) / (10000 / speed);
            int rgb = Color.HSBtoRGB(hue, 0.7f, 0.9f);
            return new float[] {
                    ((rgb >> 16) & 0xFF) / 255f,
                    ((rgb >> 8) & 0xFF) / 255f,
                    (rgb & 0xFF) / 255f
            };
        }
        return new float[] {r, g, b};
    }
}