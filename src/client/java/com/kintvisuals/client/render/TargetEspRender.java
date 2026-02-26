package com.kintvisuals.client.render;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.TargetEspModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.Color;

/**
 * Fixed TargetEspRender:
 * - world-space rendering only (translate by worldPos - cameraPos)
 * - single TargetESP2 object (crossed quads inside one matrix, NOT duplicated instances)
 * - Ghosts are 3D crosses (two perpendicular quads) placed in XZ orbit
 * - Breath deforms orbit into "+" (not ellipse) and controls tilt \ / by quadrant
 * - Uses emissive render layer + fullbright light to avoid darkening
 * Note: if you still observe shading artifacts, consider adding a custom unlit RenderLayer.
 */
public class TargetEspRender {

    private static final Identifier TARGET_TEX = new Identifier("kintvisuals", "textures/target-esp2.png");
    private static final Identifier GLOW_TEX = new Identifier("kintvisuals", "textures/glow.png");

    public static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        TargetEspModule mod = ModuleManager.get(TargetEspModule.class);
        if (mod == null || !mod.isEnabled()) return;

        LivingEntity target = mod.getTarget();
        if (target == null || !mc.player.canSee(target)) return;

        if (mod.renderMode == 0) {
            renderTargetEsp2(ctx, target, mod);
        } else {
            renderGhosts(ctx, target, mod);
        }
    }

    // =========================
    // TARGET-ESP2 (single 3D object)
    // =========================
    private static void renderTargetEsp2(WorldRenderContext ctx, LivingEntity target, TargetEspModule mod) {
        MinecraftClient mc = MinecraftClient.getInstance();
        MatrixStack ms = ctx.matrixStack();
        VertexConsumerProvider vcp = ctx.consumers();
        if (vcp == null) return;

        Vec3d camPos = ctx.camera().getPos();
        float td = ctx.tickDelta();

        ms.push();

        // Interpolate world position and translate to camera-relative coordinates
        double tX = MathHelper.lerp(td, target.prevX, target.getX());
        double tY = MathHelper.lerp(td, target.prevY, target.getY());
        double tZ = MathHelper.lerp(td, target.prevZ, target.getZ());
        ms.translate(tX - camPos.x, (tY - camPos.y) + target.getHeight() * 0.5, tZ - camPos.z);

        // Rotate horizontally to face player (only Y axis)
        double dx = mc.player.getX() - tX;
        double dz = mc.player.getZ() - tZ;
        float yawToPlayer = (float) (Math.atan2(dz, dx) * 57.29577951308232) - 90f;
        mod.smoothYaw = MathHelper.lerpAngleDegrees(0.15f, mod.smoothYaw, yawToPlayer);
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mod.smoothYaw));

        // Spin on Z if requested
        float time = (mc.player.age + td);
        float spinAngle = time * mod.squareSpinSpeed * 5.0f;
        ms.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(spinAngle));

        float size = mod.squareSize * 0.01f;

        Color top, bottom;
        if (mod.squareRainbow) {
            float hue = (time * mod.squareRainbowSpeed * 0.01f) % 1f;
            top = Color.getHSBColor(hue, 0.85f, 1f);
            bottom = Color.getHSBColor((hue + 0.25f) % 1f, 0.85f, 1f);
        } else {
            top = new Color(clamp(mod.squareColorR), clamp(mod.squareColorG), clamp(mod.squareColorB));
            bottom = top;
        }

        // Render: disable depth test if we want to see through walls; keep fullbright/emissive to avoid shading
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Draw ONE object composed of two perpendicular quads as a single logical object (gives depth)
        // They are drawn inside the same matrix push, so not a "duplicate" instance.
        drawCrossedQuads(ms, vcp, TARGET_TEX, top, bottom, size);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        ms.pop();
    }

    // =========================
    // GHOST MODE (3D ghost crosses orbiting in XZ plane)
    // =========================
    // =========================
// GHOST MODE — Только вращающиеся призраки (правильная версия)
// Использует GLOW_TEX + drawCrossedParticle (объёмный 3D-крест)
// =========================
    // =========================
