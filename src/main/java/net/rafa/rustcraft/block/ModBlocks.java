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

public class ModBlocks {

    public static final Block SULFUR_ORE = registerBlock("sulfur_ore",
            new ExperienceDroppingBlock(UniformIntProvider.create(2, 5),
                    AbstractBlock.Settings.create()
                            .strength(4f)
                            .requiresTool()
                            .sounds(BlockSoundGroup.NETHER_GOLD_ORE)));

    public static final Block WOODEN_BUILDING_BLOCK = registerBlock("wooden_building_block", new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
                    ));

    public static final Block WOODEN_BUILDING_BLOCK_CENTER = registerBlock("wooden_building_block_center", new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
    ));

    public static final Block STONE_BUILDING_BLOCK = registerBlock("stone_building_block", new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
    ));

    public static final Block STONE_BUILDING_BLOCK_CENTER = registerBlock("stone_building_block_center", new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
    ));

    public static final Block METAL_BUILDING_BLOCK = registerBlock("metal_building_block", new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
    ));

    public static final Block METAL_BUILDING_BLOCK_CENTER = registerBlock("metal_building_block_center", new Block(
            AbstractBlock.Settings.create()
                    .strength(-1)
                    .requiresTool()
    ));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
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
