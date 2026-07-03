package com.renderguard.client;

import com.renderguard.client.command.RenderGuardCommands;
import com.renderguard.client.config.RenderGuardConfig;
import com.renderguard.client.hud.RenderGuardHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class RenderGuardClient implements ClientModInitializer {

    public static final String MOD_ID = "renderguard";

    public static final RenderGuardConfig CONFIG = RenderGuardConfig.load();

    // Rolling per-second counters, read by the HUD and reset once a second.
    public static volatile int culledEntities = 0;
    public static volatile int culledParticles = 0;
    public static volatile int lastEntityCount = 0;
    public static volatile int lastParticleCount = 0;

    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        RenderGuardCommands.register();
        HudRenderCallback.EVENT.register(new RenderGuardHud());

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter >= 20) { // roughly once a second
                tickCounter = 0;
                lastEntityCount = culledEntities;
                lastParticleCount = culledParticles;
                culledEntities = 0;
                culledParticles = 0;
            }
        });
    }
}
