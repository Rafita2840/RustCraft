package net.rafa.rustcraft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.config.ModGameRules;
import net.rafa.rustcraft.config.RustConfig;
import net.rafa.rustcraft.data.PlayerStats;
import net.rafa.rustcraft.data.ThirstMath;

public class PlayerTickHandler implements ServerTickEvents.StartTick {

    @Override
    public void onStartTick(MinecraftServer server) {
        if (!server.getGameRules().getBoolean(ModGameRules.THIRST_ENABLED)) {
            return;
        }
        RustConfig.Thirst config = RustConfig.get().thirst;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            float speedVariation = player.horizontalSpeed - player.prevHorizontalSpeed;
            int drain = ThirstMath.drainPerTick(speedVariation, config);
            // The exhaustion counter is saved with the player but never sent to the client
            PlayerStats.set(player, ThirstMath.applyDrain(PlayerStats.get(player), drain, config.exhaustionLimit));
        }
    }
}
