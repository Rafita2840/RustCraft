package net.rafa.rustcraft.config;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.networking.ServerConfigSyncS2CPayload;
import net.rafa.rustcraft.util.ModText;

/** Sends the config values that clients need, and provides {@code /rustcraft reload}. */
public final class ConfigSync {
    private ConfigSync() {}

    public static void register() {
        PayloadTypeRegistry.playS2C().register(ServerConfigSyncS2CPayload.PAYLOAD_ID, ServerConfigSyncS2CPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> send(handler.player));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("rustcraft")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("reload").executes(context -> {
                            MinecraftServer server = context.getSource().getServer();
                            if (!RustConfig.load()) {
                                context.getSource().sendError(ModText.error("text.rustcraft.config_reload_failed"));
                                return 0;
                            }
                            server.getPlayerManager().getPlayerList().forEach(ConfigSync::send);
                            context.getSource().sendFeedback(() -> ModText.info("text.rustcraft.config_reloaded"), true);
                            return 1;
                        }))));
    }

    public static void send(ServerPlayerEntity player) {
        RustConfig.Gamma gamma = RustConfig.get().gamma;
        ServerPlayNetworking.send(player, new ServerConfigSyncS2CPayload(gamma.forceLow, gamma.maxGamma));
    }
}
