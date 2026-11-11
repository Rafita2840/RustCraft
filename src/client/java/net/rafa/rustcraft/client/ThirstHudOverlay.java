package net.rafa.rustcraft.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.rafa.rustcraft.RustCraft;
import net.rafa.rustcraft.util.IEntityDataSaver;

public class ThirstHudOverlay implements HudRenderCallback {

    private static final Identifier FILLED_THIRST = Identifier.of(RustCraft.MOD_ID,
            "textures/thirst/filled_thirst.png");

    private static final Identifier HALF_FILLED_THIRST = Identifier.of(RustCraft.MOD_ID,
            "textures/thirst/half_filled_thirst.png");

    private static final Identifier EMPTY_THIRST = Identifier.of(RustCraft.MOD_ID,
            "textures/thirst/empty_thirst.png");

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        int x = 0;
        int y = 0;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null) {
            int width = client.getWindow().getScaledWidth();
            int height = client.getWindow().getScaledHeight();
            x = width / 2;
            y = height;
        }
        if (client != null && client.player != null && !client.player.isInCreativeMode()) {
            RenderSystem.setShader(GameRenderer::getPositionTexProgram);
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShaderTexture(0, EMPTY_THIRST);
            for (int i = 0; i < 10; i++) {
                drawContext.drawTexture(EMPTY_THIRST,
                        x + 6 + (i * 8),
                        y - 53,
                        0, 0,
                        16, 16,
                        16, 16
                );
            }

            RenderSystem.setShaderTexture(0, FILLED_THIRST);
            int thirst = ((IEntityDataSaver) client.player).getPersistentData().getInt("thirst");
            float halfThirst = (float) thirst /2;
            thirst = thirst / 2;
            for (int i = 0; i < 10; i++) {
                if (halfThirst == 0.5f) {
                    drawContext.drawTexture(
                            HALF_FILLED_THIRST,
                            x + 78 - (i * 8),
                            y - 53,
                            0, 0,
                            16, 16,
                            16, 16
                    );
                }
                if (thirst > i) {
                    drawContext.drawTexture(
                            FILLED_THIRST,
                            x + 78 - (i * 8),
                            y - 53,
                            0, 0,
                            16, 16,
                            16, 16
                    );
                    halfThirst--;
                } else {
                    break;
                }
            }
        }
    }
}
