package com.kintvisuals.client.module.render;

import com.kintvisuals.client.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.client.MinecraftClient;

public class TargetEspModule extends Module {
    public static Entity targetEntity = null;
    public static float animation = 0f;

    // СДЕЛАЙ ЕЁ STATIC:
    public static float targetSize = 0.5f;

    public boolean onlyPlayers = true;
    public float liveTime = 2.0f;
    private long lastSeenTime = 0;

    public TargetEspModule() {
        super("TargetESP");
    }

    public void onUpdate() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || !this.isEnabled()) {
            animation = MathHelper.lerp(0.1f, animation, 0f);
            return;
        }

        Entity current = mc.targetedEntity;
        if (current instanceof net.minecraft.entity.LivingEntity) {
            if (!onlyPlayers || current instanceof net.minecraft.entity.player.PlayerEntity) {
                targetEntity = current;
                lastSeenTime = System.currentTimeMillis();
            }
        }

        long now = System.currentTimeMillis();
        if (targetEntity != null) {
            if (now - lastSeenTime < (liveTime * 1000L) && !targetEntity.isRemoved()) {
                animation = MathHelper.lerp(0.15f, animation, 1f);
            } else {
                animation = MathHelper.lerp(0.1f, animation, 0f);
                if (animation < 0.01f) targetEntity = null;
            }
        }
    }
}