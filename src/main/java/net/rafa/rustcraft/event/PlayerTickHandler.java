package net.rafa.rustcraft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.config.ModGameRules;
import net.rafa.rustcraft.config.RustConfig;
import net.rafa.rustcraft.data.PlayerStats;
import net.rafa.rustcraft.data.RustPlayerData;

public class PlayerTickHandler implements ServerTickEvents.StartTick {

    @Override
    public void onStartTick(MinecraftServer server) {
        if (!server.getGameRules().getBoolean(ModGameRules.THIRST_ENABLED)) {
            return;
        }
        RustConfig.Thirst config = RustConfig.get().thirst;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            float speedVariation = player.horizontalSpeed - player.prevHorizontalSpeed;
            int amount = config.idleDrain;
            if (speedVariation > 0f) {
                amount += config.walkDrain;
            }
            if (speedVariation > 0.13f) {
                amount += config.runDrain;
            }
            if (speedVariation > 0.20f) {
                amount += config.sprintDrain;
            }

            RustPlayerData data = PlayerStats.get(player);
            int exhaustion = data.thirstExhaustion() + amount;
            if (exhaustion >= config.exhaustionLimit) {
                PlayerStats.set(player, data.withThirst(data.thirst() - 1).withThirstExhaustion(0));
            } else {
                // Hidden counter: saved with the player but never sent to the client
                PlayerStats.set(player, data.withThirstExhaustion(exhaustion));
            }
        }
    }
}
