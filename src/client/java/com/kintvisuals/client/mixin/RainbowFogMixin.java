package com.kintvisuals.client.mixin;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.RainbowFogModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BackgroundRenderer.class, priority = 1001)
public class RainbowFogMixin {

    @Inject(
            method = "method_3211(Lnet/minecraft/class_4184;Lnet/minecraft/class_758$class_4596;FZF)V",
            at = @At("TAIL"),
            remap = false
    )
    private static void onSetupFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float skyDarkness, CallbackInfo ci) {
        RainbowFogModule mod = ModuleManager.get(RainbowFogModule.class);
        if (mod != null && mod.isEnabled()) {
            float[] color = mod.getFogColor();
            RenderSystem.setShaderFogColor(color[0], color[1], color[2], 1.0f);

            // Применяем те самые значения, которые убрали полоску, без ползунков
            float multiplier = 100.0f;
            RenderSystem.setShaderFogStart(viewDistance * mod.finalFogStart * multiplier);
            RenderSystem.setShaderFogEnd(viewDistance * mod.finalFogEnd * multiplier);
        }
    }

    @Inject(
            method = "method_3210(Lnet/minecraft/class_4184;FLnet/minecraft/class_638;IF)V",
            at = @At("TAIL"),
            remap = false
    )
    private static void onSetupColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        RainbowFogModule mod = ModuleManager.get(RainbowFogModule.class);
        if (mod != null && mod.isEnabled()) {
            float[] color = mod.getFogColor();
            // Подчищаем фон, чтобы всё было монолитным
            RenderSystem.clearColor(color[0], color[1], color[2], 1.0f);
        }
    }
}