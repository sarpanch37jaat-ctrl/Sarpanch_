package com.renderguard.client.hud;

import com.renderguard.client.RenderGuardClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class RenderGuardHud implements HudRenderCallback {

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (!RenderGuardClient.CONFIG.enabled || !RenderGuardClient.CONFIG.showHud) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) {
            return;
        }

        String line = String.format(
                "RenderGuard: -%d entities/s  -%d particles/s",
                RenderGuardClient.lastEntityCount,
                RenderGuardClient.lastParticleCount
        );

        context.drawTextWithShadow(client.textRenderer, line, 4, 4, 0x55FF55);
    }
}
