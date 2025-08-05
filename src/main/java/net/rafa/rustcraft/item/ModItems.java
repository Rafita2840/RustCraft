package net.rafa.rustcraft.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;

public class ModItems {

    public static final Item SCRAP = registerItem("scrap", new Item(new Item.Settings()));
    public static final Item LOW_GRADE_FUEL = registerItem("low_grade_fuel", new Item(new Item.Settings()));
    public static final Item CLOTH = registerItem("cloth", new Item(new Item.Settings()));
    public static final Item SULFUR = registerItem("sulfur", new Item(new Item.Settings()));
    public static final Item ANIMAL_FAT = registerItem("animal_fat", new Item(new Item.Settings()));



    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, Identifier.of(RustCraft.MOD_ID, name), item);
    }

    public static void registerModItems() {
        RustCraft.LOGGER.info("Registering Mod Items for " + RustCraft.MOD_ID);
    }
}
