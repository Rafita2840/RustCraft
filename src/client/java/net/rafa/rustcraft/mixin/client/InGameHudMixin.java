package net.rafa.rustcraft.mixin.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.rafa.rustcraft.client.StatusHud;
import net.rafa.rustcraft.util.HudLayout;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Puts the mod's status bars inside vanilla's status bar drawing (1.21.1 draws it in the main HUD layer, before
 * the chat layer), so they are covered by the chat like the vanilla bars and not drawn over it. Also moves the air
 * bubbles one row up so they sit above the thirst bar.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "renderStatusBars", at = @At("TAIL"))
    private void rustcraft$renderStatusHud(DrawContext context, CallbackInfo ci) {
        StatusHud.render(context);
    }

    /**
     * Local 15 of {@code renderStatusBars} is the y of the current row. At the "air" profiler step it is the row
     * above the food bar, which the thirst bar takes, so the bubbles start one row higher.
     */
    @ModifyVariable(method = "renderStatusBars",
            at = @At(value = "CONSTANT", args = "stringValue=air"), index = 15)
    private int rustcraft$airAboveThirst(int y) {
        return StatusHud.thirstBarVisible() ? y - HudLayout.AIR_BUBBLES_RAISE : y;
    }
}
