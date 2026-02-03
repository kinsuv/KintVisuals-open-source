package com.kintvisuals.client.screen;

import com.kintvisuals.client.module.Module;
import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.hud.ArmorHudModule;
import com.kintvisuals.client.module.render.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ModuleListScreen extends Screen {

    private Module selectedModule = null;
    private boolean isDragging = false;

    public ModuleListScreen() {
        super(Text.of("KintVisuals"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int startX = width / 2 - 160;
        int startY = height / 2 - 110;

        // Фон меню
        context.fill(startX, startY, startX + 320, startY + 220, 0xFF121212);
        context.drawBorder(startX, startY, 320, 220, 0xFF252525);

        // ===== СПИСОК МОДУЛЕЙ =====
        int modY = startY + 20;
        for (Module mod : ModuleManager.getModules()) {
            context.drawText(textRenderer, mod.getName(), startX + 15, modY, mod.isEnabled() ? 0xFF00FF99 : 0xFFFFFFFF, false);

            int gearX = startX + 130;
            // Квадратик настройки (буква S)
            context.fill(gearX, modY - 2, gearX + 14, modY + 10, selectedModule == mod ? 0xFF00FF99 : 0xFF333333);
            context.drawText(textRenderer, "S", gearX + 4, modY, 0xFFFFFFFF, false);

            modY += 22;
        }

        // ===== ПРАВАЯ ПАНЕЛЬ (НАСТРОЙКИ) =====
        if (selectedModule != null) {
            int sx = startX + 160;
            int sy = startY + 20;
            float r = 0, g = 0, b = 0;
            boolean showPreview = false;


            if (selectedModule instanceof ChinaHatModule hat) {
                context.drawText(textRenderer, "CHINA HAT SETTINGS", sx, sy, 0xFF00FF99, false);
                hat.r = drawSlider(context, sx, sy + 30, mouseX, mouseY, "Red", hat.r, 0f, 1f);
                hat.g = drawSlider(context, sx, sy + 55, mouseX, mouseY, "Green", hat.g, 0f, 1f);
                hat.b = drawSlider(context, sx, sy + 80, mouseX, mouseY, "Blue", hat.b, 0f, 1f);
                hat.radius = drawSlider(context, sx, sy + 105, mouseX, mouseY, "Radius", hat.radius, 0.3f, 1.5f);
                hat.alpha = drawSlider(context, sx, sy + 155, mouseX, mouseY, "Alpha", hat.alpha, 0.0f, 1.0f);
                hat.height = drawSlider(context, sx, sy + 130, mouseX, mouseY, "Height", hat.height, 0.05f, 0.8f);
                r = hat.r; g = hat.g; b = hat.b;
                showPreview = true;
            }
            else if (selectedModule instanceof TrailsModule trails) {
                context.drawText(textRenderer, "TRAILS SETTINGS", sx, sy, 0xFF00FF99, false);
                trails.r = drawSlider(context, sx, sy + 30, mouseX, mouseY, "Red", trails.r, 0f, 1f);
                trails.g = drawSlider(context, sx, sy + 55, mouseX, mouseY, "Green", trails.g, 0f, 1f);
                trails.b = drawSlider(context, sx, sy + 80, mouseX, mouseY, "Blue", trails.b, 0f, 1f);
                trails.length = drawSlider(context, sx, sy + 105, mouseX, mouseY, "Length", trails.length, 5f, 100f);
                r = trails.r; g = trails.g; b = trails.b;
                showPreview = true;
            }

            else if (selectedModule instanceof ProjectileTrail pt) {
                context.drawText(textRenderer, "TRAIL SETTINGS", sx, sy, 0xFF00FF99, false);
                int y = sy + 25;

                // Rainbow ползунок (0-100)
                float rainbowValue = (float)pt.rainbow.value * 100f;
                float newRainbowValue = drawSlider(context, sx, y, mouseX, mouseY, "Rainbow", rainbowValue, 0f, 100f);

                if (newRainbowValue > 50f) {
                    pt.rainbow.value = 1.0;
                } else if (newRainbowValue <= 50f && newRainbowValue > 0) {
                    pt.rainbow.value = 0.0;
                }

                y += 22;

                boolean rainbowEnabled = pt.rainbow.value > 0.5;

                if (!rainbowEnabled) {
                    // Static RGB режим
                    pt.red.value = drawSlider(context, sx, y, mouseX, mouseY, "Red", (float)pt.red.value, 0f, 255f);
                    pt.green.value = drawSlider(context, sx, y + 22, mouseX, mouseY, "Green", (float)pt.green.value, 0f, 255f);
                    pt.blue.value = drawSlider(context, sx, y + 44, mouseX, mouseY, "Blue", (float)pt.blue.value, 0f, 255f);
                    y += 66;

                    // Preview Color - квадратик с цветом
                    int previewX = sx;
                    int previewY = y;
                    int previewSize = 30;
                    context.fill(previewX, previewY, previewX + previewSize, previewY + previewSize, pt.getPreviewColor());
                    context.drawText(textRenderer, "Preview Color", previewX + previewSize + 5, previewY + 10, -1, false);
                    y += 40;

                } else {
                    // Rainbow режим
                    pt.rainbowSpeed.value = drawSlider(context, sx, y, mouseX, mouseY, "Rainbow Speed", (float)pt.rainbowSpeed.value, 1f, 20f);
                    y += 22;
                }

                // Общие настройки
                pt.thickness.value = drawSlider(context, sx, y, mouseX, mouseY, "Thickness", (float)pt.thickness.value, 0f, 500f);
                pt.trailLength.value = drawSlider(context, sx, y + 22, mouseX, mouseY, "Trail Length", (float)pt.trailLength.value, 10f, 50f);
                pt.lineWidth.value = drawSlider(context, sx, y + 44, mouseX, mouseY, "Diameter", (float)pt.lineWidth.value, 1f, 15f);
            }

            else if (selectedModule instanceof ArmorHudModule ah) {
                context.drawText(textRenderer, "ARMOR HUD SETTINGS", sx, sy, 0xFF00FF99, false);
                int y = sy + 25;

                // Show Percent checkbox (через ползунок)
                float showPercentValue = (float)ah.showPercent.value * 100f;
                float newShowPercentValue = drawSlider(context, sx, y, mouseX, mouseY, "Show Percent", showPercentValue, 0f, 100f);
                if (newShowPercentValue > 50f) {
                    ah.showPercent.value = 1.0;
                } else {
                    ah.showPercent.value = 0.0;
                }
                y += 22;

                // Show Icons checkbox (через ползунок)
                float showIconsValue = (float)ah.showIcons.value * 100f;
                float newShowIconsValue = drawSlider(context, sx, y, mouseX, mouseY, "Show Icons", showIconsValue, 0f, 100f);
                if (newShowIconsValue > 50f) {
                    ah.showIcons.value = 1.0;
                } else {
                    ah.showIcons.value = 0.0;
                }
                y += 22;

                // HUD Scale
                ah.hudScale.value = drawSlider(context, sx, y, mouseX, mouseY, "HUD Scale", (float)ah.hudScale.value, 0.5f, 2.0f);
                y += 22;

                // Position X
                ah.posX.value = drawSlider(context, sx, y, mouseX, mouseY, "Position X", (float)ah.posX.value, 0f, 1000f);
                y += 22;

                // Position Y
                ah.posY.value = drawSlider(context, sx, y, mouseX, mouseY, "Position Y", (float)ah.posY.value, 0f, 1000f);
            }

            else if (selectedModule instanceof RainbowFogModule fog) {
                context.drawText(textRenderer, "CUSTOM FOG SETTINGS", sx, sy, 0xFF00FF99, false);

                float rainbowVal = drawSlider(context, sx, sy + 25, mouseX, mouseY, "Rainbow Mode", fog.rainbow ? 1f : 0f, 0f, 1f);
                fog.rainbow = rainbowVal > 0.5f;

                if (!fog.rainbow) {
                    // Оставляем только настройки цвета
                    fog.r = drawSlider(context, sx, sy + 50, mouseX, mouseY, "Red", fog.r, 0f, 1f);
                    fog.g = drawSlider(context, sx, sy + 75, mouseX, mouseY, "Green", fog.g, 0f, 1f);
                    fog.b = drawSlider(context, sx, sy + 100, mouseX, mouseY, "Blue", fog.b, 0f, 1f);
                } else {
                    // Оставляем только скорость для радуги
                    fog.speed = drawSlider(context, sx, sy + 50, mouseX, mouseY, "Speed", fog.speed, 0.1f, 10.0f);
                }

                float[] c = fog.getFogColor();
                r = c[0]; g = c[1]; b = c[2];
                showPreview = true;
            }

            else if (selectedModule instanceof TargetEspModule esp) {
                context.drawText(textRenderer, "TARGET ESP SETTINGS", sx, sy, 0xFF00FF99, false);

                // Вместо esp.setEnabled используем прямое обращение к переменной или метод toggle()
                float enabledVal = drawSlider(context, sx, sy + 25, mouseX, mouseY, "Enabled", esp.isEnabled() ? 1f : 0f, 0f, 1f);

                boolean shouldBeEnabled = enabledVal > 0.5f;
                if (shouldBeEnabled != esp.isEnabled()) {
                    // Если метода setEnabled нет, попробуй:
                    // esp.enabled = shouldBeEnabled;
                    // Или если есть метод toggle():
                    esp.toggle();
                }

                TargetEspModule.targetSize = drawSlider(context, sx, sy + 50, mouseX, mouseY, "Spiral Size", TargetEspModule.targetSize, 0.1f, 2.0f);
                esp.liveTime = drawSlider(context, sx, sy + 75, mouseX, mouseY, "Live Time (s)", esp.liveTime, 1.0f, 10.0f);
            }

            else if (selectedModule instanceof ViewModel vm) {
                context.drawText(textRenderer, "VIEW MODEL SETTINGS", sx, sy, 0xFF00FF99, false);

                // --- РИСУЕМ ТАБЫ ---
                int tabY = sy + 20;
                int tabWidth = 60;

                // Отрисовка "кнопки" Right Hand
                int rightTabColor = (vm.selectedTab == 0) ? 0xFF00FF99 : 0xFFAAAAAA;
                context.drawText(textRenderer, "[ RIGHT ]", sx, tabY, rightTabColor, false);
                // Проверка клика (упрощенно)
                if (mouseX >= sx && mouseX <= sx + tabWidth && mouseY >= tabY && mouseY <= tabY + 10) {
                    if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(), 0) == 1) {
                        vm.selectedTab = 0;
                    }
                }

                // Отрисовка "кнопки" Left Hand
                int leftTabColor = (vm.selectedTab == 1) ? 0xFF00FF99 : 0xFFAAAAAA;
                context.drawText(textRenderer, "[ LEFT ]", sx + 70, tabY, leftTabColor, false);
                // Проверка клика
                if (mouseX >= sx + 70 && mouseX <= sx + 70 + tabWidth && mouseY >= tabY && mouseY <= tabY + 10) {
                    if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(), 0) == 1) {
                        vm.selectedTab = 1;
                    }
                }

                context.fill(sx, tabY + 12, sx + 130, tabY + 13, 0x55FFFFFF); // Разделительная линия

                // --- ЛОГИКА ОТОБРАЖЕНИЯ ---
                int sliderY = tabY + 20;
                int gap = 22;

                if (vm.selectedTab == 0) {
                    vm.rightX.value = drawSlider(context, sx, sliderY, mouseX, mouseY, "X", (float)vm.rightX.value, -2.0f, 2.0f);
                    vm.rightY.value = drawSlider(context, sx, sliderY + gap, mouseX, mouseY, "Y", (float)vm.rightY.value, -2.0f, 2.0f);
                    vm.rightZ.value = drawSlider(context, sx, sliderY + gap*2, mouseX, mouseY, "Z", (float)vm.rightZ.value, -2.0f, 2.0f);
                    vm.rightRotX.value = drawSlider(context, sx, sliderY + gap*3, mouseX, mouseY, "Rot X", (float)vm.rightRotX.value, -180f, 180f);
                    vm.rightRotY.value = drawSlider(context, sx, sliderY + gap*4, mouseX, mouseY, "Rot Y", (float)vm.rightRotY.value, -180f, 180f);
                    vm.rightRotZ.value = drawSlider(context, sx, sliderY + gap*5, mouseX, mouseY, "Rot Z", (float)vm.rightRotZ.value, -180f, 180f);
                    vm.rightScale.value = drawSlider(context, sx, sliderY + gap*6, mouseX, mouseY, "Scale", (float)vm.rightScale.value, 0.1f, 2.0f);
                } else {
                    vm.leftX.value = drawSlider(context, sx, sliderY, mouseX, mouseY, "X", (float)vm.leftX.value, -2.0f, 2.0f);
                    vm.leftY.value = drawSlider(context, sx, sliderY + gap, mouseX, mouseY, "Y", (float)vm.leftY.value, -2.0f, 2.0f);
                    vm.leftZ.value = drawSlider(context, sx, sliderY + gap*2, mouseX, mouseY, "Z", (float)vm.leftZ.value, -2.0f, 2.0f);
                    vm.leftRotX.value = drawSlider(context, sx, sliderY + gap*3, mouseX, mouseY, "Rot X", (float)vm.leftRotX.value, -180f, 180f);
                    vm.leftRotY.value = drawSlider(context, sx, sliderY + gap*4, mouseX, mouseY, "Rot Y", (float)vm.leftRotY.value, -180f, 180f);
                    vm.leftRotZ.value = drawSlider(context, sx, sliderY + gap*5, mouseX, mouseY, "Rot Z", (float)vm.leftRotZ.value, -180f, 180f);
                    vm.leftScale.value = drawSlider(context, sx, sliderY + gap*6, mouseX, mouseY, "Scale", (float)vm.leftScale.value, 0.1f, 2.0f);
                }
            }

            // Рисуем превью цвета только если нужно
            if (showPreview) {
                int previewColor = (255 << 24) | ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
                context.fill(sx, sy + 165, sx + 140, sy + 185, previewColor);
                context.drawText(textRenderer, "Preview Color", sx + 5, sy + 170, 0xFF000000, false);
            }
        }
    }

    private float drawSlider(DrawContext context, int x, int y, int mouseX, int mouseY, String name, float value, float min, float max) {
        int width = 120;
        int height = 10;

        // Логика перетаскивания (смещение y + 10 так как текст выше)
        if (isDragging && mouseX >= x && mouseX <= x + width && mouseY >= y + 8 && mouseY <= y + height + 8) {
            value = min + ((float) (mouseX - x) / width) * (max - min);
        }

        // Фон слайдера
        context.fill(x, y + 10, x + width, y + 14, 0xFF333333);
        // Заполнение слайдера
        int filled = (int) ((value - min) / (max - min) * width);
        context.fill(x, y + 10, x + MathHelper.clamp(filled, 0, width), y + 14, 0xFF00FF99);

        context.drawText(textRenderer, name + ": " + String.format("%.2f", value), x, y, 0xFFFFFFFF, false);

        return MathHelper.clamp(value, min, max);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int startX = width / 2 - 160;
        int startY = height / 2 - 110;
        int modY = startY + 20;

        for (Module mod : ModuleManager.getModules()) {
            // Клик по имени модуля (включение/выключение)
            if (mouseX >= startX + 15 && mouseX <= startX + 120 && mouseY >= modY && mouseY <= modY + 10) {
                mod.toggle();
                return true;
            }
            // Клик по кнопке S (выбор модуля для настройки)
            if (mouseX >= startX + 130 && mouseX <= startX + 144 && mouseY >= modY - 2 && mouseY <= modY + 10) {
                selectedModule = mod;
                return true;
            }
            modY += 22;
        }

        isDragging = true;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        isDragging = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}