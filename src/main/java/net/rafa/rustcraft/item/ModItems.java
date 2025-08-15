package net.rafa.rustcraft.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.item.custom.BlueprintFloorItem;
import net.rafa.rustcraft.item.custom.BlueprintStairsItem;
import net.rafa.rustcraft.item.custom.BlueprintWallItem;
import net.rafa.rustcraft.item.custom.HammerItem;

public class ModItems {

    public static final Item SCRAP = registerItem("scrap", new Item(new Item.Settings()));
    public static final Item LOW_GRADE_FUEL = registerItem("low_grade_fuel", new Item(new Item.Settings()));
    public static final Item CLOTH = registerItem("cloth", new Item(new Item.Settings()));
    public static final Item SULFUR = registerItem("sulfur", new Item(new Item.Settings()));
    public static final Item ANIMAL_FAT = registerItem("animal_fat", new Item(new Item.Settings()));
    public static final Item STONE = registerItem("stone", new Item(new Item.Settings()));
    public static final Item WOOD = registerItem("wood", new Item(new Item.Settings()));

    public static final Item HAMMER = registerItem("hammer", new HammerItem(new Item.Settings().maxCount(1)));
    public static final Item BLUEPRINT_FLOOR = registerItem("blueprint_floor", new BlueprintFloorItem(new Item.Settings().maxCount(1)));
    public static final Item BLUEPRINT_STAIRS = registerItem("blueprint_stairs", new BlueprintStairsItem(new Item.Settings().maxCount(1)));
    public static final Item BLUEPRINT_WALL = registerItem("blueprint_wall", new BlueprintWallItem(new Item.Settings().maxCount(1)));



    private static Item registerItem(String name, Item item){
        return Registry.register(Registries.ITEM, Identifier.of(RustCraft.MOD_ID, name), item);
    }

    public static void registerModItems() {
        RustCraft.LOGGER.info("Registering Mod Items for " + RustCraft.MOD_ID);
    }
}
