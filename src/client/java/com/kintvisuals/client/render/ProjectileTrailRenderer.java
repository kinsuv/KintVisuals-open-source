package com.kintvisuals.client.render;

import com.kintvisuals.client.data.TrailData;
import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.render.ProjectileTrail;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Quaternionf;
import org.joml.Matrix4f;

import java.util.*;

public class ProjectileTrailRenderer {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static final Map<Integer, TrailData> trails = new HashMap<>();

    // Настройки — меняй здесь:
    private static final int SIDES = 8;          // сглаженность "сосиски" (6-12 рекомендовано)
    private static final double CAMERA_OFFSET = 0.06; // на сколько отодвинуть рендер от камеры
    private static final float MIN_SCALE = 0.25f;     // минимальный множитель радиуса при близкой пёрле (0..1)
    private static final float MIN_DIST = 0.5f;      // расстояние, до которого действует минимальный масштаб
    private static final float MAX_DIST = 4.0f;      // расстояние, после которого масштаб = 1.0 (полный радиус)

    public static void onWorldRender(MatrixStack matrices, Camera camera, float tickDelta) {
        ProjectileTrail mod = ModuleManager.get(ProjectileTrail.class);
        if (mod == null || !mod.isEnabled()) {
            trails.clear();
            return;
        }

        if (mc.world == null) return;

        updateTrails(mod);
        renderTrails(matrices, camera, mod);
        cleanupTrails();
    }

    private static void updateTrails(ProjectileTrail mod) {
        for (Entity e : mc.world.getEntities()) {
            if (e instanceof ArrowEntity || e instanceof EnderPearlEntity) {
                if (!e.isRemoved() && !e.isOnGround()) {
                    trails
                            .computeIfAbsent(e.getId(),
                                    id -> new TrailData((int) mod.trailLength.value))
                            .addPoint(e.getPos());
                }
            }
        }
    }

    private static void renderTrails(MatrixStack matrices, Camera camera, ProjectileTrail mod) {
        // Получаем направление взгляда камеры через кватернион (корректно для 1.20.1)
        Vec3d cameraLook = getCameraLook(camera);

        Vec3d cameraPos = camera.getPos().add(cameraLook.multiply(CAMERA_OFFSET));

        float baseRadius = (float) mod.lineWidth.value * 0.01f;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        matrices.push();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        for (TrailData trail : trails.values()) {
            List<Vec3d> points = trail.getPoints();
            if (points.size() < 2) continue;

            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            for (int i = 0; i < points.size() - 1; i++) {
                Vec3d p1 = points.get(i);
                Vec3d p2 = points.get(i + 1);

                Vec3d dir = p2.subtract(p1);
                if (dir.lengthSquared() < 1e-6) continue;
                dir = dir.normalize();

                // Орто-базис
                Vec3d up = Math.abs(dir.y) > 0.9 ? new Vec3d(1, 0, 0) : new Vec3d(0, 1, 0);
                Vec3d right = dir.crossProduct(up).normalize();
                up = right.crossProduct(dir).normalize();

                float t1 = (float) i / points.size();
                float t2 = (float) (i + 1) / points.size();

                float[] rgb1 = mod.getRGB(t1);
                float[] rgb2 = mod.getRGB(t2);
                float a1 = mod.getAlpha(t1);
                float a2 = mod.getAlpha(t2);

                // --- НОВОЕ: вычисляем масштаб радиуса по расстоянию ---
                Vec3d mid = p1.add(p2).multiply(0.5);
                double distToCamera = mid.distanceTo(camera.getPos()); // расстояние до реальной камеры (без offset)
                float scale = computeScaleByDistance((float) distToCamera);
                float radiusSegment = baseRadius * scale;

                Vec3d rp1 = p1.subtract(cameraPos);
                Vec3d rp2 = p2.subtract(cameraPos);

                for (int s = 0; s < SIDES; s++) {
                    double a = 2 * Math.PI * s / SIDES;
                    double b = 2 * Math.PI * (s + 1) / SIDES;

                    Vec3d o1 = right.multiply(Math.cos(a) * radiusSegment)
                            .add(up.multiply(Math.sin(a) * radiusSegment));
                    Vec3d o2 = right.multiply(Math.cos(b) * radiusSegment)
                            .add(up.multiply(Math.sin(b) * radiusSegment));

                    addQuad(buffer, matrix, rp1, rp2, o1, o2, rgb1, a1, rgb2, a2);
                }
            }

            tessellator.draw();
        }

        matrices.pop();

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    // Возвращает вектор взгляда камеры как Vec3d (кватернион -> вектор)
    private static Vec3d getCameraLook(Camera camera) {
        Vector3f v = new Vector3f(0, 0, -1);          // forward в локальной системе
        Quaternionf q = camera.getRotation();         // org.joml.Quaternionf
        q.transform(v);                               // поворачиваем
        return new Vec3d(v.x(), v.y(), v.z());
    }

    // Масштаб по дистанции: при dist <= MIN_DIST -> MIN_SCALE,
    // при dist >= MAX_DIST -> 1.0f, между ними — плавный переход.
    private static float computeScaleByDistance(float dist) {
        if (dist <= MIN_DIST) return MIN_SCALE;
        if (dist >= MAX_DIST) return 1.0f;
        float t = (dist - MIN_DIST) / (MAX_DIST - MIN_DIST);
        // можно сгладить кривую (smoothstep), но линейно достаточно:
        return MIN_SCALE + (1.0f - MIN_SCALE) * t;
    }

    private static void addQuad(BufferBuilder b, Matrix4f m,
                                Vec3d p1, Vec3d p2,
                                Vec3d o1, Vec3d o2,
                                float[] c1, float a1,
                                float[] c2, float a2) {

        b.vertex(m,
                        (float) (p1.x + o1.x),
                        (float) (p1.y + o1.y),
                        (float) (p1.z + o1.z))
                .color(c1[0], c1[1], c1[2], a1).next();

        b.vertex(m,
                        (float) (p1.x + o2.x),
                        (float) (p1.y + o2.y),
                        (float) (p1.z + o2.z))
                .color(c1[0], c1[1], c1[2], a1).next();

        b.vertex(m,
                        (float) (p2.x + o2.x),
                        (float) (p2.y + o2.y),
                        (float) (p2.z + o2.z))
                .color(c2[0], c2[1], c2[2], a2).next();

        b.vertex(m,
                        (float) (p2.x + o1.x),
                        (float) (p2.y + o1.y),
                        (float) (p2.z + o1.z))
                .color(c2[0], c2[1], c2[2], a2).next();
    }

    private static void cleanupTrails() {
        if (mc.world == null) {
            trails.clear();
            return;
        }

        Iterator<Map.Entry<Integer, TrailData>> it = trails.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, TrailData> entry = it.next();
            Entity e = mc.world.getEntityById(entry.getKey());
            if (e == null || e.isRemoved() || (e instanceof ArrowEntity && e.isOnGround())) {
                it.remove();
            }
        }
    }
}