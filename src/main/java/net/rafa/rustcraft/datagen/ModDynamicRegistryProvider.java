package net.rafa.rustcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.rafa.rustcraft.damage.ModDamageTypes;

import java.util.concurrent.CompletableFuture;

/**
 * Data-driven registry entries (damage types now; biomes and worldgen features later).
 * The entries themselves are built in {@code RustCraftDataGenerator.buildRegistry}.
 */
public class ModDynamicRegistryProvider extends FabricDynamicRegistryProvider {

    public ModDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
        RegistryWrapper.Impl<net.minecraft.entity.damage.DamageType> damageTypes =
                registries.getWrapperOrThrow(RegistryKeys.DAMAGE_TYPE);
        entries.add(damageTypes, ModDamageTypes.DEHYDRATION);
    }

    @Override
    public String getName() {
        return "RustCraft dynamic registries";
    }
}
