package net.rafa.rustcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BlockTags;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.util.ModTags;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends FabricTagProvider.BlockTagProvider {

    public ModBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        // Vanilla tags
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE)
                .add(ModBlocks.SULFUR_ORE);
        getOrCreateTagBuilder(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.SULFUR_ORE);

        // Mod tags (not used by the code yet, the building refactor in P4.1 will use them)
        getOrCreateTagBuilder(ModTags.Blocks.CENTER_BUILDING_BLOCKS)
                .add(ModBlocks.WOODEN_BUILDING_BLOCK_CENTER)
                .add(ModBlocks.STONE_BUILDING_BLOCK_CENTER)
                .add(ModBlocks.METAL_BUILDING_BLOCK_CENTER)
                .add(ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER);

        getOrCreateTagBuilder(ModTags.Blocks.BUILDING_BLOCKS)
                .addTag(ModTags.Blocks.CENTER_BUILDING_BLOCKS)
                .add(ModBlocks.WOODEN_BUILDING_BLOCK)
                .add(ModBlocks.STONE_BUILDING_BLOCK)
                .add(ModBlocks.METAL_BUILDING_BLOCK)
                .add(ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK);

        getOrCreateTagBuilder(ModTags.Blocks.BUILDABLE_SURFACES)
                .add(ModBlocks.BUILDABLE_GRASS_BLOCK)
                .add(ModBlocks.BUILDABLE_SAND_BLOCK);
    }
}
