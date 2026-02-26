package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TargetEspModule extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();

    // Общие настройки
    public float targetSize = 0.5f;
    public int renderMode = 0; // 0 = Target-Esp2, 1 = Ghosts

    // Параметры Target-Esp2 (квадрат)
    public boolean squareRainbow = true;
    public float squareRainbowSpeed = 1.0f; // Скорость смены цвета радуги
    public float squareColorR = 255f;
    public float squareColorG = 120f;
    public float squareColorB = 20f;
    public float squareSize = 150f; // Размер квадрата
    public float squareSpinSpeed = 0.5f; // Скорость вращения

    // Сглаживание ориентации
    public float smoothYaw = 0f;

    // ========== ПАРАМЕТРЫ ПРИЗРАКОВ (Ghost mode) ==========
    public float ghostScale = 1.85f;          // размер призрака (длина тела)
    public float ghostSpeed = 3.1f;           // скорость вращения
    public float ghostRadius = 2.05f;         // радиус орбиты вокруг цели
    public float ghostAlpha = 0.94f;
    public int ghostCount = 6;                // сколько призраков летает
    public boolean ghostRainbow = true;       // ПО УМОЛЧАНИЮ РЕЙНБОУ ВКЛЮЧЁН
    public float ghostColorR = 40f;
    public float ghostColorG = 255f;          // яркий лайм по умолчанию в static-режиме
    public float ghostColorB = 75f;
    public float ghostTime = 0f;

    // Логика цели
    public LivingEntity target;
    public float fadeAnim = 0f;

    public TargetEspModule() {
        super("TargetESP");
    }

    public LivingEntity getTarget() {
        return target;
    }

    /**
     * Метод захвата цели - вызывается каждый тик
     * Работаеt для обоих режимов
     */
    public void onTick() {
        if (mc.player == null || mc.world == null) {
            target = null;
            fadeAnim = 0;
            return;
        }

        // Проверяем, на кого наведён прицел
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();
            if (entity instanceof LivingEntity living && living != mc.player) {
                // Ограничение до 5 блоков
                if (living.isAlive() && mc.player.distanceTo(living) <= 5.0f) {
                    this.target = living;
                }
            }
        }

        // Проверяем, жива ли текущая цель и в пределах дистанции
        if (target != null) {
            if (!target.isAlive() || target.isRemoved() || mc.player.distanceTo(target) > 5.0f) {
                target = null;
            }
        }

        // Плавная анимация появления/исчезновения
        if (target != null) {
            if (fadeAnim < 1f) fadeAnim += 0.1f;
        } else {
            if (fadeAnim > 0f) fadeAnim -= 0.1f;
        }

        // Зажимаем значения
        if (fadeAnim < 0.01f) fadeAnim = 0f;
        if (fadeAnim > 0.99f) fadeAnim = 1f;
    }

    @Override
    protected void onEnable() {
        // Можно добавить логику при включении модуля
    }

    @Override
    protected void onDisable() {
        target = null;
        fadeAnim = 0f;
        ghostTime = 0f; // Сбрасываем время анимации
        smoothYaw = 0f; // Сбрасываем сглаженный yaw
    }
}
