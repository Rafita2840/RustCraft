package net.rafa.rustcraft.util;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.networking.ThirstSaturationSyncS2CPayload;

public class ThirstSaturationData {

    public static int addSaturation(IEntityDataSaver player, int amount){
        NbtCompound nbt = player.getPersistentData();
        int saturation = nbt.getInt("thirst_saturation");
        saturation = Math.min(saturation + amount, 5000);
        nbt.putInt("thirst_saturation", saturation);
        syncSaturation(player);
        return saturation;
    }

    public static int resetSaturation(IEntityDataSaver player){
        NbtCompound nbt = player.getPersistentData();
        int saturation = 0;
        nbt.putInt("thirst_saturation", saturation);
        syncSaturation(player);
        return saturation;
    }

    public static void syncSaturation(IEntityDataSaver player) {
        int saturation = player.getPersistentData().getInt("thirst_saturation");
        ThirstSaturationSyncS2CPayload payload = new ThirstSaturationSyncS2CPayload(saturation);
        ServerPlayNetworking.send((ServerPlayerEntity) player, payload);
    }
}
