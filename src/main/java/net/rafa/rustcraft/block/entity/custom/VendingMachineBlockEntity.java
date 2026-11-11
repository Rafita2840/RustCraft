package net.rafa.rustcraft.block.entity.custom;



import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.rafa.rustcraft.block.entity.ModBlockEntities;
import net.rafa.rustcraft.block.entity.vending.VendingMachineTrade;
import net.rafa.rustcraft.screen.custom.VendingMachineScreenHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Block entity for the Vending Machine.
 *
 * ─────────────────────────────────────────────────────────────
 *  HOW TO CUSTOMIZE TRADES
 * ─────────────────────────────────────────────────────────────
 *  Edit the buildTrades() method below.
 *  Each VendingMachineTrade takes:
 *    (inputItem, inputCount, outputItem, outputCount)
 *
 *  Example trades already included. Add/remove as needed.
 * ─────────────────────────────────────────────────────────────
 */
public class VendingMachineBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<List<VendingMachineTrade>> {

    // ── Inventory: slots for each trade's input stock ──────────────
    // One slot per trade to hold the input items stocked in the machine.
    // The screen handler manages the actual trade logic.
    private final List<VendingMachineTrade> trades;

    // Storage inventory: holds the items that players pay with (inputs collected)
    // and the items to dispense (outputs stocked). One pair of slots per trade.
    // Layout: [input_slot_0, output_slot_0, input_slot_1, output_slot_1, ...]
    private final DefaultedList<net.minecraft.item.ItemStack> inventory;

    public VendingMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VENDING_MACHINE, pos, state);
        this.trades = buildTrades();
        // 2 slots per trade (input collected + output stock)
        this.inventory = DefaultedList.ofSize(trades.size() * 2, net.minecraft.item.ItemStack.EMPTY);
    }

    // ══════════════════════════════════════════════════════════════
    //  ★  CUSTOMIZE YOUR TRADES HERE  ★
    // ══════════════════════════════════════════════════════════════
    private List<VendingMachineTrade> buildTrades() {
        List<VendingMachineTrade> list = new ArrayList<>();

        // Format: new VendingMachineTrade(inputItem, inputCount, outputItem, outputCount)
        list.add(new VendingMachineTrade(Items.IRON_INGOT,   5,  Items.GOLD_INGOT,    1));
        list.add(new VendingMachineTrade(Items.GOLD_INGOT,   3,  Items.DIAMOND,       1));
        list.add(new VendingMachineTrade(Items.EMERALD,      1,  Items.NETHERITE_SCRAP, 1));
        list.add(new VendingMachineTrade(Items.COPPER_INGOT, 8,  Items.IRON_INGOT,    1));

        // Add more trades here ↓
        // list.add(new VendingMachineTrade(Items.WHEAT, 10, Items.BREAD, 3));

        return list;
    }
    // ══════════════════════════════════════════════════════════════

    public List<VendingMachineTrade> getTrades() {
        return trades;
    }

    /** Returns the slot index that holds collected inputs for tradeIndex */
    public int getInputSlot(int tradeIndex) {
        return tradeIndex * 2;
    }

    /** Returns the slot index that holds output stock for tradeIndex */
    public int getOutputSlot(int tradeIndex) {
        return tradeIndex * 2 + 1;
    }

    public DefaultedList<net.minecraft.item.ItemStack> getInventory() {
        return inventory;
    }

    /**
     * Attempt to execute trade at the given index from a player.
     * Returns true if the trade succeeded.
     */
    public boolean executeTrade(PlayerEntity player, int idx) {
        if (idx < 0 || idx >= trades.size()) return false;
        VendingMachineTrade trade = trades.get(idx);
        PlayerInventory playerInv = player.getInventory();

        if (countItems(playerInv, trade.inputItem()) < trade.inputCount()) return false;

        int outSlot = idx * 2 + 1;
        ItemStack stock = inventory.get(outSlot);
        if (stock.isEmpty() || stock.getItem() != trade.outputItem()
                || stock.getCount() < trade.outputCount()) return false;

        removeItems(playerInv, trade.inputItem(), trade.inputCount());
        player.giveItemStack(trade.getOutputStack().copy());

        stock.decrement(trade.outputCount());
        if (stock.isEmpty()) inventory.set(outSlot, ItemStack.EMPTY);

        int inSlot = idx * 2;
        ItemStack collected = inventory.get(inSlot);
        if (collected.isEmpty()) {
            inventory.set(inSlot, new ItemStack(trade.inputItem(), trade.inputCount()));
        } else {
            collected.increment(trade.inputCount());
        }
        markDirty();
        return true;
    }

    private int countItems(PlayerInventory inv, Item item) {
        int n = 0;
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() == item) n += s.getCount();
        }
        return n;
    }

    private void removeItems(PlayerInventory inv, Item item, int amount) {
        int rem = amount;
        for (int i = 0; i < inv.size() && rem > 0; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() == item) {
                int take = Math.min(rem, s.getCount());
                s.decrement(take);
                rem -= take;
                if (s.isEmpty()) inv.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    // ── NBT Persistence ────────────────────────────────────────────

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        Inventories.writeNbt(nbt, inventory, registries);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        Inventories.readNbt(nbt, inventory, registries);
    }

    // ── Sync packet ────────────────────────────────────────────────

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    // ── Screen factory ─────────────────────────────────────────────

    @Override
    public List<VendingMachineTrade> getScreenOpeningData(ServerPlayerEntity player) {
        return trades;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.rustcraft.vending_machine");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new VendingMachineScreenHandler(syncId, playerInventory, this);
    }
}