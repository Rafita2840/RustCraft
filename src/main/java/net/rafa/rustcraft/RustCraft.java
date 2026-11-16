package net.rafa.rustcraft;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.block.entity.ModBlockEntities;
import net.rafa.rustcraft.event.PlayerTickHandler;
import net.rafa.rustcraft.item.ModGroups;
import net.rafa.rustcraft.item.ModItems;
import net.rafa.rustcraft.networking.ModPayloads;
import net.rafa.rustcraft.screen.ModScreenHandlers;
import net.rafa.rustcraft.util.IEntityDataSaver;
import net.rafa.rustcraft.util.ThirstData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RustCraft implements ModInitializer {
	public static final String MOD_ID = "rustcraft";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
        // Items and blocks first: the creative tabs are filled from what they registered
        ModItems.registerModItems();
        ModBlocks.registerModBlocks();
        ModGroups.registerModItemGroups();
        ModBlockEntities.registerModBlockEntities();
        ModScreenHandlers.registerScreenHandlers();

        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());

        ModPayloads.register();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            int thirst = ((IEntityDataSaver) player).getPersistentData().getInt("thirst");
            ThirstData.syncThirst(thirst, (IEntityDataSaver) player);
        });
    }
}