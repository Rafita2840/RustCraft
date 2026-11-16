package net.rafa.rustcraft;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.block.entity.ModBlockEntities;
import net.rafa.rustcraft.data.ModAttachments;
import net.rafa.rustcraft.data.PlayerDataEvents;
import net.rafa.rustcraft.event.PlayerTickHandler;
import net.rafa.rustcraft.item.ModGroups;
import net.rafa.rustcraft.item.ModItems;
import net.rafa.rustcraft.networking.ModPayloads;
import net.rafa.rustcraft.screen.ModScreenHandlers;
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

        ModAttachments.init();
        PlayerDataEvents.register();
    }
}