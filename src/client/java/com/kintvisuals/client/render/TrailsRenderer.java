package com.kintvisuals.client.render;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.TrailsModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TrailsRenderer {

    public static void renderTrails(MatrixStack matrices, Camera camera) {
        TrailsModule mod = ModuleManager.get(TrailsModule.class);

        // 1. Сначала проверяем на null и включение, чтобы не вылететь
        if (mod == null || !mod.isEnabled() || mod.points.isEmpty()) return;

        // 2. Ограничиваем длину (теперь обращаемся к mod.points)
        while (mod.points.size() > (int) mod.length) {
            mod.points.remove(0);
        }

        if (mod.points.size() < 2) return;

        Vec3d cameraPos = camera.getPos();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableDepthTest();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Начинаем отрисовку
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int size = mod.points.size();

        // Переводим 0.0-1.0 в 0-255 для цвета
        int r = (int) (mod.r * 255);
        int g = (int) (mod.g * 255);
        int b = (int) (mod.b * 255);

        for (int i = 0; i < size; i++) {
            TrailsModule.TrailPoint p = mod.points.get(i);

            float fade = (float) i / size;
            int alpha = (int) (fade * 180);


            float x = (float) (p.pos().x - cameraPos.x);
            float y = (float) (p.pos().y - cameraPos.y) + 0.9f;
            float z = (float) (p.pos().z - cameraPos.z);

            // Рисуем две точки для создания "ленты"
            buffer.vertex(matrix, x, y - 0.25f, z).color(r, g, b, alpha).next(); // Нижний край ленты
            buffer.vertex(matrix, x, y + 0.25f, z).color(r, g, b, alpha).next(); // Верхний край ленты
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }
}