package net.rafa.rustcraft.thirst;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.rafa.rustcraft.config.ModGameRules;
import net.rafa.rustcraft.config.RustConfig;
import net.rafa.rustcraft.damage.ModDamageTypes;
import net.rafa.rustcraft.data.PlayerStats;
import net.rafa.rustcraft.data.RustPlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Drains thirst every tick from what each player does, and hurts players whose thirst is 0.
 * <p>
 * Movement is measured as the change in position since the last tick: the server doesn't know a player's real
 * velocity ({@code getVelocity()} is near zero for players). Jumps are counted with the vanilla jump statistic,
 * which the server increases itself, so no mixin is needed.
 */
public class PlayerTickHandler implements ServerTickEvents.StartTick {

    /** Horizontal distance per tick (squared) above which a player counts as moving. Sneaking is ~0.065 blocks/tick. */
    private static final double MOVING_DISTANCE_SQ = 0.01 * 0.01;

    /** Where a player was and how many times they had jumped at the previous tick. */
    private record Tracked(Vec3d pos, int jumps) {}

    private final Map<UUID, Tracked> tracked = new HashMap<>();

    public static void register() {
        PlayerTickHandler handler = new PlayerTickHandler();
        ServerTickEvents.START_SERVER_TICK.register(handler);
        ServerPlayConnectionEvents.DISCONNECT.register((networkHandler, server) ->
                handler.tracked.remove(networkHandler.player.getUuid()));
    }

    @Override
    public void onStartTick(MinecraftServer server) {
        if (!server.getGameRules().getBoolean(ModGameRules.THIRST_ENABLED)) {
            tracked.clear();
            return;
        }
        RustConfig.Thirst config = RustConfig.get().thirst;
        boolean damageTick = server.getTicks() % config.dehydrationDamageInterval == 0;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            Vec3d pos = player.getPos();
            int jumps = player.getStatHandler().getStat(Stats.CUSTOM, Stats.JUMP);
            Tracked last = tracked.put(player.getUuid(), new Tracked(pos, jumps));
            // First tick after joining: nothing to compare with yet
            if (last == null || player.isCreative() || player.isSpectator() || !player.isAlive()) {
                continue;
            }

            double dx = pos.x - last.pos().x;
            double dz = pos.z - last.pos().z;
            boolean moving = !player.hasVehicle() && dx * dx + dz * dz > MOVING_DISTANCE_SQ;
            boolean jumped = jumps > last.jumps();

            World world = player.getWorld();
            float temperature = world.getBiome(player.getBlockPos()).value().getTemperature();
            float drain = ThirstMath.drainPerTick(moving, player.isSprinting(), jumped,
                    world.getDimension().ultrawarm(), temperature, config);

            // The exhaustion counter is saved with the player but never sent to the client
            RustPlayerData data = ThirstMath.applyDrain(PlayerStats.get(player), drain, config.exhaustionLimit);
            PlayerStats.set(player, data);

            if (damageTick && data.thirst() == 0
                    && ThirstMath.canDehydrationHurt(player.getHealth(), world.getDifficulty())) {
                player.damage(ModDamageTypes.source(world, ModDamageTypes.DEHYDRATION), config.dehydrationDamage);
            }
        }
    }
}
