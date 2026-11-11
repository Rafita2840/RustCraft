package net.rafa.rustcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.block.Block;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateVariant;
import net.minecraft.data.client.BlockStateVariantMap;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.data.client.VariantSettings;
import net.minecraft.data.client.VariantsBlockStateSupplier;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.item.ModItems;

/**
 * Generates blockstates and models.
 *
 * Block item models are created automatically (they point at the block model).
 */
public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
        // Plain cubes: texture block/<name>.png
        gen.registerSimpleCubeAll(ModBlocks.SULFUR_ORE);
        gen.registerSimpleCubeAll(ModBlocks.BUILDABLE_GRASS_BLOCK);
        gen.registerSimpleCubeAll(ModBlocks.BUILDABLE_SAND_BLOCK);
        gen.registerSimpleCubeAll(ModBlocks.DRINKABLE_BLOCK);

        // Building blocks: <name>_side.png on the sides, <name>_top.png on top and bottom
        buildingBlock(gen, ModBlocks.WOODEN_BUILDING_BLOCK, "wooden_building_block");
        buildingBlock(gen, ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, "wooden_building_block_center");
        buildingBlock(gen, ModBlocks.STONE_BUILDING_BLOCK, "stone_building_block");
        buildingBlock(gen, ModBlocks.STONE_BUILDING_BLOCK_CENTER, "stone_building_block_center");
        buildingBlock(gen, ModBlocks.METAL_BUILDING_BLOCK, "metal_building_block");
        buildingBlock(gen, ModBlocks.METAL_BUILDING_BLOCK_CENTER, "metal_building_block_center");
        buildingBlock(gen, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK, "high_quality_metal_building_block");
        buildingBlock(gen, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER, "high_quality_metal_building_block_center");

        // Blockbench models stay hand-made; only their blockstates are generated here.
        // Their item models are created automatically (parent = the block model).
        gen.blockStateCollector.accept(
                BlockStateModelGenerator.createSingletonBlockState(ModBlocks.CRATE,
                        Identifier.of(RustCraft.MOD_ID, "block/crate")));
        vendingMachine(gen);
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        // Resources
        gen.register(ModItems.SCRAP, Models.GENERATED);
        gen.register(ModItems.LOW_GRADE_FUEL, Models.GENERATED);
        gen.register(ModItems.CLOTH, Models.GENERATED);
        gen.register(ModItems.SULFUR, Models.GENERATED);
        gen.register(ModItems.ANIMAL_FAT, Models.GENERATED);
        gen.register(ModItems.STONE, Models.GENERATED);
        gen.register(ModItems.WOOD, Models.GENERATED);
        gen.register(ModItems.METAL, Models.GENERATED);
        gen.register(ModItems.HIGH_QUALITY_METAL, Models.GENERATED);

        // Tools and building
        gen.register(ModItems.HAMMER, Models.HANDHELD);
        gen.register(ModItems.BLUEPRINT_FLOOR, Models.GENERATED);
        gen.register(ModItems.BLUEPRINT_STAIRS, Models.GENERATED);
        gen.register(ModItems.BLUEPRINT_WALL, Models.GENERATED);

        // Hazmat suit (textures are still missing, see STATUS.md #15)
        gen.register(ModItems.HAZMAT_SUIT_HELMET, Models.GENERATED);
        gen.register(ModItems.HAZMAT_SUIT_CHESTPLATE, Models.GENERATED);
        gen.register(ModItems.HAZMAT_SUIT_LEGGINGS, Models.GENERATED);
        gen.register(ModItems.HAZMAT_SUIT_BOOTS, Models.GENERATED);
    }

    private static void buildingBlock(BlockStateModelGenerator gen, Block block, String name) {
        TextureMap textures = TextureMap.sideEnd(
                Identifier.of(RustCraft.MOD_ID, "block/" + name + "_side"),
                Identifier.of(RustCraft.MOD_ID, "block/" + name + "_top"));
        gen.registerSingleton(block, textures, Models.CUBE_COLUMN);
    }

    /** Two-block-tall, rotatable vending machine: one model per half, rotated by facing. */
    private static void vendingMachine(BlockStateModelGenerator gen) {
        Identifier lower = Identifier.of(RustCraft.MOD_ID, "block/vending_machine_lower");
        Identifier upper = Identifier.of(RustCraft.MOD_ID, "block/vending_machine_upper");

        BlockStateVariantMap.DoubleProperty<Direction, DoubleBlockHalf> variants =
                BlockStateVariantMap.create(Properties.HORIZONTAL_FACING, Properties.DOUBLE_BLOCK_HALF);

        for (Direction facing : Direction.Type.HORIZONTAL) {
            VariantSettings.Rotation rotation = switch (facing) {
                case EAST -> VariantSettings.Rotation.R90;
                case SOUTH -> VariantSettings.Rotation.R180;
                case WEST -> VariantSettings.Rotation.R270;
                default -> VariantSettings.Rotation.R0;
            };
            variants.register(facing, DoubleBlockHalf.LOWER, variant(lower, rotation));
            variants.register(facing, DoubleBlockHalf.UPPER, variant(upper, rotation));
        }

        gen.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(ModBlocks.VENDING_MACHINE).coordinate(variants));
    }

    private static BlockStateVariant variant(Identifier model, VariantSettings.Rotation rotation) {
        BlockStateVariant variant = BlockStateVariant.create().put(VariantSettings.MODEL, model);
        if (rotation != VariantSettings.Rotation.R0) {
            variant.put(VariantSettings.Y, rotation);
        }
        return variant;
    }
}
