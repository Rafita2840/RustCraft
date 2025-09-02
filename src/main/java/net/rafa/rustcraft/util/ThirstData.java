package net.rafa.rustcraft.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.networking.ModMessages;
import net.rafa.rustcraft.networking.ThirstSyncS2CPayload;

public class ThirstData {

    public static int addThirst(IEntityDataSaver player, int amount){
        NbtCompound nbt = player.getPersistentData();
        int thirst = nbt.getInt("thirst");
        thirst = Math.min(thirst + amount, 10);
        nbt.putInt("thirst", thirst);
        return thirst;
    }

    public static int removeThirst(IEntityDataSaver player, int amount){
        NbtCompound nbt = player.getPersistentData();
        int thirst = nbt.getInt("thirst");
        thirst = Math.max(thirst - amount, 0);
        nbt.putInt("thirst", thirst);
        return thirst;
    }

    public static void syncThirst(IEntityDataSaver player) {
        int thirst = player.getPersistentData().getInt("thirst");
        ThirstSyncS2CPayload payload = new ThirstSyncS2CPayload(thirst);
        ServerPlayNetworking.send((ServerPlayerEntity) player, payload);
    }
}
