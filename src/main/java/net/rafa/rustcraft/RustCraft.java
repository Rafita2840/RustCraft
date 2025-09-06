package net.rafa.rustcraft;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.event.PlayerTickHandler;
import net.rafa.rustcraft.item.ModGroups;
import net.rafa.rustcraft.item.ModItems;
import net.rafa.rustcraft.networking.ThirstSaturationSyncS2CPayload;
import net.rafa.rustcraft.networking.ThirstSyncS2CPayload;
import net.rafa.rustcraft.util.IEntityDataSaver;
import net.rafa.rustcraft.util.ThirstData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RustCraft implements ModInitializer {
	public static final String MOD_ID = "rustcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        ModGroups.registerModItemGroups();
        ModBlocks.registerModBlocks();
        ModItems.registerModItems();

        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());

        PayloadTypeRegistry.playS2C().register(ThirstSyncS2CPayload.PAYLOAD_ID, ThirstSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ThirstSaturationSyncS2CPayload.PAYLOAD_ID, ThirstSaturationSyncS2CPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            int thirst = ((IEntityDataSaver) player).getPersistentData().getInt("thirst");
            ThirstData.syncThirst(thirst, (IEntityDataSaver) player);
        });
    }
}