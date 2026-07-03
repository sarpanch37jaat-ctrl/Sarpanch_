package com.renderguard.client.command;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.renderguard.client.RenderGuardClient;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.DoubleArgumentType.doubleArg;
import static com.mojang.brigadier.arguments.BoolArgumentType.bool;

public class RenderGuardCommands {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(RenderGuardCommands::onRegister);
    }

    private static void onRegister(com.mojang.brigadier.CommandDispatcher<net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource> dispatcher,
                                    CommandRegistryAccess registryAccess) {
        dispatcher.register(ClientCommandManager.literal("renderguard")
                .executes(ctx -> {
                    ctx.getSource().sendFeedback(Text.literal(
                            "RenderGuard: enabled=" + RenderGuardClient.CONFIG.enabled
                                    + " entityCullDistance=" + RenderGuardClient.CONFIG.entityCullDistance
                                    + " particleCullDistance=" + RenderGuardClient.CONFIG.particleCullDistance
                                    + " showHud=" + RenderGuardClient.CONFIG.showHud));
                    return 1;
                })
                .then(ClientCommandManager.literal("toggle")
                        .executes(ctx -> {
                            RenderGuardClient.CONFIG.enabled = !RenderGuardClient.CONFIG.enabled;
                            RenderGuardClient.CONFIG.save();
                            ctx.getSource().sendFeedback(Text.literal("RenderGuard enabled=" + RenderGuardClient.CONFIG.enabled));
                            return 1;
                        }))
                .then(ClientCommandManager.literal("hud")
                        .then(ClientCommandManager.argument("value", bool())
                                .executes(ctx -> {
                                    boolean value = BoolArgumentType.getBool(ctx, "value");
                                    RenderGuardClient.CONFIG.showHud = value;
                                    RenderGuardClient.CONFIG.save();
                                    ctx.getSource().sendFeedback(Text.literal("RenderGuard showHud=" + value));
                                    return 1;
                                })))
                .then(ClientCommandManager.literal("entitydistance")
                        .then(ClientCommandManager.argument("blocks", doubleArg(0.0, 512.0))
                                .executes(ctx -> {
                                    double value = DoubleArgumentType.getDouble(ctx, "blocks");
                                    RenderGuardClient.CONFIG.entityCullDistance = value;
                                    RenderGuardClient.CONFIG.save();
                                    ctx.getSource().sendFeedback(Text.literal("RenderGuard entityCullDistance=" + value));
                                    return 1;
                                })))
                .then(ClientCommandManager.literal("particledistance")
                        .then(ClientCommandManager.argument("blocks", doubleArg(0.0, 256.0))
                                .executes(ctx -> {
                                    double value = DoubleArgumentType.getDouble(ctx, "blocks");
                                    RenderGuardClient.CONFIG.particleCullDistance = value;
                                    RenderGuardClient.CONFIG.save();
                                    ctx.getSource().sendFeedback(Text.literal("RenderGuard particleCullDistance=" + value));
                                    return 1;
                                }))));
    }
}
