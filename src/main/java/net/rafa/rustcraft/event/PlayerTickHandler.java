package net.rafa.rustcraft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.data.PlayerStats;
import net.rafa.rustcraft.data.RustPlayerData;

public class PlayerTickHandler implements ServerTickEvents.StartTick {

    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            float speedVariation = player.horizontalSpeed - player.prevHorizontalSpeed;
            int amount = 1;
            if (speedVariation > 0f) {
                amount += 2;
            }
            if (speedVariation > 0.13f) {
                amount += 7;
            }
            if (speedVariation > 0.20f) {
                amount += 2;
            }

            RustPlayerData data = PlayerStats.get(player);
            int exhaustion = data.thirstExhaustion() + amount;
            if (exhaustion >= RustPlayerData.THIRST_EXHAUSTION_LIMIT) {
                PlayerStats.set(player, data.withThirst(data.thirst() - 1).withThirstExhaustion(0));
            } else {
                // Hidden counter: saved with the player but never sent to the client
                PlayerStats.set(player, data.withThirstExhaustion(exhaustion));
            }
        }
    }
}
