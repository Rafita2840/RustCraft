package net.rafa.rustcraft;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKeys;
import net.rafa.rustcraft.damage.ModDamageTypes;
import net.rafa.rustcraft.datagen.ModBlockLootTableProvider;
import net.rafa.rustcraft.datagen.ModBlockTagProvider;
import net.rafa.rustcraft.datagen.ModDamageTypeTagProvider;
import net.rafa.rustcraft.datagen.ModDynamicRegistryProvider;
import net.rafa.rustcraft.datagen.ModLangProvider;
import net.rafa.rustcraft.datagen.ModModelProvider;
import net.rafa.rustcraft.datagen.ModRecipeProvider;

public class RustCraftDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(ModModelProvider::new);
		pack.addProvider(ModLangProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModDynamicRegistryProvider::new);
		pack.addProvider(ModDamageTypeTagProvider::new);
	}

	/** Entries of data-driven registries, written to JSON by ModDynamicRegistryProvider. */
	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE, ModDamageTypes::bootstrap);
	}
}
