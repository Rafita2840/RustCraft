package net.rafa.rustcraft.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.rafa.rustcraft.util.IEntityDataSaver;
import net.rafa.rustcraft.util.ThirstData;
import net.rafa.rustcraft.util.ThirstSaturationData;

public class PlayerTickHandler implements ServerTickEvents.StartTick {



    @Override
    public void onStartTick(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()){
            float speedVariation = player.horizontalSpeed - player.prevHorizontalSpeed;
            int amount = 1;
            if (speedVariation > 0f) {
                amount += 2;
            }
            if (speedVariation > 0.13f) {
                amount += 7;
            }
            if (speedVariation > 0.20f) {
                amount += 2;
            }
            IEntityDataSaver dataPlayer = ((IEntityDataSaver) player);
            if (ThirstSaturationData.addSaturation(dataPlayer, amount) == 5000) {
                ThirstData.removeThirst(dataPlayer, 1);
                ThirstData.syncThirst(dataPlayer);
                ThirstSaturationData.resetSaturation(dataPlayer);
            }
            player.sendMessage(Text.literal("Saturation " +
                                    ((IEntityDataSaver) player)
                                            .getPersistentData()
                                            .getInt("thirst_saturation"))
                            .fillStyle(Style.EMPTY
                                    .withColor(Formatting.AQUA)),
                    true);
        }
    }
}
