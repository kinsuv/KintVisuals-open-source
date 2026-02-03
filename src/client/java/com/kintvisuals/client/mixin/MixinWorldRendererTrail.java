package com.kintvisuals.client.mixin;

import com.kintvisuals.client.render.ProjectileTrailRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldRenderer.class, priority = 1001)
public class MixinWorldRendererTrail {

    @Inject(
            method = "method_22710(Lnet/minecraft/class_4587;FJZLnet/minecraft/class_4184;Lnet/minecraft/class_757;Lnet/minecraft/class_765;Lorg/joml/Matrix4f;)V",
            at = @At("TAIL"),
            remap = false
    )
    private void onAfterEntitiesRender(MatrixStack matrices, float tickDelta, long limitTime,
                                       boolean renderBlockOutline, Camera camera,
                                       net.minecraft.client.render.GameRenderer gameRenderer,
                                       net.minecraft.client.render.LightmapTextureManager lightmapTextureManager,
                                       org.joml.Matrix4f projectionMatrix, CallbackInfo ci) {
        ProjectileTrailRenderer.onWorldRender(matrices, camera, tickDelta);
    }
}