package com.kintvisuals.client.mixin;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.ViewModel;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HeldItemRenderer.class, priority = 1001)
public class MixinHeldItemRenderer {

    // Временные переменные, чтобы помнить, что мы натворили в HEAD
    private float lastPx, lastPy, lastPz, lastS;
    private float lastRx, lastRy, lastRz;

    @Inject(
            method = "*(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
            at = @At("HEAD"),
            remap = false
    )
    private void onRenderItemHead(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModel mod = ModuleManager.get(ViewModel.class);

        if (mod != null && mod.isEnabled()) {
            // Определяем физическую руку
            boolean isRightArm = (hand == Hand.MAIN_HAND && player.getMainArm() == net.minecraft.util.Arm.RIGHT)
                    || (hand == Hand.OFF_HAND && player.getMainArm() == net.minecraft.util.Arm.LEFT);

            // Запоминаем значения в поля класса, чтобы в TAIL знать, что откатывать
            lastPx = (float) (isRightArm ? mod.rightX.value : mod.leftX.value);
            lastPy = (float) (isRightArm ? mod.rightY.value : mod.leftY.value);
            lastPz = (float) (isRightArm ? mod.rightZ.value : mod.leftZ.value);
            lastRx = (float) (isRightArm ? mod.rightRotX.value : mod.leftRotX.value);
            lastRy = (float) (isRightArm ? mod.rightRotY.value : mod.leftRotY.value);
            lastRz = (float) (isRightArm ? mod.rightRotZ.value : mod.leftRotZ.value);
            lastS  = (float) (isRightArm ? mod.rightScale.value : mod.leftScale.value);

            // Применяем
            matrices.translate(lastPx, lastPy, lastPz);
            matrices.translate(0.5f * (1.0f - lastS), -0.5f * (1.0f - lastS), 0.0f);
            matrices.scale(lastS, lastS, lastS);
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lastRx));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(lastRy));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(lastRz));
        }
    }

    @Inject(
            method = "*(Lnet/minecraft/class_742;FFLnet/minecraft/class_1268;FLnet/minecraft/class_1799;FLnet/minecraft/class_4587;Lnet/minecraft/class_4597;I)V",
            at = @At("TAIL"),
            remap = false
    )
    private void onRenderItemTail(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ViewModel mod = ModuleManager.get(ViewModel.class);
        if (mod != null && mod.isEnabled()) {
            // ОТКАТЫВАЕМ ВСЁ В ОБРАТНОМ ПОРЯДКЕ
            // Чтобы следующая рука начала с "чистого листа"
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-lastRz));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-lastRy));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-lastRx));
            matrices.scale(1.0f / lastS, 1.0f / lastS, 1.0f / lastS);
            matrices.translate(-(0.5f * (1.0f - lastS)), -(-(0.5f * (1.0f - lastS))), 0.0f);
            matrices.translate(-lastPx, -lastPy, -lastPz);
        }
    }
}