package net.rafa.rustcraft.data;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.networking.PlayerStatsSyncS2CPayload;

/**
 * Server-side access to the player stats. Always change stats through here:
 * the client is only sent a packet when a value it displays actually changed.
 */
public final class PlayerStats {
    private PlayerStats() {}

    public static RustPlayerData get(ServerPlayerEntity player) {
        return player.getAttachedOrCreate(ModAttachments.PLAYER_DATA);
    }

    /** Stores the data and syncs the client if the displayed values changed. */
    public static void set(ServerPlayerEntity player, RustPlayerData data) {
        RustPlayerData old = get(player);
        player.setAttached(ModAttachments.PLAYER_DATA, data);
        if (old.thirst() != data.thirst()) {
            sync(player);
        }
    }

    public static int addThirst(ServerPlayerEntity player, int amount) {
        RustPlayerData data = get(player).withThirst(get(player).thirst() + amount);
        set(player, data);
        return data.thirst();
    }

    public static int removeThirst(ServerPlayerEntity player, int amount) {
        return addThirst(player, -amount);
    }

    /** Sends the displayed values to the player. Used on join, respawn and dimension change. */
    public static void sync(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new PlayerStatsSyncS2CPayload(get(player).thirst()));
    }
}
