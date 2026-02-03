package com.kintvisuals.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

public class RenderUtils {

    // Метод подготовки рендера (X-Ray и смешивание)
    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
    }

    // Метод завершения рендера
    public static void endRender() {
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
    }

    // Твой метод отрисовки сосисок с gap
    public static void draw3DLine(MatrixStack matrices, float x1, float y1, float z1, float x2, float y2, float z2, float thickness, float gap, int r, int g, int b, int a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float dx = x2 - x1, dy = y2 - y1, dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < gap * 2.1f) return;

        float nx = dx / len, ny = dy / len, nz = dz / len;
        float sX = x1 + nx * gap, sY = y1 + ny * gap, sZ = z1 + nz * gap;
        float eX = x2 - nx * gap, eY = y2 - ny * gap, eZ = z2 - nz * gap;

        // Базис для круга
        float px = 1, py = 0, pz = 0;
        if (Math.abs(nx) > 0.9f) { px = 0; py = 1; }
        float ax = py * nz - pz * ny, ay = pz * nx - px * nz, az = px * ny - py * nx;
        float alen = (float) Math.sqrt(ax*ax + ay*ay + az*az);
        ax /= alen; ay /= alen; az /= alen;
        float bx = ay * nz - az * ny, by = az * nx - ax * nz, bz = ax * ny - ay * nx;

        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        // 1. Рисуем саму трубу
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        int segments = 12;
        for (int i = 0; i < segments; i++) {
            float ang1 = (float) (i * Math.PI * 2 / segments), ang2 = (float) ((i + 1) * Math.PI * 2 / segments);
            float cx1 = (float)Math.cos(ang1)*thickness, cy1 = (float)Math.sin(ang1)*thickness;
            float cx2 = (float)Math.cos(ang2)*thickness, cy2 = (float)Math.sin(ang2)*thickness;

            buffer.vertex(matrix, sX + cx1*ax + cy1*bx, sY + cx1*ay + cy1*by, sZ + cx1*az + cy1*bz).color(r, g, b, a).next();
            buffer.vertex(matrix, eX + cx1*ax + cy1*bx, eY + cx1*ay + cy1*by, eZ + cx1*az + cy1*bz).color(r, g, b, a).next();
            buffer.vertex(matrix, eX + cx2*ax + cy2*bx, eY + cx2*ay + cy2*by, eZ + cx2*az + cy2*bz).color(r, g, b, a).next();
            buffer.vertex(matrix, sX + cx2*ax + cy2*bx, sY + cx2*ay + cy2*by, sZ + cx2*az + cy2*bz).color(r, g, b, a).next();
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // 2. Рисуем закругления (шапочки) на концах
        drawCap(matrix, sX, sY, sZ, thickness, ax, ay, az, bx, by, bz, -nx, -ny, -nz, r, g, b, a);
        drawCap(matrix, eX, eY, eZ, thickness, ax, ay, az, bx, by, bz, nx, ny, nz, r, g, b, a);
    }

    // Вспомогательный метод для "шапочки" сосиски
    private static void drawCap(Matrix4f mat, float x, float y, float z, float rad, float ax, float ay, float az, float bx, float by, float bz, float nx, float ny, float nz, int r, int g, int b, int a) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        int segments = 12;
        int stacks = 6;
        for (int i = 0; i < segments; i++) {
            for (int j = 0; j < stacks; j++) {
                float phi1 = (float) (j * Math.PI / 2 / stacks);
                float phi2 = (float) ((j + 1) * Math.PI / 2 / stacks);
                float theta1 = (float) (i * Math.PI * 2 / segments);
                float theta2 = (float) ((i + 1) * Math.PI * 2 / segments);

                drawSphereQuad(buffer, mat, x, y, z, rad, phi1, phi2, theta1, theta2, ax, ay, az, bx, by, bz, nx, ny, nz, r, g, b, a);
            }
        }
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    private static void drawSphereQuad(BufferBuilder b, Matrix4f m, float x, float y, float z, float r, float p1, float p2, float t1, float t2, float ax, float ay, float az, float bx, float by, float bz, float nx, float ny, float nz, int cr, int cg, int cb, int ca) {
        vS(b, m, x, y, z, r, p1, t1, ax, ay, az, bx, by, bz, nx, ny, nz, cr, cg, cb, ca);
        vS(b, m, x, y, z, r, p2, t1, ax, ay, az, bx, by, bz, nx, ny, nz, cr, cg, cb, ca);
        vS(b, m, x, y, z, r, p2, t2, ax, ay, az, bx, by, bz, nx, ny, nz, cr, cg, cb, ca);
        vS(b, m, x, y, z, r, p1, t2, ax, ay, az, bx, by, bz, nx, ny, nz, cr, cg, cb, ca);
    }

    private static void vS(BufferBuilder b, Matrix4f m, float x, float y, float z, float r, float p, float t, float ax, float ay, float az, float bx, float by, float bz, float nx, float ny, float nz, int cr, int cg, int cb, int ca) {
        float sinP = (float)Math.sin(p), cosP = (float)Math.cos(p);
        float sinT = (float)Math.sin(t), cosT = (float)Math.cos(t);
        float ox = (cosP*cosT*ax + cosP*sinT*bx + sinP*nx) * r;
        float oy = (cosP*cosT*ay + cosP*sinT*by + sinP*ny) * r;
        float oz = (cosP*cosT*az + cosP*sinT*bz + sinP*nz) * r;
        b.vertex(m, x + ox, y + oy, z + oz).color(cr, cg, cb, ca).next();
    }
}