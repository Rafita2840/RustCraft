package net.rafa.rustcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.rafa.rustcraft.client.ThirstHudOverlay;
import net.rafa.rustcraft.networking.ThirstSaturationSyncS2CPayload;
import net.rafa.rustcraft.networking.ThirstSyncS2CPayload;
import net.rafa.rustcraft.screen.ModScreenHandlers;
import net.rafa.rustcraft.screen.custom.DefaultCrateScreen;
import net.rafa.rustcraft.util.IEntityDataSaver;

public class RustCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(new ThirstHudOverlay());
        ClientPlayNetworking.registerGlobalReceiver(ThirstSyncS2CPayload.PAYLOAD_ID,
                ((payload, context) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.execute(() -> {
                        if (client.player != null){
                            ((IEntityDataSaver) client.player).getPersistentData().putInt(ThirstSyncS2CPayload.getPath(), payload.thirstValue());
                        }
                    });
                }));
        ClientPlayNetworking.registerGlobalReceiver(ThirstSaturationSyncS2CPayload.PAYLOAD_ID,
                ((payload, context) -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.execute(() -> {
                        if (client.player != null){
                            ((IEntityDataSaver) client.player).getPersistentData().putInt(ThirstSaturationSyncS2CPayload.getPath(), payload.saturationValue());
                        }
                    });
                }));
        HandledScreens.register(ModScreenHandlers.CRATE_SCREEN_HANDLER, DefaultCrateScreen::new);
    }
}
