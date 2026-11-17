package net.rafa.rustcraft.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * The mod's status HUD. {@code InGameHudMixin} calls it from inside vanilla's status bar drawing, so it is part
 * of the same HUD layer: it is drawn under the chat and hidden with F1 exactly like the vanilla bars.
 * P1.9 adds the other statuses (radiation, bleeding, cold...) here, next to the thirst bar.
 */
public final class StatusHud {
    private static final ThirstHudOverlay THIRST_BAR = new ThirstHudOverlay();

    private StatusHud() {}

    /** Whether the thirst bar is shown for this player; the air bubbles make room for it only then. */
    public static boolean thirstBarVisible() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && !player.isCreative() && !player.isSpectator()
                && ClientServerConfig.thirstEnabled();
    }

    public static void render(DrawContext context) {
        if (thirstBarVisible()) {
            THIRST_BAR.render(context, MinecraftClient.getInstance().player);
        }
    }
}
