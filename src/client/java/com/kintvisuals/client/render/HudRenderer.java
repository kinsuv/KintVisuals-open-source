package com.kintvisuals.client.render;

import com.kintvisuals.client.module.ModuleManager;
import com.kintvisuals.client.module.hud.HudModule;
import com.kintvisuals.client.module.hud.TargetHudModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class HudRenderer {

    static final MinecraftClient mc = MinecraftClient.getInstance();

    public static void render(DrawContext context, float tickDelta) {

        HudModule hud = ModuleManager.get(HudModule.class);
        if (hud != null && hud.isEnabled()) {
            renderHud(context);
        }

        TargetHudModule targetHud = ModuleManager.get(TargetHudModule.class);
        if (targetHud != null && targetHud.isEnabled()) {
            renderTargetHud(context, targetHud);
        }
    }

    private static void renderHud(DrawContext context) {
        // обычный HUD
    }

    private static void renderTargetHud(DrawContext context, TargetHudModule targetHud) {
        var target = targetHud.getTarget();
        if (target == null) return;

        // target HUD
    }
}
