package com.kintvisuals.client.render; // Убедись, что пакет совпадает с твоим проектом

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.ChinaHatModule;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ChinaHatRenderer {

    public static void render(WorldRenderContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ChinaHatModule mod = ModuleManager.get(ChinaHatModule.class);
        if (mod == null || !mod.isEnabled()) return;

        // Проверки на включение
        if (mod == null || !mod.isEnabled() || mc.options.getPerspective().isFirstPerson() || mc.player == null) {
            return;
        }

        // --- ИСПРАВЛЕНИЕ ОШИБОК ТУТ ---
        // Теперь мы берем значения напрямую, так как это просто float переменные
        float radius = mod.radius;
        float r = mod.r;
        float g = mod.g;
        float b = mod.b;
        float a = mod.alpha;
        // ------------------------------

        MatrixStack matrices = context.matrixStack();
        Camera camera = context.camera();

        matrices.push();

        double x = mc.player.prevX + (mc.player.getX() - mc.player.prevX) * context.tickDelta() - camera.getPos().x;
        double y = mc.player.prevY + (mc.player.getY() - mc.player.prevY) * context.tickDelta() - camera.getPos().y;
        double z = mc.player.prevZ + (mc.player.getZ() - mc.player.prevZ) * context.tickDelta() - camera.getPos().z;

        matrices.translate(x, y + mc.player.getHeight() + 0.1, z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.player.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.player.getPitch() * 0.2f));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.disableCull();

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Верхушка (используем r, g, b из модуля)
        buffer.vertex(matrix, 0, mod.height, 0).color(r, g, b, a).next();

        int segments = 40;
        for (int i = 0; i <= 40; i++) {
            float angle = (float) (i * Math.PI * 2 / 40);
            float vx = (float) Math.cos(angle) * mod.radius;
            float vz = (float) Math.sin(angle) * mod.radius;

            // Края (чуть прозрачнее - 0.3f)
            buffer.vertex(matrix, vx, 0, vz).color(r, g, b, a * 0.5f).next();
            float edgeAlpha = mod.alpha * 0.5f;
            buffer.vertex(matrix, vx, 0, vz).color(mod.r, mod.g, mod.b, edgeAlpha).next();
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableCull();
        RenderSystem.disableBlend();

        matrices.pop();
    }
}