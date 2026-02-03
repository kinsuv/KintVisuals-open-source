package com.kintvisuals.client.mixin;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.RainbowFogModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {

    // Указываем только ID метода. Mixin сам сопоставит аргументы.
    @Inject(method = "method_3188", at = @At("HEAD"), remap = false)
    private void onRenderHead(CallbackInfo ci) {
        RainbowFogModule mod = ModuleManager.get(RainbowFogModule.class);
        if (mod != null && mod.isEnabled()) {
            float[] color = mod.getFogColor();

            // Закрашиваем ту самую "белую пустоту" на горизонте
            RenderSystem.clearColor(color[0], color[1], color[2], 1.0f);
        }
    }
}