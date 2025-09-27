package net.rafa.rustcraft.screen.custom;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.TradeOutputSlot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.rafa.rustcraft.merchant.BaseVendingMachineMerchant;
import net.rafa.rustcraft.merchant.TradeUtil;
import net.rafa.rustcraft.screen.ModScreenHandlers;

public class ResourceVendingMachineScreenHandler extends ScreenHandler {

    private final Merchant merchant;
    private final MerchantInventory merchantInventory;

    public ResourceVendingMachineScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        this(syncId, playerInventory, playerInventory.player.getWorld().getBlockEntity(pos));
    }

    protected ResourceVendingMachineScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity) {
        super(ModScreenHandlers.RVM_SCREEN_HANDLER, syncId);
        this.merchant = new BaseVendingMachineMerchant((ServerPlayerEntity) playerInventory.player,
                TradeUtil.getResourcesTradeOffers()) ;
        this.merchantInventory = new MerchantInventory(merchant);
        this.addSlot(new Slot(this.merchantInventory, 0, 136, 37));
        this.addSlot(new Slot(this.merchantInventory, 1, 162, 37));
        this.addSlot(new TradeOutputSlot(playerInventory.player, merchant, this.merchantInventory, 2, 220, 37));

        for (int i = 0; i < 7; i++){

        }

        //player inventory
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        //player hotbar
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.merchant.getCustomer() == player;
    }
}
