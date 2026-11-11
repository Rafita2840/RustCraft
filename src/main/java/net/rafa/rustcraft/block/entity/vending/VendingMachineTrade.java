package net.rafa.rustcraft.block.entity.vending;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Represents a single trade in the Vending Machine.
 * Define trades by creating instances of this class in VendingMachineBlockEntity.
 *
 * Example:
 *   new VendingMachineTrade(Items.IRON_INGOT, 5, Items.GOLD_INGOT, 1)
 *   → 5 Iron Ingots  for  1 Gold Ingot
 */
public record VendingMachineTrade(Item inputItem, int inputCount, Item outputItem, int outputCount) {

    /** Convenience constructor for 1-to-1 trades */
    public VendingMachineTrade(Item inputItem, Item outputItem) {
        this(inputItem, 1, outputItem, 1);
    }

    public ItemStack getInputStack() {
        return new ItemStack(inputItem, inputCount);
    }

    public ItemStack getOutputStack() {
        return new ItemStack(outputItem, outputCount);
    }
}
