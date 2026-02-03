package com.kintvisuals.client.mixin;

import com.kintvisuals.client.render.ArmorHudRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class MixinChatScreen {

    @Inject(
            method = "Lnet/minecraft/class_408;method_25402(DDI)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ArmorHudRenderer.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "Lnet/minecraft/class_408;method_25403(DDIDD)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void onMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if (ArmorHudRenderer.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(
            method = "Lnet/minecraft/class_408;method_25406(DDI)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (ArmorHudRenderer.mouseReleased(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }
}