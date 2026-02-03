package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import net.minecraft.client.MinecraftClient; // НУЖЕН ЭТОТ ИМПОРТ
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.List;

public class TrailsModule extends Module {
    // Создаем переменную mc, чтобы код понимал, к чему мы обращаемся
    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public List<TrailPoint> points = new ArrayList<>();

    public float r = 1.0f;
    public float g = 0.0f;
    public float b = 1.0f;
    public float length = 20.0f;

    public TrailsModule() {
        super("Trails");
    }

    public void onUpdate() {
        // Теперь mc.player будет работать
        if (mc.player != null && isEnabled()) {
            points.add(new TrailPoint(mc.player.getPos(), System.currentTimeMillis()));

            while (points.size() > (int)length) {
                points.remove(0);
            }
        } else if (!isEnabled()) {
            points.clear(); // Очищаем хвост, если модуль выключен
        }
    }

    public record TrailPoint(Vec3d pos, long time) {}
}