package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import net.minecraft.client.MinecraftClient;

public class NoHurtCamModule extends Module {
    public NoHurtCamModule() {
        super("NoHurtCam");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.options == null) return;

        if (this.isEnabled()) {
            // Force-set силу тряски на 0
            mc.options.getDamageTiltStrength().setValue(0.0);
        } else {
            // Возвращаем стандартную тряску (1.0 - 100%)
            mc.options.getDamageTiltStrength().setValue(1.0);
        }
    }
}