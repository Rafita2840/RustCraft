package net.rafa.rustcraft.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.rafa.rustcraft.util.IEntityDataSaver;
import net.rafa.rustcraft.util.ThirstData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {
    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void copyThirst(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        IEntityDataSaver oldData = (IEntityDataSaver) oldPlayer;
        IEntityDataSaver newData = (IEntityDataSaver) this;

        if (!alive) {
            ThirstData.syncThirst(10, newData);
            newData.getPersistentData().putInt("thirst", 10);
        } else {
            int oldThirst = oldData.getPersistentData().getInt("thirst");
            ThirstData.syncThirst(oldThirst, newData);
            newData.getPersistentData().putInt("thirst", oldThirst);
        }

        ThirstData.syncThirst(newData.getPersistentData().getInt("thirst"), newData);
    }
}