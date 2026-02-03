package com.kintvisuals.client.util;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.hud.HudModule;
import com.kintvisuals.client.module.hud.TargetHudModule;
import com.kintvisuals.client.module.render.ChinaHatModule;
import com.kintvisuals.client.module.render.FullBrightModule;
import com.kintvisuals.client.module.render.NoHurtCamModule;
import com.kintvisuals.client.module.render.TrailsModule;
import com.kintvisuals.client.screen.ModuleListScreen;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import static net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.*;

public class Keybinds {

    public static KeyBinding toggleESP;
    public static KeyBinding toggleHUD;
    public static KeyBinding toggleTargetHUD;
    public static KeyBinding openMenuKey;
    public static KeyBinding toggleNoHurtCam;
    public static KeyBinding toggleFullBright;
    public static KeyBinding toggleChinaHat;

    public static void register() {
        // --- 1. Регистрация кнопок (ID должны быть уникальны!) ---

        toggleESP = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kintvisuals.toggle_esp",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                "key.categories.kintvisuals"
        ));

        toggleChinaHat = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kintvisuals.toggle_chinahat",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z, // Пусть будет на Z
                "key.categories.kintvisuals"
        ));

        toggleHUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kintvisuals.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_H,
                "key.categories.kintvisuals"
        ));

        toggleFullBright = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kintvisuals.toggle_fullbright",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "key.categories.kintvisuals"
        ));

        toggleNoHurtCam = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kintvisuals.toggle_nohurtcam",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_I,
                "key.categories.kintvisuals"
        ));

        toggleTargetHUD = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kintvisuals.toggle_targethud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_T,
                "key.categories.kintvisuals"
        ));

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.kintvisuals.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "key.categories.kintvisuals"
        ));

        // --- 2. Обработка событий в тике ---
        END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Переключение ESP

            // Переключение FullBright
            if (toggleFullBright.wasPressed()) {
                FullBrightModule fb = ModuleManager.get(FullBrightModule.class);
                if (fb != null) fb.toggle();
            }

            if (toggleChinaHat.wasPressed()) {
                ChinaHatModule ch = ModuleManager.get(ChinaHatModule.class);
                if (ch != null) ch.toggle();
            }

            // Переключение NoHurtCam
            if (toggleNoHurtCam.wasPressed()) {
                NoHurtCamModule nhc = ModuleManager.get(NoHurtCamModule.class);
                if (nhc != null) nhc.toggle();
            }

            // Переключение HUD
            if (toggleHUD.wasPressed()) {
                HudModule hud = ModuleManager.get(HudModule.class);
                if (hud != null) hud.toggle();
            }

            // Переключение TargetHUD
            if (toggleTargetHUD.wasPressed()) {
                TargetHudModule targetHud = ModuleManager.get(TargetHudModule.class);
                if (targetHud != null) targetHud.toggle();
            }

            // Открытие меню
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ModuleListScreen());
                }
            }

            // --- 3. Выполнение логики включенных модулей ---

            // Логика FullBright (обновление яркости)
            FullBrightModule fb = ModuleManager.get(FullBrightModule.class);
            if (fb != null && fb.isEnabled()) {
                fb.onTick();
            }

            // Логика NoHurtCam (обновление через настройки)
            NoHurtCamModule nhc = ModuleManager.get(NoHurtCamModule.class);
            if (nhc != null && nhc.isEnabled()) {
                nhc.onTick();
            }

            // Логика Trails
            TrailsModule trails = ModuleManager.get(TrailsModule.class);
            if (trails != null && trails.isEnabled()) {
                trails.onTick();
                if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_UP)) {
                    trails.r = (trails.r + 0.05f);
                    if (trails.r > 1.0f) trails.r = 0f;
                }
            }

            // Логика TargetHUD
            TargetHudModule targetHud = ModuleManager.get(TargetHudModule.class);
            if (targetHud != null && targetHud.isEnabled()) {
                targetHud.onTick();
            }
        });
    }
}