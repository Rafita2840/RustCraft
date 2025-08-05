package net.rafa.rustcraft;

import net.fabricmc.api.ModInitializer;

import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.item.ModGroups;
import net.rafa.rustcraft.item.ModItems;
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
	}
}