package com.kintvisuals.client.render;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.hud.TargetHudModule;
import com.kintvisuals.client.module.render.TargetEspModule;
import com.kintvisuals.client.util.RenderUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import java.awt.Color;

public class TargetEspRender {

    public static void render(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TargetEspModule espMod = ModuleManager.get(TargetEspModule.class);
        TargetHudModule hudMod = ModuleManager.get(TargetHudModule.class);

        // Проверяем включены ли модули и есть ли игрок
        if (mc.player == null || espMod == null || !espMod.isEnabled() || hudMod == null) return;

        // Синхронизация: берем "липкую" цель из TargetHUD
        LivingEntity target = hudMod.getTarget();
        float scale = hudMod.hudScale;

        // Если цели нет или она исчезает (scale близок к 0), не рендерим
        if (target == null || scale <= 0.01f) return;

        MatrixStack matrices = context.matrixStack();
        Vec3d camPos = mc.gameRenderer.getCamera().getPos();

        // Подготовка GL состояний (X-Ray, Blend) через наш Utils
        RenderUtils.setupRender();
        matrices.push();

        // Интерполяция позиции цели для плавности
        double x = MathHelper.lerp(context.tickDelta(), target.prevX, target.getX()) - camPos.x;
        double y = MathHelper.lerp(context.tickDelta(), target.prevY, target.getY()) - camPos.y;
        double z = MathHelper.lerp(context.tickDelta(), target.prevZ, target.getZ()) - camPos.z;

        // Центрируем на уровне груди
        matrices.translate(x, y + target.getHeight() * 0.5, z);

        // Слежение только по оси Y (Yaw), чтобы ромб стоял вертикально
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));

        // Плавное появление (масштабирование самого ромба)
        matrices.scale(scale, scale, scale);

        // Вращение колеса
        float time = (System.currentTimeMillis() % 2000L) / 2000f;
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(time * 360f));

        Color rainbow = Color.getHSBColor(time, 0.7f, 1f);
        float size = TargetEspModule.targetSize;
        float th = 0.045f; // Толщина сосиски
        float gap = 0.15f; // Расстояние между ними

        // Отрисовка 4 сосисок с закруглениями (через цилиндры и сферы в RenderUtils)
        int r = rainbow.getRed(), g = rainbow.getGreen(), b = rainbow.getBlue();

        RenderUtils.draw3DLine(matrices, size, 0, 0, 0, size, 0, th, gap, r, g, b, 255);
        RenderUtils.draw3DLine(matrices, 0, size, 0, -size, 0, 0, th, gap, r, g, b, 255);
        RenderUtils.draw3DLine(matrices, -size, 0, 0, 0, -size, 0, th, gap, r, g, b, 255);
        RenderUtils.draw3DLine(matrices, 0, -size, 0, size, 0, 0, th, gap, r, g, b, 255);

        matrices.pop();
        RenderUtils.endRender();
    }
}