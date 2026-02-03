package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

public class FullBrightModule extends Module {
    public FullBrightModule() {
        super("FullBright");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        if (this.isEnabled()) {
            // Накладываем эффект ночного зрения на 20 секунд каждый тик (бесконечно)
            // Параметры: эффект, длительность (в тиках, 400 = 20 сек), уровень (0 = первый),
            // ambient (false), showParticles (false - чтобы не было пузырьков перед глазами)
            mc.player.addStatusEffect(new StatusEffectInstance(
                    StatusEffects.NIGHT_VISION, 400, 0, false, false
            ));
        } else {
            // Когда выключаем — убираем эффект
            if (mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
        }
    }

    @Override
    public void onDisable() {
        // Убираем эффект сразу при выключении модуля
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null && mc.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            mc.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
        }
    }
}