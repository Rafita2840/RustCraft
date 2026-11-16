package net.rafa.rustcraft.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

/** Creative tabs, one per {@link ModCategory} that has entries. Must run after items and blocks are registered. */
public class ModGroups {

    public static void registerModItemGroups() {
        for (ModCategory category : ModCategory.values()) {
            if (category.isEmpty()) {
                continue;
            }
            Registry.register(Registries.ITEM_GROUP,
                    Identifier.of(RustCraft.MOD_ID, category.id()),
                    FabricItemGroup.builder()
                            .icon(() -> new ItemStack(category.icon()))
                            .displayName(Text.translatable(category.translationKey()))
                            .entries((displayContext, entries) -> category.entries().forEach(entries::add))
                            .build());
        }
        RustCraft.LOGGER.info("Registering Mod Groups for " + RustCraft.MOD_ID);
    }
}
