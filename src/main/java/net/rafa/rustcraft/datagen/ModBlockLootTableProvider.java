package net.rafa.rustcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.rafa.rustcraft.block.ModBlocks;
import net.rafa.rustcraft.item.ModItems;

import java.util.concurrent.CompletableFuture;

/**
 * Block loot tables.
 *
 * Building blocks, buildable surfaces, the crate and the vending machine are unbreakable
 * (strength -1) for now, so they have no loot table. Add one here when they become breakable.
 */
public class ModBlockLootTableProvider extends FabricBlockLootTableProvider {

    public ModBlockLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        // Sulfur ore: itself with Silk Touch, otherwise 1-3 sulfur (+ Fortune)
        addDrop(ModBlocks.SULFUR_ORE, oreWithFortune(ModBlocks.SULFUR_ORE, ModItems.SULFUR, 1f, 3f));
    }

    private LootTable.Builder oreWithFortune(net.minecraft.block.Block ore, net.minecraft.item.Item drop, float min, float max) {
        RegistryWrapper.Impl<Enchantment> enchantments = this.registryLookup.getWrapperOrThrow(RegistryKeys.ENCHANTMENT);
        return dropsWithSilkTouch(ore, applyExplosionDecay(ore, ItemEntry.builder(drop)
                .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(min, max)))
                .apply(ApplyBonusLootFunction.uniformBonusCount(enchantments.getOrThrow(Enchantments.FORTUNE)))));
    }
}
