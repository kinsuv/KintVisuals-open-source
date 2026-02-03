package com.kintvisuals.client.module.hud;

import com.kintvisuals.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TargetHudModule extends Module {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public LivingEntity stickyTarget;
    public float hudScale = 0f;

    public TargetHudModule() {
        super("TargetHUD");
    }

    public LivingEntity getTarget() {
        return this.stickyTarget;
    }

    public void onTick() {
        if (mc.player == null || mc.world == null) {
            stickyTarget = null;
            hudScale = 0;
            return;
        }

        // 1. Поиск цели через прицел
        HitResult hit = mc.crosshairTarget;
        if (hit instanceof EntityHitResult entityHit) {
            Entity entity = entityHit.getEntity();

            // Проверяем: это живое существо, не мы сами, оно живо и в пределах 5 блоков
            if (entity instanceof LivingEntity living && living != mc.player) {
                if (living.isAlive() && mc.player.distanceTo(living) <= 5.0f) {
                    this.stickyTarget = living;
                }
            }
        }

        // 2. Проверка текущей цели на "вылет" (сброс)
        if (stickyTarget != null) {
            // Если цель умерла, удалена из мира или ушла дальше 5 блоков — обнуляем
            if (!stickyTarget.isAlive() || stickyTarget.isRemoved() || mc.player.distanceTo(stickyTarget) > 5.0f) {
                stickyTarget = null;
            }
        }

        // 3. Плавная анимация появления плашки (Scale)
        if (stickyTarget != null) {
            if (hudScale < 1f) hudScale += 0.1f;
        } else {
            if (hudScale > 0f) hudScale -= 0.1f;
        }

        // Ограничиваем значения, чтобы не было микро-чисел
        if (hudScale < 0.01f) hudScale = 0f;
        if (hudScale > 0.99f) hudScale = 1f;
    }
}