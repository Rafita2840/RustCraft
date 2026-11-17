package net.rafa.rustcraft.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.DamageTypeTags;
import net.rafa.rustcraft.damage.ModDamageTypes;
import net.rafa.rustcraft.util.ModTags;

import java.util.concurrent.CompletableFuture;

/** Damage type tags. Dehydration gets the same tags as vanilla starvation (armor, effects, knockback). */
public class ModDamageTypeTagProvider extends FabricTagProvider<DamageType> {

    public ModDamageTypeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.DAMAGE_TYPE, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup lookup) {
        // Armor and Resistance don't help against thirst
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.DEHYDRATION);
        getOrCreateTagBuilder(DamageTypeTags.BYPASSES_EFFECTS).add(ModDamageTypes.DEHYDRATION);
        // Damage without an attacker still pushes the player a little in 1.21 unless the type is in this tag.
        // Vanilla starvation, drowning, fire and fall damage are in it. Every over-time / environmental
        // damage type of the mod (radiation, bleeding...) must be added here too.
        getOrCreateTagBuilder(ModTags.DamageTypes.NO_KNOCKBACK).add(ModDamageTypes.DEHYDRATION);
    }
}
