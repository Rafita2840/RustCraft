package net.rafa.rustcraft.item;

import net.minecraft.item.ArmorItem;
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

    public static final Item SCRAP = registerItem("scrap", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item LOW_GRADE_FUEL = registerItem("low_grade_fuel", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item CLOTH = registerItem("cloth", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item SULFUR = registerItem("sulfur", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item ANIMAL_FAT = registerItem("animal_fat", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item STONE = registerItem("stone", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item WOOD = registerItem("wood", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item METAL = registerItem("metal", ModCategory.RESOURCES, new Item(new Item.Settings()));
    public static final Item HIGH_QUALITY_METAL = registerItem("high_quality_metal", ModCategory.RESOURCES, new Item(new Item.Settings()));

    public static final Item HAMMER = registerItem("hammer", ModCategory.TOOLS, new HammerItem(new Item.Settings().maxCount(1)));
    public static final Item BLUEPRINT_FLOOR = registerItem("blueprint_floor", ModCategory.TOOLS, new BlueprintFloorItem(new Item.Settings().maxCount(1)));
    public static final Item BLUEPRINT_STAIRS = registerItem("blueprint_stairs", null, new BlueprintStairsItem(new Item.Settings().maxCount(1)));
    public static final Item BLUEPRINT_WALL = registerItem("blueprint_wall", null, new BlueprintWallItem(new Item.Settings().maxCount(1)));

    public static final Item HAZMAT_SUIT_HELMET = registerItem("hazmat_suit_helmet", ModCategory.CLOTHING,
            new ArmorItem(ModArmorMaterials.HAZMAT_SUIT_ARMOR_MATERIAL, ArmorItem.Type.HELMET,
                    new Item.Settings()
                            .maxDamage(ArmorItem.Type.HELMET.getMaxDamage(20))
            ));
    public static final Item HAZMAT_SUIT_CHESTPLATE = registerItem("hazmat_suit_chestplate", ModCategory.CLOTHING,
            new ArmorItem(ModArmorMaterials.HAZMAT_SUIT_ARMOR_MATERIAL, ArmorItem.Type.CHESTPLATE,
                    new Item.Settings()
                            .maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(20))
            ));
    public static final Item HAZMAT_SUIT_LEGGINGS = registerItem("hazmat_suit_leggings", ModCategory.CLOTHING,
            new ArmorItem(ModArmorMaterials.HAZMAT_SUIT_ARMOR_MATERIAL, ArmorItem.Type.LEGGINGS,
                    new Item.Settings()
                            .maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(20))
            ));
    public static final Item HAZMAT_SUIT_BOOTS = registerItem("hazmat_suit_boots", ModCategory.CLOTHING,
            new ArmorItem(ModArmorMaterials.HAZMAT_SUIT_ARMOR_MATERIAL, ArmorItem.Type.BOOTS,
                    new Item.Settings()
                            .maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(20))
            ));
    
    


    /** Registers an item and lists it in the creative tab of its category. */
    private static Item registerItem(String name, ModCategory category, Item item) {
        Item registered = Registry.register(Registries.ITEM, Identifier.of(RustCraft.MOD_ID, name), item);
        if (category != null)
            category.add(registered);
        return registered;
    }

    public static void registerModItems() {
        RustCraft.LOGGER.info("Registering Mod Items for " + RustCraft.MOD_ID);
    }
}
