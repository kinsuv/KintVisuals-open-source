package com.kintvisuals.client.render;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.hud.TargetHudModule;
import com.kintvisuals.client.module.render.TargetEspModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class TargetHudRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static float animatedHp = 0f;

    public static void renderHud(DrawContext context, float delta) {
        TargetHudModule mod = ModuleManager.get(TargetHudModule.class);
        if (mod == null || !mod.isEnabled() || mod.getTarget() == null || mod.hudScale <= 0.05f) return;

        LivingEntity target = mod.getTarget();

        // Позиция
        int width = 140;
        int height = 45;
        int x = mc.getWindow().getScaledWidth() / 2 - (width / 2);
        int y = mc.getWindow().getScaledHeight() / 2 + 50;

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        // Скейл от центра
        matrices.translate(x + (width / 2f), y + (height / 2f), 0);
        matrices.scale(mod.hudScale, mod.hudScale, 1);
        matrices.translate(-(x + (width / 2f)), -(y + (height / 2f)), 0);

        // 1. Фон (Скругленный)
        drawRoundedRect(matrices, x, y, width, height, 8, 0xAA000000);

        // 2. Отрисовка головы (Скругленный квадрат)
        if (target instanceof AbstractClientPlayerEntity player) {
            Identifier skin = player.getSkinTexture();
            float avatarSize = 35;
            float avatarX = x + 5;
            float avatarY = y + 5;

            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            // Рисуем лицо (скругленный квадрат)
            drawRoundedSquareTexture(matrices, skin, avatarX, avatarY, avatarSize, 8, 8, 8, 8, 64, 64, 6);
            // Рисуем шлем (скругленный квадрат)
            drawRoundedSquareTexture(matrices, skin, avatarX, avatarY, avatarSize, 40, 8, 8, 8, 64, 64, 6);
        }

        // 3. Имя
        context.drawText(mc.textRenderer, target.getName().getString(), x + 45, y + 8, -1, true);

        // 4. Полоска HP (Скругленная)
        float maxHp = target.getMaxHealth();
        float currentHp = target.getHealth();
        if (maxHp <= 0) maxHp = 1;

        animatedHp = MathHelper.lerp(0.1f, animatedHp, currentHp);
        float hpPercent = MathHelper.clamp(animatedHp / maxHp, 0, 1);
        float hpWidth = hpPercent * 85;

        // Фон полоски (скругленный)
        drawRoundedRect(matrices, x + 45, y + 22, 85, 6, 3, 0xFF333333);
        // Сама полоска (скругленная)
        if (hpWidth > 0) {
            drawRoundedRect(matrices, x + 45, y + 22, hpWidth, 6, 3, getHpColor(currentHp, maxHp));
        }

        // 5. Текст HP
        String hpString = String.format("%.1f HP", currentHp);
        context.drawText(mc.textRenderer, hpString, x + 45, y + 32, -1, true);

        matrices.pop();

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    /**
     * Рисует скругленный прямоугольник
     */
    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        float a = (color >> 24 & 255) / 255.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        // Центр
        buffer.vertex(matrix, x + width / 2, y + height / 2, 0).color(r, g, b, a).next();

        // Правый верхний
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i - 90);
            buffer.vertex(matrix, (float) (x + width - radius + Math.cos(angle) * radius), (float) (y + radius + Math.sin(angle) * radius), 0).color(r, g, b, a).next();
        }
        // Правый нижний
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i);
            buffer.vertex(matrix, (float) (x + width - radius + Math.cos(angle) * radius), (float) (y + height - radius + Math.sin(angle) * radius), 0).color(r, g, b, a).next();
        }
        // Левый нижний
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i + 90);
            buffer.vertex(matrix, (float) (x + radius + Math.cos(angle) * radius), (float) (y + height - radius + Math.sin(angle) * radius), 0).color(r, g, b, a).next();
        }
        // Левый верхний
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i + 180);
            buffer.vertex(matrix, (float) (x + radius + Math.cos(angle) * radius), (float) (y + radius + Math.sin(angle) * radius), 0).color(r, g, b, a).next();
        }

        // Замыкаем
        double endAngle = Math.toRadians(-90);
        buffer.vertex(matrix, (float) (x + width - radius + Math.cos(endAngle) * radius), (float) (y + radius + Math.sin(endAngle) * radius), 0).color(r, g, b, a).next();

        tessellator.draw();

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Рисует скругленный квадрат с текстурой
     */
    public static void drawRoundedSquareTexture(MatrixStack matrices, Identifier texture, float x, float y, float size, float u, float v, float uWidth, float vHeight, float textureWidth, float textureHeight, float radius) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_TEXTURE);

        float centerX = x + size / 2f;
        float centerY = y + size / 2f;

        float uMin = u / textureWidth;
        float vMin = v / textureHeight;
        float uSize = uWidth / textureWidth;
        float vSize = vHeight / textureHeight;

        // Центр
        buffer.vertex(matrix, centerX, centerY, 0).texture(uMin + uSize / 2, vMin + vSize / 2).next();

        // Правый верхний угол
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i - 90);
            float px = (float) (x + size - radius + Math.cos(angle) * radius);
            float py = (float) (y + radius + Math.sin(angle) * radius);
            float tx = uMin + uSize * ((px - x) / size);
            float ty = vMin + vSize * ((py - y) / size);
            buffer.vertex(matrix, px, py, 0).texture(tx, ty).next();
        }

        // Правый нижний угол
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i);
            float px = (float) (x + size - radius + Math.cos(angle) * radius);
            float py = (float) (y + size - radius + Math.sin(angle) * radius);
            float tx = uMin + uSize * ((px - x) / size);
            float ty = vMin + vSize * ((py - y) / size);
            buffer.vertex(matrix, px, py, 0).texture(tx, ty).next();
        }

        // Левый нижний угол
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i + 90);
            float px = (float) (x + radius + Math.cos(angle) * radius);
            float py = (float) (y + size - radius + Math.sin(angle) * radius);
            float tx = uMin + uSize * ((px - x) / size);
            float ty = vMin + vSize * ((py - y) / size);
            buffer.vertex(matrix, px, py, 0).texture(tx, ty).next();
        }

        // Левый верхний угол
        for (int i = 0; i <= 90; i += 10) {
            double angle = Math.toRadians(i + 180);
            float px = (float) (x + radius + Math.cos(angle) * radius);
            float py = (float) (y + radius + Math.sin(angle) * radius);
            float tx = uMin + uSize * ((px - x) / size);
            float ty = vMin + vSize * ((py - y) / size);
            buffer.vertex(matrix, px, py, 0).texture(tx, ty).next();
        }

        // Замыкаем
        double endAngle = Math.toRadians(-90);
        float px = (float) (x + size - radius + Math.cos(endAngle) * radius);
        float py = (float) (y + radius + Math.sin(endAngle) * radius);
        float tx = uMin + uSize * ((px - x) / size);
        float ty = vMin + vSize * ((py - y) / size);
        buffer.vertex(matrix, px, py, 0).texture(tx, ty).next();

        tessellator.draw();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    public static void renderESP(MatrixStack matrices, Camera camera, float delta) {
        TargetHudModule hud = ModuleManager.get(TargetHudModule.class);
        TargetEspModule esp = ModuleManager.get(TargetEspModule.class);

        if (hud == null || !hud.isEnabled() || esp == null || !esp.isEnabled()) return;
        LivingEntity target = hud.getTarget();
        if (target == null) return;

        Vec3d c = camera.getPos();
        double x = MathHelper.lerp(delta, target.prevX, target.getX()) - c.x;
        double y = MathHelper.lerp(delta, target.prevY, target.getY()) - c.y + target.getHeight() / 2.0;
        double z = MathHelper.lerp(delta, target.prevZ, target.getZ()) - c.z;

        matrices.push();
        matrices.translate(x, y, z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees((System.currentTimeMillis() / 5f) % 360));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(45));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(VertexFormat.DrawMode.LINES, VertexFormats.POSITION_COLOR);

        int clr = getHpColor(target.getHealth(), target.getMaxHealth());
        float r = (clr >> 16 & 255) / 255f;
        float g = (clr >> 8 & 255) / 255f;
        float bl = (clr & 255) / 255f;

        drawDiamond(b, matrices.peek().getPositionMatrix(), TargetEspModule.targetSize, r, g, bl, 0.8f);
        Tessellator.getInstance().draw();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        matrices.pop();
    }

    private static void drawDiamond(BufferBuilder b, Matrix4f m, float s, float r, float g, float bl, float a) {
        float[][] v = {{0,s,0},{0,-s,0},{s,0,0},{-s,0,0},{0,0,s},{0,0,-s}};
        int[][] e = {{0,2},{0,3},{0,4},{0,5},{1,2},{1,3},{1,4},{1,5},{2,4},{4,3},{3,5},{5,2}};
        for (int[] l : e) {
            b.vertex(m, v[l[0]][0], v[l[0]][1], v[l[0]][2]).color(r, g, bl, a).next();
            b.vertex(m, v[l[1]][0], v[l[1]][1], v[l[1]][2]).color(r, g, bl, a).next();
        }
    }

    private static int getHpColor(float hp, float max) {
        float ratio = hp / max;
        if (ratio > 0.5f) return 0xFF55FF55;
        if (ratio > 0.2f) return 0xFFFFFF55;
        return 0xFFFF5555;
    }
}