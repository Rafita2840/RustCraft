package net.rafa.rustcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;
import net.rafa.rustcraft.item.ModItems;

import java.util.concurrent.CompletableFuture;

/** Crafting recipes. All future recipes go here. */
public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate(RecipeExporter exporter) {
        // 3 animal fat + 1 cloth -> 2 low grade fuel
        ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, ModItems.LOW_GRADE_FUEL, 2)
                .input(ModItems.ANIMAL_FAT, 3)
                .input(ModItems.CLOTH)
                .criterion(hasItem(ModItems.ANIMAL_FAT), conditionsFromItem(ModItems.ANIMAL_FAT))
                .offerTo(exporter);
    }
}
