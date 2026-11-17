package net.rafa.rustcraft.mixin.client;

import net.minecraft.client.network.ClientPlayerEntity;
import net.rafa.rustcraft.client.ClientPlayerStats;
import net.rafa.rustcraft.client.ClientServerConfig;
import net.rafa.rustcraft.thirst.ThirstMath;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Low thirst blocks sprinting, the way low food does in vanilla.
 * <p>
 * Vanilla's private {@code canSprint()} is true when the player rides something, has more than 6 food, or may fly.
 * It is used both to start a sprint and to cancel a running one, so hooking its result covers both. This stays on
 * the client on purpose: forcing sprint off from the server makes the movement stutter.
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @Inject(method = "canSprint", at = @At("RETURN"), cancellable = true)
    private void rustcraft$thirstBlocksSprint(CallbackInfoReturnable<Boolean> cir) {
        ClientPlayerEntity self = (ClientPlayerEntity) (Object) this;
        if (cir.getReturnValueZ()
                && !self.hasVehicle()
                && !self.getAbilities().allowFlying
                && ClientServerConfig.thirstEnabled()
                && !ThirstMath.canSprint(ClientPlayerStats.thirst())) {
            cir.setReturnValue(false);
        }
    }
}