// GHOST MODE — Только вращающиеся призраки (финальная версия)
// Теперь большие, вытянутые, яркие зелёные силуэты с glow
// =========================
    // =========================
    // GHOST MODE — Полностью переписан (чистый силуэт без полосок при движении)
    // =========================
    private static void renderGhosts(WorldRenderContext ctx, LivingEntity target, TargetEspModule mod) {
        MinecraftClient mc = MinecraftClient.getInstance();
        MatrixStack ms = ctx.matrixStack();
        VertexConsumerProvider vcp = ctx.consumers();
        if (vcp == null || mod.ghostCount < 1) return;

        if (mc.player.distanceTo(target) > 100) return;

        Vec3d camPos = ctx.camera().getPos();
        float td = ctx.tickDelta();
        float time = mc.player.age + td;

        double tX = MathHelper.lerp(td, target.prevX, target.getX());
        double tY = MathHelper.lerp(td, target.prevY, target.getY());
        double tZ = MathHelper.lerp(td, target.prevZ, target.getZ());

        ms.push();
        ms.translate(tX - camPos.x, tY + target.getHeight() * 0.5 - camPos.y, tZ - camPos.z);

        mod.ghostTime += td * mod.ghostSpeed * 0.031f;

        RenderSystem.disableDepthTest(); // X-Ray
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float pulse = (float) (Math.sin(time * 3.7) * 0.22 + 0.78);

        for (int i = 0; i < mod.ghostCount; i++) {
            float angle = mod.ghostTime * (1.35f + i * 0.12f) + i * (360f / mod.ghostCount);
            double rad = Math.toRadians(angle);

            double x = Math.cos(rad) * mod.ghostRadius;
            double z = Math.sin(rad) * mod.ghostRadius;
            double y = Math.sin(mod.ghostTime * 4.5 + i) * 0.18;

            Color color = mod.ghostRainbow
                    ? Color.getHSBColor((mod.ghostTime * 0.68f + i * 0.17f) % 1f, 0.95f, 1f)
                    : new Color((int) mod.ghostColorR, (int) mod.ghostColorG, (int) mod.ghostColorB);

            float alpha = mod.ghostAlpha * pulse;
            float scale = mod.ghostScale;

            ms.push();
            ms.translate(x, y, z);

            // БЕЗ ПОВОРОТА! Квады рендерятся в мировом пространстве (крест)
            drawGhostSilhouette(ms, vcp, color, alpha, scale);

            ms.pop();
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        ms.pop();
    }


    // =========================
    // DRAW HELPERS
    // =========================

    // Draw two perpendicular quads inside the same matrix push (gives volume)
    private static void drawCrossedQuads(MatrixStack ms, VertexConsumerProvider vcp, Identifier texture,
                                         Color top, Color bottom, float size) {
        RenderLayer layer = RenderLayer.getEntityTranslucentEmissive(texture);
        VertexConsumer vc = vcp.getBuffer(layer);
        Matrix4f m = ms.peek().getPositionMatrix();
        float h = size / 2f;
        int light = 15728880;

        vc.vertex(m, -h, -h, 0).color(top.getRed(), top.getGreen(), top.getBlue(), 255).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();
        vc.vertex(m, h, -h, 0).color(top.getRed(), top.getGreen(), top.getBlue(), 255).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();
        vc.vertex(m, h, h, 0).color(bottom.getRed(), bottom.getGreen(), bottom.getBlue(), 255).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();
        vc.vertex(m, -h, h, 0).color(bottom.getRed(), bottom.getGreen(), bottom.getBlue(), 255).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();

        ms.push();
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f));
        Matrix4f m2 = ms.peek().getPositionMatrix();
        vc.vertex(m2, -h, -h, 0).color(top.getRed(), top.getGreen(), top.getBlue(), 255).texture(0f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();
        vc.vertex(m2, h, -h, 0).color(top.getRed(), top.getGreen(), top.getBlue(), 255).texture(1f, 1f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();
        vc.vertex(m2, h, h, 0).color(bottom.getRed(), bottom.getGreen(), bottom.getBlue(), 255).texture(1f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();
        vc.vertex(m2, -h, h, 0).color(bottom.getRed(), bottom.getGreen(), bottom.getBlue(), 255).texture(0f, 0f).overlay(OverlayTexture.DEFAULT_UV).light(light).normal(0,0,1).next();
        ms.pop();
    }

    // Новый настоящий призрак: тело + хвост (вытянутый силуэт с glow)
    // Новый настоящий призрак: тело + хвост (вытянутый силуэт с glow)
// ИСПРАВЛЕНО: всё через QUADS + normal(0,0,1)
    private static void drawGhostSilhouette(MatrixStack ms, VertexConsumerProvider vcp, Color color, float alpha, float scale) {
        RenderLayer layer = RenderLayer.getEntityTranslucentEmissive(GLOW_TEX);
        VertexConsumer vc = vcp.getBuffer(layer);
        Matrix4f mat = ms.peek().getPositionMatrix();
        int light = 15728880;
        int a = MathHelper.clamp((int) (alpha * 255f), 0, 255);

        float w = 0.25f * scale;      // ширина (узкая точка)
        float h = 0.85f * scale;      // высота (компактный размер)

        // QUAD 1 (вперёд-назад по Z)
        vc.vertex(mat, -w, h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), a)
                .texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        vc.vertex(mat, w, h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), a)
                .texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        vc.vertex(mat, w, -h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 0.7f))
                .texture(1f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        vc.vertex(mat, -w, -h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 0.7f))
                .texture(0f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        // QUAD 2 (повёрнут на 90° для крестовины - даёт объём)
        ms.push();
        ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90f));
        Matrix4f mat2 = ms.peek().getPositionMatrix();

        vc.vertex(mat2, -w, h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), a)
                .texture(0f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        vc.vertex(mat2, w, h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), a)
                .texture(1f, 1f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        vc.vertex(mat2, w, -h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 0.7f))
                .texture(1f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        vc.vertex(mat2, -w, -h, 0)
                .color(color.getRed(), color.getGreen(), color.getBlue(), (int)(a * 0.7f))
                .texture(0f, 0f)
                .overlay(OverlayTexture.DEFAULT_UV)
                .light(light)
                .normal(0, 0, 1)
                .next();

        ms.pop();
    }
    private static int clamp(float v) {
        return MathHelper.clamp((int) v, 0, 255);
    }
}
