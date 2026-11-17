package net.rafa.rustcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.util.HudLayout;

/**
 * The thirst bar: 10 droplets in the row above the food bar, on the right. It keeps that row (the air bubbles move
 * above it) and only moves up when a mount's hearts take extra rows, see {@link HudLayout}.
 * Drawn by {@link StatusHud}, which decides when it is shown.
 */
public class ThirstHudOverlay {

    private static final Identifier FILLED_THIRST = Identifier.of(RustCraft.MOD_ID,
            "textures/thirst/filled_thirst.png");

    private static final Identifier HALF_FILLED_THIRST = Identifier.of(RustCraft.MOD_ID,
            "textures/thirst/half_filled_thirst.png");

    private static final Identifier EMPTY_THIRST = Identifier.of(RustCraft.MOD_ID,
            "textures/thirst/empty_thirst.png");

    /** Pixels from the bottom of the screen to the top of the 16x16 droplet texture in the normal row. */
    private static final int BASE_Y_FROM_BOTTOM = 53;
    /** Droplets are 16 px textures drawn 8 px apart (they have transparent margins). */
    private static final int SPACING = 8;
    private static final int ICON_SIZE = 16;
    private static final int DROPLETS = 10;

    public void render(DrawContext context, ClientPlayerEntity player) {
        int x = context.getScaledWindowWidth() / 2 + 6;
        int y = context.getScaledWindowHeight() - BASE_Y_FROM_BOTTOM - raise(player);

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // Empty droplets first, left to right
        for (int i = 0; i < DROPLETS; i++) {
            context.drawTexture(EMPTY_THIRST, x + i * SPACING, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        }

        // Then the filled ones on top, from the right: the bar empties towards the left like the food bar
        int thirst = ClientPlayerStats.thirst();
        for (int i = 0; i < DROPLETS; i++) {
            int iconX = x + (DROPLETS - 1 - i) * SPACING;
            if (thirst >= 2 * i + 2) {
                context.drawTexture(FILLED_THIRST, iconX, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            } else if (thirst == 2 * i + 1) {
                context.drawTexture(HALF_FILLED_THIRST, iconX, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            } else {
                break;
            }
        }
    }

    /** Only extra rows of mount hearts move the bar (same conditions as the vanilla mount health bar). */
    private static int raise(ClientPlayerEntity player) {
        Entity vehicle = player.getVehicle();
        boolean ridingLiving = vehicle instanceof LivingEntity living && living.isLiving();
        float mountMaxHealth = ridingLiving ? ((LivingEntity) vehicle).getMaxHealth() : 0f;
        return HudLayout.thirstBarRaise(HudLayout.mountHeartCount(ridingLiving, mountMaxHealth));
    }
}
