package com.renderguard.client.mixin;

import com.renderguard.client.RenderGuardClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops particles from ever being constructed/added when they spawn far
 * enough away that they'd be sub-pixel on screen anyway. This is cheaper
 * than letting Sodium/vanilla cull them post-construction, since we skip
 * the allocation and the addition to the tracked particle list entirely.
 */
@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(
            method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void renderguard$cullDistantParticles(
            ParticleEffect parameters, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            CallbackInfo ci) {

        if (!RenderGuardClient.CONFIG.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        double dx = x - cameraEntity.getX();
        double dy = y - cameraEntity.getY();
        double dz = z - cameraEntity.getZ();
        double distanceSq = dx * dx + dy * dy + dz * dz;

        double maxDistance = RenderGuardClient.CONFIG.particleCullDistance;
        double maxDistanceSq = maxDistance * maxDistance;

        if (distanceSq > maxDistanceSq) {
            RenderGuardClient.culledParticles++;
            ci.cancel();
        }
    }
}
