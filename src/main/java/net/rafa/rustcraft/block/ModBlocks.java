package net.rafa.rustcraft.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ExperienceDroppingBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.item.ModCategory;
import net.rafa.rustcraft.block.custom.CrateBlock;
import net.rafa.rustcraft.block.custom.DrinkableBlock;
import net.rafa.rustcraft.block.custom.VendingMachineBlock;

public class ModBlocks {

    public static final Block BUILDABLE_SAND_BLOCK = registerBlock("buildable_sand_block", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.SAND)
    ));

    public static final Block BUILDABLE_GRASS_BLOCK = registerBlock("buildable_grass_block", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.GRASS)
    ));

    public static final Block SULFUR_ORE = registerBlock("sulfur_ore", ModCategory.RESOURCES,
            new ExperienceDroppingBlock(UniformIntProvider.create(2, 5),
                    AbstractBlock.Settings.create()
                            .strength(4f)
                            .requiresTool()
                            .sounds(BlockSoundGroup.NETHER_GOLD_ORE)));

    public static final Block WOODEN_BUILDING_BLOCK = registerBlock("wooden_building_block", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.WOOD)
                    ));
    
    public static final Block WOODEN_BUILDING_BLOCK_CENTER = registerBlock("wooden_building_block_center", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.WOOD)
    ));

    public static final Block STONE_BUILDING_BLOCK = registerBlock("stone_building_block", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE)
    ));

    public static final Block STONE_BUILDING_BLOCK_CENTER = registerBlock("stone_building_block_center", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.STONE)
    ));

    public static final Block METAL_BUILDING_BLOCK = registerBlock("metal_building_block", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.CHAIN)
    ));

    public static final Block METAL_BUILDING_BLOCK_CENTER = registerBlock("metal_building_block_center", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.CHAIN)
    ));

    public static final Block HIGH_QUALITY_METAL_BUILDING_BLOCK = registerBlock("high_quality_metal_building_block", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.METAL)
    ));

    public static final Block HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER = registerBlock("high_quality_metal_building_block_center", ModCategory.BUILDING, new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .sounds(BlockSoundGroup.METAL)
    ));

    public static final Block VENDING_MACHINE = registerBlock("vending_machine", ModCategory.DEPLOYABLES, new VendingMachineBlock(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .nonOpaque()
    ));

    public static final Block DRINKABLE_BLOCK = registerBlock("drinkable_block", ModCategory.DEPLOYABLES, new DrinkableBlock(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
    ));

    public static final Block CRATE = registerBlock("crate", ModCategory.DEPLOYABLES, new CrateBlock(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    .nonOpaque()
    ));

    /** Registers a block and its item, and lists it in the creative tab of its category. */
    private static Block registerBlock(String name, ModCategory category, Block block) {
        registerBlockItem(name, block);
        category.add(block);
        return Registry.register(Registries.BLOCK, Identifier.of(RustCraft.MOD_ID, name), block);
    }

    private static void registerBlockItem(String name, Block block) {
        Registry.register(Registries.ITEM, Identifier.of(RustCraft.MOD_ID, name),
                new BlockItem(block, new Item.Settings()));
    }

    public static void registerModBlocks(){
        RustCraft.LOGGER.info("Registering Mod Blocks for " + RustCraft.MOD_ID);
    }

}
