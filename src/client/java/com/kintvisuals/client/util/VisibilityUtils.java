package com.kintvisuals.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public class VisibilityUtils {

    public static boolean canSee(Entity entity) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return false;

        return mc.player.canSee(entity);
    }
}
