package com.kintvisuals.client.mixin;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.RainbowFogModule;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public class MixinClientWorld {

    // ВНИМАНИЕ: Здесь Vec3d заменен на class_243, как просил лог
    @Inject(
            method = "method_23777(Lnet/minecraft/class_243;F)Lnet/minecraft/class_243;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onGetSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> cir) {
        RainbowFogModule mod = ModuleManager.get(RainbowFogModule.class);
        if (mod != null && mod.isEnabled()) {
            float[] color = mod.getFogColor();
            // Возвращаем Vec3d (Java сама поймет, что это class_243)
            cir.setReturnValue(new Vec3d(color[0], color[1], color[2]));
        }
    }
}