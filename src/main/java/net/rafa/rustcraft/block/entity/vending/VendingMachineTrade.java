package net.rafa.rustcraft.block.entity.vending;


import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.RegistryKeys;

import java.util.List;

/**
 * Represents a single trade in the Vending Machine.
 * Define trades by creating instances of this class in VendingMachineBlockEntity.
 *
 * Example:
 *   new VendingMachineTrade(Items.IRON_INGOT, 5, Items.GOLD_INGOT, 1)
 *   → 5 Iron Ingots  for  1 Gold Ingot
 */
public record VendingMachineTrade(Item inputItem, int inputCount, Item outputItem, int outputCount) {

    /** Network codec for one trade (used when the screen opens). */
    public static final PacketCodec<RegistryByteBuf, VendingMachineTrade> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.registryValue(RegistryKeys.ITEM), VendingMachineTrade::inputItem,
            PacketCodecs.VAR_INT, VendingMachineTrade::inputCount,
            PacketCodecs.registryValue(RegistryKeys.ITEM), VendingMachineTrade::outputItem,
            PacketCodecs.VAR_INT, VendingMachineTrade::outputCount,
            VendingMachineTrade::new
    );

    /** Network codec for the whole trade list. */
    public static final PacketCodec<RegistryByteBuf, List<VendingMachineTrade>> LIST_PACKET_CODEC =
            PACKET_CODEC.collect(PacketCodecs.toList());

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
