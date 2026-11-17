package net.rafa.rustcraft.config;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

/**
 * Per-world switches, changed in game with {@code /gamerule}. The balance numbers live in {@link RustConfig}.
 * <p>
 * Names use camelCase without a namespace: a colon cannot be typed in the {@code /gamerule} command.
 * Add new rules here (for example {@code rustcraftBuildingDecay} in P4.6).
 */
public final class ModGameRules {
    private ModGameRules() {}

    public static final String THIRST_ENABLED_NAME = "rustcraftThirstEnabled";

    /** When false, thirst does not drain. */
    public static final GameRules.Key<GameRules.BooleanRule> THIRST_ENABLED = GameRuleRegistry.register(
            THIRST_ENABLED_NAME, GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(true));

    /** Call once from onInitialize so the rules are registered before any world loads. */
    public static void init() {
    }
}
