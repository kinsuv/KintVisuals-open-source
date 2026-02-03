package com.kintvisuals.client;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.hud.ArmorHudModule;
import com.kintvisuals.client.module.hud.TargetHudModule;
import com.kintvisuals.client.module.render.*;
import com.kintvisuals.client.render.*;
import com.kintvisuals.client.util.Keybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback; // Добавь этот импорт!

public class KintVisualsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 1️⃣ Регистрируем модули
        ModuleManager.register(new TargetHudModule());
        ModuleManager.register(new TrailsModule());
        ModuleManager.register(new FullBrightModule());
        ModuleManager.register(new NoHurtCamModule());
        ModuleManager.register(new ChinaHatModule());
        ModuleManager.register(new TargetEspModule());
        ModuleManager.register(new ViewModel());
        ModuleManager.register(new RainbowFogModule());
        ModuleManager.register(new ProjectileTrail());
        ModuleManager.register(new ArmorHudModule());
        // 2️⃣ Клавиши
        Keybinds.register();

        // 3️⃣ ЛОГИКА (Tick)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            TrailsModule trails = ModuleManager.get(TrailsModule.class);
            if (trails != null) {
                trails.onUpdate();
            }
        });

        // 4️⃣ РЕНДЕР ПЛАШКИ (HUD) - ЭТОГО У ТЕБЯ НЕ БЫЛО!
        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            TargetHudModule mod = ModuleManager.get(TargetHudModule.class);
            if (mod != null && mod.isEnabled()) {
                mod.onTick();
                TargetHudRenderer.renderHud(drawContext, tickDelta);
            }

            // Добавляем ArmorHud
            ArmorHudModule armorMod = ModuleManager.get(ArmorHudModule.class);
            if (armorMod != null && armorMod.isEnabled()) {
                ArmorHudRenderer.renderHud(drawContext, tickDelta);
            }
        });

        WorldRenderEvents.LAST.register(TargetEspRender::render);
            // Теперь передаем только drawContext

        // 5️⃣ МИРОВОЙ РЕНДЕР (Шляпа, Трейлы, Ромб)
        WorldRenderEvents.END.register(context -> {
            if (context.matrixStack() == null || context.camera() == null) return;

            // China Hat
            ChinaHatRenderer.render(context);

            // Trails
            TrailsRenderer.renderTrails(context.matrixStack(), context.camera());

            // Target ESP (Ромб) - вызываем ОДИН раз
            TargetHudRenderer.renderESP(context.matrixStack(), context.camera(), context.tickDelta());// В WorldRenderContext это метод, возвращающий float
        });
    }
}