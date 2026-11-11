package net.rafa.rustcraft.screen.custom;



import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.block.entity.vending.VendingMachineTrade;
import net.rafa.rustcraft.networking.VendingTradePacket;

import java.util.List;

/**
 * Client-side GUI for the Vending Machine.
 *
 * GUI texture layout (176 × 232 px):
 *
 *  y=0   ┌──────────────────────────────┐  ← panel top
 *  y=18  │  [slot]  [slot]  [BUY BTN]  │  trade 0   (30px tall each)
 *  y=48  │  [slot]  [slot]  [BUY BTN]  │  trade 1
 *  y=78  │  [slot]  [slot]  [BUY BTN]  │  trade 2
 *  y=108 │  [slot]  [slot]  [BUY BTN]  │  trade 3
 *  y=140 ├──────────────────────────────┤  ← divider
 *  y=152 │  player inventory  9×3       │
 *  y=210 │  hotbar            9×1       │
 *  y=232 └──────────────────────────────┘
 */
public class VendingMachineScreen extends HandledScreen<VendingMachineScreenHandler> {


    private static final Identifier TEXTURE =
            Identifier.of(RustCraft.MOD_ID, "textures/gui/vending_machine.png");

    private static final int TRADE_AREA_Y = VendingMachineScreenHandler.TRADE_AREA_Y;
    private static final int TRADE_ROW_H  = VendingMachineScreenHandler.TRADE_ROW_H;
    private static final int INPUT_X      = VendingMachineScreenHandler.INPUT_X;
    private static final int OUTPUT_X     = VendingMachineScreenHandler.OUTPUT_X;
    private static final int BTN_X        = VendingMachineScreenHandler.BTN_X;
    private static final int BTN_W        = VendingMachineScreenHandler.BTN_W;
    private static final int BTN_H        = VendingMachineScreenHandler.BTN_H;

    private final List<VendingMachineTrade> trades;

    public VendingMachineScreen(VendingMachineScreenHandler handler,
                                PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.trades             = handler.getTrades();
        this.backgroundWidth    = VendingMachineScreenHandler.GUI_W;
        this.backgroundHeight   = VendingMachineScreenHandler.GUI_H;
        this.playerInventoryTitleY = 140;
    }

