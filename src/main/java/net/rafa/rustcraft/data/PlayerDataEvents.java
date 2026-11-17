package net.rafa.rustcraft.data;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

/** Keeps player stats correct across join, death and dimension change. */
public final class PlayerDataEvents {
    private PlayerDataEvents() {}

    public static void register() {
        // The client forgets everything when it (re)connects, so send the values on join.
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> PlayerStats.sync(handler.player));

        // New player object after death or returning from the End: carry the data over.
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            RustPlayerData data = alive
                    ? oldPlayer.getAttachedOrCreate(ModAttachments.PLAYER_DATA)
                    : RustPlayerData.afterDeath();
            newPlayer.setAttached(ModAttachments.PLAYER_DATA, data);
        });

        // The client creates a new player on respawn and dimension change, so it needs the values again.
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> PlayerStats.sync(newPlayer));
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
                (player, origin, destination) -> PlayerStats.sync(player));
    }
}
