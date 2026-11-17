package net.rafa.rustcraft.config;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.networking.ServerConfigSyncS2CPayload;

/**
 * Sends the config values and game rules that clients need. {@code /rustcraft reload} (see RustCommands) and
 * a change of a synced game rule (see {@link ModGameRules}) send them again.
 */
public final class ConfigSync {
    private ConfigSync() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ServerConfigSyncS2CPayload.PAYLOAD_ID, ServerConfigSyncS2CPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.player));
    }

    public static void send(ServerPlayerEntity player) {
        RustConfig.Gamma gamma = RustConfig.get().gamma;
        MinecraftServer server = player.getServer();
        boolean thirstEnabled = server == null || server.getGameRules().getBoolean(ModGameRules.THIRST_ENABLED);
        ServerPlayNetworking.send(player, new ServerConfigSyncS2CPayload(gamma.forceLow, gamma.maxGamma, thirstEnabled));
    }
}
