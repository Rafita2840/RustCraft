package net.rafa.rustcraft.screen.custom;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.rafa.rustcraft.block.entity.custom.VendingMachineBlockEntity;
import net.rafa.rustcraft.block.entity.vending.VendingMachineTrade;
import net.rafa.rustcraft.screen.ModScreenHandlers;

import java.util.ArrayList;
import java.util.List;

public class VendingMachineScreenHandler extends ScreenHandler {

    // GUI layout constants (shared with VendingMachineScreen)
    public static final int GUI_W        = 176;
    public static final int GUI_H        = 232;
    public static final int TRADE_AREA_Y = 18;
    public static final int TRADE_ROW_H  = 30;
    public static final int INPUT_X      = 8;
    public static final int OUTPUT_X     = 44;
    public static final int BTN_X        = 68;
    public static final int BTN_W        = 40;
    public static final int BTN_H        = 14;
    public static final int INV_X        = 8;
    public static final int INV_Y        = 152;
    public static final int HOTBAR_Y     = 210;

    private final VendingMachineBlockEntity blockEntity; // null on client
    private final List<VendingMachineTrade> trades;

    // ── Server-side constructor ────────────────────────────────────
    public VendingMachineScreenHandler(int syncId, PlayerInventory playerInv,
                                       VendingMachineBlockEntity be) {
        super(ModScreenHandlers.VENDING_MACHINE_SCREEN_HANDLER, syncId);
        this.blockEntity = be;
        this.trades = be.getTrades();
        addPlayerSlots(playerInv);
    }

    // ── Client-side constructor (receives trade data over network) ─
    public VendingMachineScreenHandler(int syncId, PlayerInventory playerInv,
                                       RegistryByteBuf buf) {
        super(ModScreenHandlers.VENDING_MACHINE_SCREEN_HANDLER, syncId);
        this.blockEntity = null;
        this.trades = readTrades(buf);
        addPlayerSlots(playerInv);
    }

    // ── Serialise trades into the open-screen packet ───────────────
    public static void writeTrades(RegistryByteBuf buf, List<VendingMachineTrade> trades) {
        buf.writeInt(trades.size());
        for (VendingMachineTrade t : trades) {
            buf.writeRegistryValue(net.minecraft.registry.Registries.ITEM, t.inputItem());
            buf.writeInt(t.inputCount());
            buf.writeRegistryValue(net.minecraft.registry.Registries.ITEM, t.outputItem());
            buf.writeInt(t.outputCount());
        }
    }

    private static List<VendingMachineTrade> readTrades(RegistryByteBuf buf) {
        int count = buf.readInt();
        List<VendingMachineTrade> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            net.minecraft.item.Item input  = buf.readRegistryValue(net.minecraft.registry.Registries.ITEM);
            int inputCount  = buf.readInt();
            net.minecraft.item.Item output = buf.readRegistryValue(net.minecraft.registry.Registries.ITEM);
            int outputCount = buf.readInt();
            if (input != null && output != null) {
                list.add(new VendingMachineTrade(input, inputCount, output, outputCount));
            }
        }
        return list;
    }

    // ── Player inventory slots ─────────────────────────────────────
    private void addPlayerSlots(PlayerInventory inv) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inv, col + row * 9 + 9,
                        INV_X + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inv, col, INV_X + col * 18, HOTBAR_Y));
        }
    }

    // ── Trade execution (server only) ──────────────────────────────
    public boolean executeTrade(PlayerEntity player, int tradeIndex) {
        if (blockEntity == null) return false;
        return blockEntity.executeTrade(player, tradeIndex);
    }

    public List<VendingMachineTrade> getTrades() { return trades; }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) { return ItemStack.EMPTY; }

    @Override
    public boolean canUse(PlayerEntity player) {
        return blockEntity == null || !blockEntity.isRemoved();
    }
}