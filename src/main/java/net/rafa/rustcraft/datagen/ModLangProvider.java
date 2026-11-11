package net.rafa.rustcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.item.ModItems;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * English translations. The build fails if a block or item of this mod has no name,
 * so a missing translation can't slip through.
 */
public class ModLangProvider extends FabricLanguageProvider {

    public ModLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder builder) {
        Set<String> added = new HashSet<>();

        // ── Items ──────────────────────────────────────────────
        item(builder, added, ModItems.SCRAP, "Scrap");
        item(builder, added, ModItems.CLOTH, "Cloth");
        item(builder, added, ModItems.LOW_GRADE_FUEL, "Low Grade Fuel");
        item(builder, added, ModItems.SULFUR, "Sulfur");
        item(builder, added, ModItems.ANIMAL_FAT, "Animal Fat");
        item(builder, added, ModItems.STONE, "Stone");
        item(builder, added, ModItems.WOOD, "Wood");
        item(builder, added, ModItems.METAL, "Metal");
        item(builder, added, ModItems.HIGH_QUALITY_METAL, "High Quality Metal");

        item(builder, added, ModItems.HAMMER, "Hammer");
        item(builder, added, ModItems.BLUEPRINT_FLOOR, "Blueprint (Floor)");
        item(builder, added, ModItems.BLUEPRINT_STAIRS, "Blueprint (Stairs)");
        item(builder, added, ModItems.BLUEPRINT_WALL, "Blueprint (Wall)");

        item(builder, added, ModItems.HAZMAT_SUIT_BOOTS, "Hazmat Suit Boots");
        item(builder, added, ModItems.HAZMAT_SUIT_LEGGINGS, "Hazmat Suit Leggings");
        item(builder, added, ModItems.HAZMAT_SUIT_CHESTPLATE, "Hazmat Suit Chestplate");
        item(builder, added, ModItems.HAZMAT_SUIT_HELMET, "Hazmat Suit Helmet");

        // ── Blocks ─────────────────────────────────────────────
        block(builder, added, ModBlocks.BUILDABLE_GRASS_BLOCK, "Buildable Grass Block");
        block(builder, added, ModBlocks.BUILDABLE_SAND_BLOCK, "Buildable Sand Block");
        block(builder, added, ModBlocks.SULFUR_ORE, "Sulfur Ore");
        block(builder, added, ModBlocks.DRINKABLE_BLOCK, "Drinkable Block");
        block(builder, added, ModBlocks.VENDING_MACHINE, "Vending Machine");
        block(builder, added, ModBlocks.CRATE, "Crate");

        block(builder, added, ModBlocks.WOODEN_BUILDING_BLOCK_CENTER, "Center Wooden Building Block");
        block(builder, added, ModBlocks.WOODEN_BUILDING_BLOCK, "Wooden Building Block");
        block(builder, added, ModBlocks.STONE_BUILDING_BLOCK_CENTER, "Center Stone Building Block");
        block(builder, added, ModBlocks.STONE_BUILDING_BLOCK, "Stone Building Block");
        block(builder, added, ModBlocks.METAL_BUILDING_BLOCK_CENTER, "Center Metal Building Block");
        block(builder, added, ModBlocks.METAL_BUILDING_BLOCK, "Metal Building Block");
        block(builder, added, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK_CENTER, "Center High Quality Metal Building Block");
        block(builder, added, ModBlocks.HIGH_QUALITY_METAL_BUILDING_BLOCK, "High Quality Metal Building Block");

        // ── Creative tabs ──────────────────────────────────────
        builder.add("group.rustcraft.rustcraft_items", "RustCraft Items");
        builder.add("group.rustcraft.rustcraft_blocks", "RustCraft Blocks");

        // ── Messages (coloured in code via ModText) ────────────
        builder.add("text.rustcraft.cant_use", "Can't use!");
        builder.add("text.rustcraft.cant_place_wall", "Can't place wall in this position!");
        builder.add("text.rustcraft.cant_place_stairs", "Can't place stairs in this position!");
        builder.add("text.rustcraft.cant_remove", "Can't remove building!");
        builder.add("text.rustcraft.non_buildable_surface", "Non buildable surface!");
        builder.add("text.rustcraft.off_grid", "Off grid!");
        builder.add("text.rustcraft.not_enough_resources", "Not enough resources!");
        builder.add("text.rustcraft.max_out", "Maxed out building!");

        // ── Tooltips (coloured in code via ModText) ────────────
        builder.add("tooltip.rustcraft.blueprint.control_down1", "Sneak + R-Click:");
        builder.add("tooltip.rustcraft.blueprint.control_down2", " Switches blueprints.");

        builder.add("tooltip.rustcraft.blueprint_floor.shift_down", "Builds floors.");
        builder.add("tooltip.rustcraft.blueprint_floor.control_down1", "R-Click on a buildable surface:");
        builder.add("tooltip.rustcraft.blueprint_floor.control_down2", " Builds floor, requires Wood.");

        builder.add("tooltip.rustcraft.hammer.shift_down", "Upgrades buildings and removes them.");
        builder.add("tooltip.rustcraft.hammer.control_down1", "R-Click on a Center Building Block:");
        builder.add("tooltip.rustcraft.hammer.control_down2", " Upgrades the building, requires materials.");
        builder.add("tooltip.rustcraft.hammer.control_down3", " If Sneaking removes wooden buildings.");

        builder.add("tooltip.rustcraft.tool.hold_shift", "Hold %s for information");
        builder.add("tooltip.rustcraft.tool.hold_control", "Hold %s for controls");

        // ── GUI ────────────────────────────────────────────────
        builder.add("gui.rustcraft.vending_machine.buy", "BUY");
        builder.add("gui.rustcraft.vending_machine.out_of_stock", "Out of stock");
        builder.add("gui.rustcraft.vending_machine.insufficient_funds", "Not enough items");

        verifyAllNamed(added);
    }

    private static void item(TranslationBuilder builder, Set<String> added, Item item, String name) {
        builder.add(item, name);
        added.add(item.getTranslationKey());
    }

    private static void block(TranslationBuilder builder, Set<String> added, Block block, String name) {
        builder.add(block, name);
        added.add(block.getTranslationKey());
    }

    /** Fails the datagen run if any item or block of this mod has no translation. */
    private static void verifyAllNamed(Set<String> added) {
        for (Item item : Registries.ITEM) {
            if (Registries.ITEM.getId(item).getNamespace().equals(RustCraft.MOD_ID)
                    && !added.contains(item.getTranslationKey())) {
                throw new IllegalStateException("Missing translation for item " + Registries.ITEM.getId(item));
            }
        }
        for (Block block : Registries.BLOCK) {
            if (Registries.BLOCK.getId(block).getNamespace().equals(RustCraft.MOD_ID)
                    && !added.contains(block.getTranslationKey())) {
                throw new IllegalStateException("Missing translation for block " + Registries.BLOCK.getId(block));
            }
        }
    }
}
