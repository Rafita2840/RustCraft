package net.rafa.rustcraft.item;

import net.minecraft.item.ItemConvertible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creative tab themes. Every item and block is registered with a category, and the creative
 * tabs are filled from these lists, so a new item shows up in the right tab without touching ModGroups.
 * Categories without any entry get no tab.
 */
public enum ModCategory {
    RESOURCES("resources", "Resources"),
    COMPONENTS("components", "Components"),
    TOOLS("tools_weapons", "Tools & Weapons"),
    CLOTHING("clothing", "Clothing"),
    BUILDING("building", "Building"),
    MEDICAL_FOOD("medical_food", "Medical & Food"),
    DEPLOYABLES("deployables", "Deployables");

    private final String id;
    private final String displayName;
    private final List<ItemConvertible> entries = new ArrayList<>();

    ModCategory(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    /** Path of the creative tab id (rustcraft:<id>). */
    public String id() { return id; }

    /** English name, used by the lang datagen. */
    public String displayName() { return displayName; }

    public String translationKey() { return "group.rustcraft." + id; }

    public void add(ItemConvertible entry) { entries.add(entry); }

    public List<ItemConvertible> entries() { return Collections.unmodifiableList(entries); }

    public boolean isEmpty() { return entries.isEmpty(); }

    /** The first registered entry is used as the tab icon. */
    public ItemConvertible icon() { return entries.getFirst(); }
}
