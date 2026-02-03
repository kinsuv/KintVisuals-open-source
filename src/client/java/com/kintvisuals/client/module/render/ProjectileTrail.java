package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import com.kintvisuals.client.module.Setting;
import java.awt.Color;

public class ProjectileTrail extends Module {

    public Setting red = new Setting("Red", 0.0, 255.0, 255.0);
    public Setting green = new Setting("Green", 0.0, 255.0, 0.0);
    public Setting blue = new Setting("Blue", 0.0, 255.0, 255.0);
    public Setting thickness = new Setting("Thickness", 0.0, 500.0, 255.0); // Теперь это прозрачность
    public Setting rainbow = new Setting("Rainbow", 0.0, 1.0, 1.0);
    public Setting trailLength = new Setting("Trail Length", 10.0, 50.0, 30.0);
    public Setting lineWidth = new Setting("Line Width", 1.0, 15.0, 7.5); // Это диаметр
    public Setting rainbowSpeed = new Setting("Rainbow Speed", 1.0, 20.0, 10.0);

    public ProjectileTrail() {
        super("ProjectileTrail");
    }

    public float[] getRGB(float agePercent) {
        if (rainbow.value > 0.5) {
            float time = System.currentTimeMillis() / 1000f;
            float hue = (time * (float)rainbowSpeed.value / 10f) % 1.0f;
            hue = (hue + agePercent * 0.3f) % 1.0f;
            int rgb = Color.HSBtoRGB(hue, 0.8f, 1.0f);
            return new float[] {
                    ((rgb >> 16) & 0xFF) / 255f,
                    ((rgb >> 8) & 0xFF) / 255f,
                    (rgb & 0xFF) / 255f
            };
        }
        return new float[] {
                (float)red.value / 255f,
                (float)green.value / 255f,
                (float)blue.value / 255f
        };
    }

    public float getAlpha(float agePercent) {
        // thickness теперь управляет прозрачностью
        // Старые точки более прозрачные
        return ((float)thickness.value / 255f) * (1.0f - agePercent * 0.7f);
    }

    public int getPreviewColor() {
        int r = (int)red.value;
        int g = (int)green.value;
        int b = (int)blue.value;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}