    // ── Background (texture) ───────────────────────────────────────
    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        ctx.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);
    }

    // ── Foreground (items + button labels, drawn OVER texture) ─────
    @Override
    protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
        // Title
        ctx.drawText(textRenderer, title, 8, 6, 0x404040, false);
        // "Inventory" label
        ctx.drawText(textRenderer, playerInventoryTitle, 8, playerInventoryTitleY, 0x404040, false);

        for (int i = 0; i < trades.size(); i++) {
            VendingMachineTrade trade = trades.get(i);
            int rowY = TRADE_AREA_Y + i * TRADE_ROW_H;

            // Input item  (slot inner area starts 1px inside the slot border)
            ctx.drawItem(trade.getInputStack(), INPUT_X + 1, rowY + 7);
            ctx.drawItemInSlot(textRenderer, trade.getInputStack(), INPUT_X + 1, rowY + 7);

            // Output item
            ctx.drawItem(trade.getOutputStack(), OUTPUT_X + 1, rowY + 7);
            ctx.drawItemInSlot(textRenderer, trade.getOutputStack(), OUTPUT_X + 1, rowY + 7);

            // BUY button label
            int btnY       = rowY + (TRADE_ROW_H - BTN_H) / 2;
            boolean hovered = isOverBtn(mouseX - x, mouseY - y, i);
            String  label   = Text.translatable("gui.rustcraft.vending_machine.buy").getString();
            int     lx      = BTN_X + (BTN_W - textRenderer.getWidth(label)) / 2;
            ctx.drawText(textRenderer, label, lx, btnY + 3, hovered ? 0xFFFFA0 : 0xFFFFFF, false);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
    }

    // ── Click detection ────────────────────────────────────────────
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Check BUY buttons BEFORE passing to super (which handles slot clicks)
        int relX = (int) mouseX - x;
        int relY = (int) mouseY - y;
        for (int i = 0; i < trades.size(); i++) {
            if (isOverBtn(relX, relY, i)) {
                VendingTradePacket.send(i);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isOverBtn(int relX, int relY, int idx) {
        int rowY = TRADE_AREA_Y + idx * TRADE_ROW_H;
        int btnY = rowY + (TRADE_ROW_H - BTN_H) / 2;
        return relX >= BTN_X && relX < BTN_X + BTN_W
                && relY >= btnY  && relY < btnY  + BTN_H;
    }
}





/*

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.impl.registry.sync.packet.DirectRegistryPacketHandler;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.networking.VendingMachinePackets;
import net.rafa.rustcraft.trade.VendingMachineTrade;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class VendingMachineScreen extends HandledScreen<VendingMachineScreenHandler> {
    private static final Identifier TEXTURE = Identifier.of(RustCraft.MOD_ID, "textures/gui/vending_machine_screen.png");
    private static final int TRADE_LIST_X = 5;
    private static final int TRADE_LIST_Y = 16;
    private static final int TRADE_BUTTON_WIDTH = 99;
    private static final int TRADE_BUTTON_HEIGHT = 20;
    
    private int scrollOffset = 0;

    public VendingMachineScreen(VendingMachineScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        this.backgroundWidth = 276;
        this.backgroundHeight = 166;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        this.titleX = (this.backgroundWidth - this.textRenderer.getWidth(this.title)) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        
        context.drawTexture(TEXTURE, x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        
        // Draw trades list
        renderTradesList(context, mouseX, mouseY, x, y);
    }

    private void renderTradesList(DrawContext context, int mouseX, int mouseY, int guiX, int guiY) {
        int tradeListX = guiX + TRADE_LIST_X;
        int tradeListY = guiY + TRADE_LIST_Y;
        
        var trades = this.handler.getTrades();
        int selectedIndex = this.handler.getSelectedTradeIndex();
        
        for (int i = 0; i < Math.min(7, trades.size() - scrollOffset); i++) {
            int tradeIndex = i + scrollOffset;
            VendingMachineTrade trade = trades.get(tradeIndex);
            
            int buttonX = tradeListX;
            int buttonY = tradeListY + i * TRADE_BUTTON_HEIGHT;
            
            // Determine button state
            boolean isSelected = tradeIndex == selectedIndex;
            boolean isHovered = mouseX >= buttonX && mouseX < buttonX + TRADE_BUTTON_WIDTH &&
                               mouseY >= buttonY && mouseY < buttonY + TRADE_BUTTON_HEIGHT;
            boolean isDisabled = trade.isDisabled();
            
            // Draw button background
            int textureY = 166; // Default
            if (isDisabled) {
                textureY = 166 + 40; // Disabled state
            } else if (isSelected) {
                textureY = 166 + 20; // Selected state
            } else if (isHovered) {
                textureY = 166 + 60; // Hovered state
            }
            
            context.drawTexture(TEXTURE, buttonX, buttonY, 0, textureY, TRADE_BUTTON_WIDTH, TRADE_BUTTON_HEIGHT);
            
            // Draw trade items
            if (!trade.getInput1().isEmpty()) {
                context.drawItem(trade.getInput1(), buttonX + 5, buttonY + 2);
                context.drawItemInSlot(this.textRenderer, trade.getInput1(), buttonX + 5, buttonY + 2);
            }
            
            if (!trade.getInput2().isEmpty()) {
                context.drawItem(trade.getInput2(), buttonX + 26, buttonY + 2);
                context.drawItemInSlot(this.textRenderer, trade.getInput2(), buttonX + 26, buttonY + 2);
            }
            
            // Draw arrow
            context.drawTexture(TEXTURE, buttonX + 47, buttonY + 6, 15, 171, 10, 9);
            
            if (!trade.getOutput().isEmpty()) {
                context.drawItem(trade.getOutput(), buttonX + 62, buttonY + 2);
                context.drawItemInSlot(this.textRenderer, trade.getOutput(), buttonX + 62, buttonY + 2);
            }
            
            // Draw uses bar if trade has limited uses
            if (trade.getMaxUses() > 0) {
                int barWidth = 75;
                int barHeight = 2;
                int barX = buttonX + 12;
                int barY = buttonY + 17;
                
                // Background
                context.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF000000);
                
                // Fill
                float usesRatio = 1.0f - ((float) trade.getUses() / trade.getMaxUses());
                int fillWidth = (int) (barWidth * usesRatio);
                int color = usesRatio > 0.5f ? 0xFF00FF00 : usesRatio > 0.25f ? 0xFFFFFF00 : 0xFFFF0000;
                context.fill(barX, barY, barX + fillWidth, barY + barHeight, color);
            }
        }
        
        // Draw scroll arrows if needed
        if (trades.size() > 7) {
            // Up arrow
            boolean canScrollUp = scrollOffset > 0;
            int upArrowY = guiY + 16;
            context.drawTexture(TEXTURE, guiX + 108, upArrowY, canScrollUp ? 0 : 12, 199, 12, 7);
            
            // Down arrow
            boolean canScrollDown = scrollOffset < trades.size() - 7;
            int downArrowY = guiY + 149;
            context.drawTexture(TEXTURE, guiX + 108, downArrowY, canScrollDown ? 0 : 12, 206, 12, 7);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int x = (this.width - this.backgroundWidth) / 2;
            int y = (this.height - this.backgroundHeight) / 2;
            
            // Check trade selection
            int tradeListX = x + TRADE_LIST_X;
            int tradeListY = y + TRADE_LIST_Y;
            
            var trades = this.handler.getTrades();
            
            for (int i = 0; i < Math.min(7, trades.size() - scrollOffset); i++) {
                int tradeIndex = i + scrollOffset;
                int buttonX = tradeListX;
                int buttonY = tradeListY + i * TRADE_BUTTON_HEIGHT;
                
                if (mouseX >= buttonX && mouseX < buttonX + TRADE_BUTTON_WIDTH &&
                    mouseY >= buttonY && mouseY < buttonY + TRADE_BUTTON_HEIGHT) {
                    
                    VendingMachineTrade trade = trades.get(tradeIndex);
                    if (!trade.isDisabled()) {
                        selectTrade(tradeIndex);
                        return true;
                    }
                }
            }
            
            // Check scroll arrows
            if (trades.size() > 7) {
                // Up arrow
                if (scrollOffset > 0 && mouseX >= x + 108 && mouseX < x + 120 &&
                    mouseY >= y + 16 && mouseY < y + 23) {
                    scrollOffset--;
                    return true;
                }
                
                // Down arrow
                if (scrollOffset < trades.size() - 7 && mouseX >= x + 108 && mouseX < x + 120 &&
                    mouseY >= y + 149 && mouseY < y + 156) {
                    scrollOffset++;
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        var trades = this.handler.getTrades();
        if (trades.size() > 7) {
            if (verticalAmount > 0 && scrollOffset > 0) {
                scrollOffset--;
                return true;
            } else if (verticalAmount < 0 && scrollOffset < trades.size() - 7) {
                scrollOffset++;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void selectTrade(int index) {
        this.handler.selectTrade(index);
        
        // Send packet to server
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeInt(index);
//        ClientPlayNetworking.send(VendingMachinePackets.SELECT_TRADE, buf);
    }
}

*/