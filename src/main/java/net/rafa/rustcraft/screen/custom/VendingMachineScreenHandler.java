package net.rafa.rustcraft.screen.custom;


import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.rafa.rustcraft.block.entity.custom.VendingMachineBlockEntity;
import net.rafa.rustcraft.block.entity.vending.VendingMachineTrade;
import net.rafa.rustcraft.screen.ModScreenHandlers;

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

    // ── Client-side constructor (trades arrive via the screen-opening data) ─
    public VendingMachineScreenHandler(int syncId, PlayerInventory playerInv,
                                       List<VendingMachineTrade> trades) {
        super(ModScreenHandlers.VENDING_MACHINE_SCREEN_HANDLER, syncId);
        this.blockEntity = null;
        this.trades = trades;
        addPlayerSlots(playerInv);
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