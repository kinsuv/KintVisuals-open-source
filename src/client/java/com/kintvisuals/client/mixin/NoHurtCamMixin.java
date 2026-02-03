package com.kintvisuals.client.mixin;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.NoHurtCamModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 1001)
public class NoHurtCamMixin {

    // ВНИМАНИЕ: Мы используем регулярное выражение.
    // "*(Lnet/minecraft/class_4587;F)V" — ищет любой метод, принимающий MatrixStack и float.
    // remap = false — ОБЯЗАТЕЛЬНО, чтобы он не искал в сломанном рефмапе.
    @Inject(
            method = "*(Lnet/minecraft/class_4587;F)V",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onTiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        NoHurtCamModule module = ModuleManager.get(NoHurtCamModule.class);
        if (module != null && module.isEnabled()) {
            ci.cancel();
        }
    }
}