package com.kintvisuals.client.render;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.hud.ArmorHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class ArmorHudRenderer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean isDragging = false;
    private static int dragOffsetX = 0;
    private static int dragOffsetY = 0;

    public static void renderHud(DrawContext context, float delta) {
        ArmorHudModule mod = ModuleManager.get(ArmorHudModule.class);
        if (mod == null || !mod.isEnabled()) return;

        PlayerEntity player = mc.player;
        if (player == null) return;

        MatrixStack matrices = context.getMatrices();
        matrices.push();

        int x = (int)mod.posX.value;
        int y = (int)mod.posY.value;

        matrices.translate(x, y, 0);
        matrices.scale((float)mod.hudScale.value, (float)mod.hudScale.value, 1);

        // Массив слотов: [3=Helmet, 2=Chestplate, 1=Leggings, 0=Boots]
        int slotHeight = 20;
        int currentY = 0;

        // Рисуем в порядке: Helmet -> Chestplate -> Leggings -> Boots
        for (int i = 3; i >= 0; i--) {
            ItemStack stack = player.getInventory().getArmorStack(i);

            // Если слот пустой - пропускаем
            if (stack.isEmpty()) {
                continue;
            }

            // Если предмет не имеет прочности - пропускаем
            if (!stack.isDamageable()) {
                continue;
            }

            // Считаем прочность
            int maxDurability = stack.getMaxDamage();
            int currentDurability = maxDurability - stack.getDamage();
            float percent = currentDurability / (float)maxDurability;

            int color = mod.getDurabilityColor(percent);

            // Рисуем иконку
            if (mod.showIcons.value > 0.5) {
                context.drawItem(stack, 0, currentY);
            }

            // Рисуем только процент
            String text = String.format("%.0f%%", percent * 100);

            int textX = mod.showIcons.value > 0.5 ? 20 : 0;
            context.drawText(mc.textRenderer, text, textX, currentY + 4, color, true);

            // Рисуем бар под текстом
            int barWidth = 50;
            int barHeight = 3;
            int barX = textX;
            int barY = currentY + 14;

            // Фон бара
            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
            // Заполнение бара
            int fillWidth = (int)(barWidth * percent);
            context.fill(barX, barY, barX + fillWidth, barY + barHeight, color);

            currentY += slotHeight;
        }

        // Оружие в руке (опционально)
        ItemStack mainHand = player.getMainHandStack();
        if (!mainHand.isEmpty() && mainHand.isDamageable()) {
            currentY += 5; // Небольшой отступ

            int maxDurability = mainHand.getMaxDamage();
            int currentDurability = maxDurability - mainHand.getDamage();
            float percent = currentDurability / (float)maxDurability;

            int color = mod.getDurabilityColor(percent);

            if (mod.showIcons.value > 0.5) {
                context.drawItem(mainHand, 0, currentY);
            }

            String text = String.format("%.0f%%", percent * 100);

            int textX = mod.showIcons.value > 0.5 ? 20 : 0;
            context.drawText(mc.textRenderer, text, textX, currentY + 4, color, true);

            // Бар
            int barWidth = 50;
            int barHeight = 3;
            int barX = textX;
            int barY = currentY + 14;

            context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF333333);
            int fillWidth = (int)(barWidth * percent);
            context.fill(barX, barY, barX + fillWidth, barY + barHeight, color);
        }

        matrices.pop();
    }

    public static boolean mouseClicked(double mouseX, double mouseY, int button) {
        ArmorHudModule mod = ModuleManager.get(ArmorHudModule.class);
        if (mod == null || !mod.isEnabled()) return false;

        // Проверяем открыт ли чат
        if (mc.currentScreen == null || !mc.currentScreen.getClass().getSimpleName().contains("ChatScreen")) {
            return false;
        }

        if (button == 0) { // Левая кнопка мыши
            int x = (int)mod.posX.value;
            int y = (int)mod.posY.value;
            int width = 80;
            int height = 100; // Примерная высота всего HUD

            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
                isDragging = true;
                dragOffsetX = (int)(mouseX - x);
                dragOffsetY = (int)(mouseY - y);
                return true;
            }
        }
        return false;
    }

    public static boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        ArmorHudModule mod = ModuleManager.get(ArmorHudModule.class);
        if (mod == null || !mod.isEnabled()) return false;

        if (isDragging && button == 0) {
            mod.posX.value = mouseX - dragOffsetX;
            mod.posY.value = mouseY - dragOffsetY;
            return true;
        }
        return false;
    }

    public static boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return false;
    }
}