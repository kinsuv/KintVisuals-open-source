package com.kintvisuals.client.mixin;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.TargetEspModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftClient.class, priority = 1001)
public class MixinMinecraftClient {

    // В 1.20.1 метод tick() в маппингах Intermediary называется method_1574
    @Inject(method = "method_1574", at = @At("HEAD"), remap = false)
    private void onTick(CallbackInfo ci) {
        TargetEspModule mod = ModuleManager.get(TargetEspModule.class);
        if (mod != null) {
            mod.onUpdate();
        }
    }
}