package net.rafa.rustcraft.merchant;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;
import net.minecraft.village.TradeOffer;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.village.TradeOfferList;
import org.jetbrains.annotations.Nullable;

public class BaseVendingMachineMerchant implements Merchant {

    private final ServerPlayerEntity customer;
    private final MerchantInventory inventory;
    private final TradeOfferList offers;

    public BaseVendingMachineMerchant(ServerPlayerEntity player, TradeOfferList offers) {
        this.customer = player;
        this.inventory = new MerchantInventory(this);
        this.offers = offers;
    }

    @Override
    public void setCustomer(@Nullable PlayerEntity player) {

    }

    @Override
    public PlayerEntity getCustomer() {
        return customer;
    }

    public MerchantInventory getInventory() {
        return inventory;

    }

    @Override
    public TradeOfferList getOffers() {
        return offers;
    }

    @Override
    public void setOffersFromServer(TradeOfferList offers) {
        this.offers.clear();
        this.offers.addAll(offers);
    }

    @Override
    public void trade(TradeOffer offer) {

    }

    @Override
    public void onSellingItem(ItemStack stack) {

    }

    @Override

    public int getExperience() {
        return 0;
    }

    @Override
    public void setExperienceFromServer(int experience) {

    }

    @Override
    public boolean isLeveledMerchant() {
        return false;
    }

    @Override
    public SoundEvent getYesSound() {
        return null;
    }

    public int getLevelProgress() {
        return 0;
    }

    public void setLevelProgress(int progress) {
    }

    @Override
    public void sendOffers(PlayerEntity player, Text text, int levelProgress) {
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, inv, p) -> new MerchantScreenHandler(syncId, inv, this),
                text
        ));
    }

    @Override
    public boolean isClient() {
        return false;
    }
}
