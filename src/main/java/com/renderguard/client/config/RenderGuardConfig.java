package com.renderguard.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-backed config, no extra config-screen dependency required.
 * Lives at .minecraft/config/renderguard.json and can be hand-edited or
 * changed at runtime with the /renderguard command.
 */
public class RenderGuardConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Master switch. */
    public boolean enabled = true;

    /** Entities farther than this (in blocks) from the camera are skipped during rendering. */
    public double entityCullDistance = 48.0;

    /** Particles farther than this (in blocks) from the camera are never spawned. */
    public double particleCullDistance = 32.0;

    /** Draw the small culled-count readout in the top-left corner. */
    public boolean showHud = true;

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("renderguard.json");
    }

    public static RenderGuardConfig load() {
        Path path = configPath();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                RenderGuardConfig loaded = GSON.fromJson(reader, RenderGuardConfig.class);
                if (loaded != null) {
                    return loaded;
                }
            } catch (IOException | com.google.gson.JsonSyntaxException e) {
                System.err.println("[RenderGuard] Failed to read config, regenerating defaults: " + e.getMessage());
            }
        }
        RenderGuardConfig fresh = new RenderGuardConfig();
        fresh.save();
        return fresh;
    }

    public void save() {
        Path path = configPath();
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            System.err.println("[RenderGuard] Failed to save config: " + e.getMessage());
        }
    }
}
