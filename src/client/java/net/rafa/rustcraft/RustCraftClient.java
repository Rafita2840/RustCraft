package net.rafa.rustcraft;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.rafa.rustcraft.client.ClientPlayerStats;
import net.rafa.rustcraft.client.ClientServerConfig;
import net.rafa.rustcraft.networking.PlayerStatsSyncS2CPayload;
import net.rafa.rustcraft.networking.ServerConfigSyncS2CPayload;
import net.rafa.rustcraft.screen.ModScreenHandlers;
import net.rafa.rustcraft.screen.custom.DefaultCrateScreen;
import net.rafa.rustcraft.screen.custom.VendingMachineScreen;
import net.rafa.rustcraft.util.TooltipKeys;

public class RustCraftClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        TooltipKeys.setProviders(Screen::hasShiftDown, Screen::hasControlDown);
        ClientPlayNetworking.registerGlobalReceiver(PlayerStatsSyncS2CPayload.PAYLOAD_ID,
                (payload, context) -> context.client().execute(() -> ClientPlayerStats.set(payload.stats())));
        ClientPlayNetworking.registerGlobalReceiver(ServerConfigSyncS2CPayload.PAYLOAD_ID,
                (payload, context) -> context.client().execute(
                        () -> ClientServerConfig.set(payload.forceLowGamma(), payload.maxGamma(), payload.thirstEnabled())));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ClientPlayerStats.reset();
            ClientServerConfig.reset();
        });
        HandledScreens.register(ModScreenHandlers.CRATE_SCREEN_HANDLER, DefaultCrateScreen::new);
        HandledScreens.register(ModScreenHandlers.VENDING_MACHINE_SCREEN_HANDLER, VendingMachineScreen::new);
    }
}
