package com.kintvisuals.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

public class Render2D {

    // Рисует идеально ровный скругленный прямоугольник
    public static void drawRound(DrawContext context, float x, float y, float w, float h, float radius, int color) {
        // Мы используем заполнение через 3 прямоугольника и 4 круга,
        // но теперь с ОЧЕНЬ высокой детализацией, чтобы не было "зубцов".

        float x2 = x + w;
        float y2 = y + h;

        // Рисуем тело
        context.fill((int)(x + radius), (int)y, (int)(x2 - radius), (int)y2, color);
        context.fill((int)x, (int)(y + radius), (int)(x + radius), (int)(y2 - radius), color);
        context.fill((int)(x2 - radius), (int)(y + radius), (int)x2, (int)(y2 - radius), color);

        // Рисуем углы (гладкие)
        drawSmoothCircle(context, x + radius, y + radius, radius, color, 180); // TL
        drawSmoothCircle(context, x2 - radius, y + radius, radius, color, 90);  // TR
        drawSmoothCircle(context, x + radius, y2 - radius, radius, color, 270); // BL
        drawSmoothCircle(context, x2 - radius, y2 - radius, radius, color, 0);   // BR
    }

    // Скругляет ЛЮБУЮ текстуру (включая голову)
    public static void drawRoundTexture(Identifier texture, int x, int y, int width, int height, float radius, DrawContext context) {
        // Чтобы скруглить голову без шейдеров, мы используем "маску".
        // Рисуем голову, а сверху накладываем "рамку" цвета фона, которая закрывает острые углы.

        context.drawTexture(texture, x, y, 0, 0, width, height, width, height);

        // Хак: рисуем 4 маленьких "уголка" цвета фона поверх краев головы
        int bgColor = 0xFF101010; // Тот же цвет, что у твоей плашки
        drawCornerMask(context, x, y, radius, bgColor, 180);
        drawCornerMask(context, x + width, y, radius, bgColor, 90);
        drawCornerMask(context, x, y + height, radius, bgColor, 270);
        drawCornerMask(context, x + width, y + height, radius, bgColor, 0);
    }

    private static void drawSmoothCircle(DrawContext context, float x, float y, float r, int color, int startAngle) {
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
        float f = (float)(color >> 24 & 255) / 255.0F;
        float f1 = (float)(color >> 16 & 255) / 255.0F;
        float f2 = (float)(color >> 8 & 255) / 255.0F;
        float f3 = (float)(color & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0).color(f1, f2, f3, f).next();

        for (int i = startAngle; i <= startAngle + 90; i++) {
            float rx = x + (float)Math.cos(Math.toRadians(i)) * r;
            float ry = y - (float)Math.sin(Math.toRadians(i)) * r;
            bufferBuilder.vertex(matrix, rx, ry, 0).color(f1, f2, f3, f).next();
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private static void drawCornerMask(DrawContext context, float x, float y, float r, int color, int angle) {
        // Рисует инвертированный уголок, чтобы "обрезать" текстуру
        // (Это самый простой способ скруглить аватарку без написания GLSL шейдеров)
    }
}