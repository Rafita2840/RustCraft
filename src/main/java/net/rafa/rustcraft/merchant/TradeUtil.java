package net.rafa.rustcraft.merchant;

import net.minecraft.item.ItemStack;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.rafa.rustcraft.item.ModItems;

public class TradeUtil {
    public static TradeOfferList getResourcesTradeOffers() {
        TradeOfferList offers = new TradeOfferList();

        offers.add(new TradeOffer(
                new TradedItem(ModItems.SCRAP, 32),
                new ItemStack(ModItems.WOOD, 64),
                10,
                0,
                0.05f
        ));

        offers.add(new TradeOffer(
                new TradedItem(ModItems.SCRAP, 32),
                new ItemStack(ModItems.STONE, 32),
                10,
                0,
                0.05f
        ));

        return offers;
    }
}
