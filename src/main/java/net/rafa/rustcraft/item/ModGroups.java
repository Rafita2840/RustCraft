package net.rafa.rustcraft.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.block.ModBlocks;

public class ModGroups {

    public static final ItemGroup RUSTCRAFT_ITEMS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(RustCraft.MOD_ID, "rustcraft_items"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModItems.SCRAP))
                    .displayName(Text.translatable("group.rustcraft.rustcraft_items"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModItems.SCRAP);
                        entries.add(ModItems.CLOTH);
                        entries.add(ModItems.LOW_GRADE_FUEL);
                        entries.add(ModItems.SULFUR);
                        entries.add(ModItems.ANIMAL_FAT);
                        entries.add(ModItems.HAMMER);
                        entries.add(ModItems.BLUEPRINT_FLOOR);
                        entries.add(ModItems.WOOD);
                        entries.add(ModItems.STONE);
                        entries.add(ModItems.METAL);
                        entries.add(ModItems.HIGH_QUALITY_METAL);
                    }).build());

    public static final ItemGroup RUSTCRAFT_BLOCKS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(RustCraft.MOD_ID, "rustcraft_blocks"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.SULFUR_ORE))
                    .displayName(Text.translatable("group.rustcraft.rustcraft_blocks"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.SULFUR_ORE);
                        entries.add(ModBlocks.WOODEN_BUILDING_BLOCK);
                        entries.add(ModBlocks.WOODEN_BUILDING_BLOCK_CENTER);
                        entries.add(ModBlocks.STONE_BUILDING_BLOCK);
                        entries.add(ModBlocks.STONE_BUILDING_BLOCK_CENTER);
                        entries.add(ModBlocks.METAL_BUILDING_BLOCK);
                        entries.add(ModBlocks.METAL_BUILDING_BLOCK_CENTER);
                        entries.add(ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK);
                        entries.add(ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER);
                        entries.add(ModBlocks.RESOURCE_VENDING_MACHINE_BLOCK);
                        entries.add(ModBlocks.BUILDABLE_GRASS_BLOCK);
                        entries.add(ModBlocks.BUILDABLE_SAND_BLOCK);
                        entries.add(ModBlocks.DRINKABLE_BLOCK);
                    }).build());

    public static void registerModItemGroups(){
        RustCraft.LOGGER.info("Registering Mod Groups for " + RustCraft.MOD_ID);
    }
}
