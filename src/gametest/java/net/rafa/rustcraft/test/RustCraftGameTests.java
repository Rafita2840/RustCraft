package net.rafa.rustcraft.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.rafa.rustcraft.config.ModGameRules;
import net.rafa.rustcraft.config.RustConfig;
import net.rafa.rustcraft.data.RustPlayerData;
import net.rafa.rustcraft.data.ThirstMath;

import java.io.StringReader;

/**
 * Game tests for the rules that do not need a player or a building. Run with {@code ./gradlew runGametest}.
 * Each test calls {@code context.complete()} when every check passed; a failed check throws.
 */
public class RustCraftGameTests implements FabricGameTest {

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static void checkEquals(Object expected, Object actual, String what) {
        check(expected.equals(actual), what + ": expected " + expected + " but got " + actual);
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void thirstDrainDependsOnMovement(TestContext context) {
        RustConfig.Thirst defaults = new RustConfig.Thirst();
        checkEquals(1, ThirstMath.drainPerTick(0f, defaults), "standing still");
        checkEquals(3, ThirstMath.drainPerTick(0.05f, defaults), "walking");
        checkEquals(10, ThirstMath.drainPerTick(0.15f, defaults), "running");
        checkEquals(12, ThirstMath.drainPerTick(0.25f, defaults), "sprinting");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void thirstDrainUsesConfigValues(TestContext context) {
        RustConfig.Thirst custom = new RustConfig.Thirst();
        custom.idleDrain = 4;
        custom.walkDrain = 0;
        checkEquals(4, ThirstMath.drainPerTick(0f, custom), "custom idle");
        checkEquals(4, ThirstMath.drainPerTick(0.05f, custom), "custom walking");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void exhaustionBuildsUpBeforeThirstDrops(TestContext context) {
        RustPlayerData result = ThirstMath.applyDrain(new RustPlayerData(20, 0), 100, 5000);
        checkEquals(20, result.thirst(), "thirst below the limit");
        checkEquals(100, result.thirstExhaustion(), "exhaustion below the limit");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void reachingTheLimitDropsThirstAndResets(TestContext context) {
        RustPlayerData result = ThirstMath.applyDrain(new RustPlayerData(20, 4950), 100, 5000);
        checkEquals(19, result.thirst(), "thirst after the limit");
        checkEquals(0, result.thirstExhaustion(), "exhaustion after the limit");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void thirstStaysBetweenZeroAndMax(TestContext context) {
        checkEquals(0, ThirstMath.applyDrain(new RustPlayerData(0, 4999), 10, 5000).thirst(), "drain at 0");
        checkEquals(0, new RustPlayerData(10, 0).withThirst(-5).thirst(), "below 0");
        checkEquals(RustPlayerData.MAX_THIRST, new RustPlayerData(10, 0).withThirst(99).thirst(), "above max");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void playerDataSurvivesSaveAndLoad(TestContext context) {
        RustPlayerData original = new RustPlayerData(7, 1234);
        JsonElement saved = RustPlayerData.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        RustPlayerData loaded = RustPlayerData.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();
        checkEquals(original, loaded, "round trip");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void oldSavesWithMissingFieldsStillLoad(TestContext context) {
        RustPlayerData loaded = RustPlayerData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow();
        checkEquals(RustPlayerData.MAX_THIRST, loaded.thirst(), "default thirst");
        checkEquals(0, loaded.thirstExhaustion(), "default exhaustion");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void configKeepsDefaultsAndClampsBadValues(TestContext context) {
        RustConfig config = RustConfig.parse(new StringReader(
                "{\"thirst\": {\"idleDrain\": -5, \"startThirst\": 99}}"));
        checkEquals(0, config.thirst.idleDrain, "negative drain is clamped");
        checkEquals(20, config.thirst.startThirst, "start thirst is clamped");
        checkEquals(7, config.thirst.runDrain, "missing option keeps its default");
        check(config.gamma != null && !config.gamma.forceLow, "missing section gets defaults");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void thirstGameRuleIsOnByDefault(TestContext context) {
        check(context.getWorld().getGameRules().getBoolean(ModGameRules.THIRST_ENABLED),
                "rustcraftThirstEnabled should default to true");
        context.complete();
    }
}
