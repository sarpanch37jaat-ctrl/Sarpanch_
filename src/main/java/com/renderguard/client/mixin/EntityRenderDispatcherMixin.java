package com.renderguard.client.mixin;

import com.renderguard.client.RenderGuardClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Skips the expensive per-entity render call for anything far enough from
 * the camera that the vertices it would submit are effectively noise.
 *
 * This mirrors the technique used by standalone "entity culling" mods:
 * vanilla already frustum-checks entities in WorldRenderer, but it does not
 * apply any distance cutoff of its own, so distant mobs/items/projectiles
 * still get fully rendered (model matrices, texture binds, buffer builder
 * work) even when they occupy only a couple of screen pixels.
 */
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void renderguard$cullDistantEntities(
            E entity, double x, double y, double z, float yaw, float tickDelta,
            MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
            CallbackInfo ci) {

        if (!RenderGuardClient.CONFIG.enabled) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) {
            return;
        }

        // Never cull the entity the camera is attached to, anything the
        // player is riding, or anything riding the player.
        if (entity == cameraEntity || entity == cameraEntity.getVehicle() || entity.hasPassenger(cameraEntity)) {
            return;
        }

        // Always render other players at full distance - they're the thing
        // people are most likely to notice popping out of existence.
        if (entity instanceof PlayerEntity) {
            return;
        }

        double maxDistance = RenderGuardClient.CONFIG.entityCullDistance;
        double maxDistanceSq = maxDistance * maxDistance;

        if (entity.squaredDistanceTo(cameraEntity) > maxDistanceSq) {
            RenderGuardClient.culledEntities++;
            ci.cancel();
        }
    }
}
