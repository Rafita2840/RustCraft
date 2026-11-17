package net.rafa.rustcraft.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.test.GameTest;
import net.minecraft.test.TestContext;
import net.minecraft.world.Difficulty;
import net.rafa.rustcraft.config.ModGameRules;
import net.rafa.rustcraft.config.RustConfig;
import net.rafa.rustcraft.damage.ModDamageTypes;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.rafa.rustcraft.data.DisplayedStats;
import net.rafa.rustcraft.data.RustPlayerData;
import net.rafa.rustcraft.thirst.ThirstMath;
import net.rafa.rustcraft.util.HudLayout;
import net.rafa.rustcraft.util.ModTags;

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
    public void thirstDrainDependsOnActivity(TestContext context) {
        RustConfig.Thirst defaults = new RustConfig.Thirst();
        checkEquals(1, ThirstMath.baseDrain(false, false, false, defaults), "standing still");
        checkEquals(3, ThirstMath.baseDrain(true, false, false, defaults), "walking");
        checkEquals(10, ThirstMath.baseDrain(true, true, false, defaults), "sprinting");
        checkEquals(12, ThirstMath.baseDrain(true, true, true, defaults), "sprint-jumping");
        checkEquals(3, ThirstMath.baseDrain(false, false, true, defaults), "jumping in place");
        checkEquals(1, ThirstMath.baseDrain(false, true, false, defaults), "sprinting into a wall counts as idle");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void thirstDrainUsesConfigValuesAndCap(TestContext context) {
        RustConfig.Thirst custom = new RustConfig.Thirst();
        custom.idleDrain = 4;
        custom.walkDrain = 0;
        checkEquals(4, ThirstMath.baseDrain(false, false, false, custom), "custom idle");
        checkEquals(4, ThirstMath.baseDrain(true, false, false, custom), "custom walking");
        custom.maxDrainPerTick = 5;
        checkEquals(5, ThirstMath.baseDrain(true, true, true, custom), "capped");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void heatMultipliesDrainAfterTheCap(TestContext context) {
        RustConfig.Thirst defaults = new RustConfig.Thirst();
        checkEquals(1f, ThirstMath.multiplier(false, 0.8f, defaults), "plains");
        checkEquals(1.5f, ThirstMath.multiplier(false, 2.0f, defaults), "desert");
        checkEquals(2f, ThirstMath.multiplier(true, 2.0f, defaults), "nether replaces hot biome");
        checkEquals(18f, ThirstMath.drainPerTick(true, true, true, false, 2.0f, defaults), "sprint-jumping in a desert");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void dehydrationFollowsDifficultyLikeStarvation(TestContext context) {
        check(!ThirstMath.canDehydrationHurt(20f, Difficulty.PEACEFUL), "peaceful never hurts");
        check(ThirstMath.canDehydrationHurt(11f, Difficulty.EASY), "easy hurts above 5 hearts");
        check(!ThirstMath.canDehydrationHurt(10f, Difficulty.EASY), "easy stops at 5 hearts");
        check(!ThirstMath.canDehydrationHurt(1f, Difficulty.NORMAL), "normal stops at half a heart");
        check(ThirstMath.canDehydrationHurt(1f, Difficulty.HARD), "hard can kill");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void dehydrationDamageTypeIsRegistered(TestContext context) {
        Registry<DamageType> damageTypes = context.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE);
        check(damageTypes.contains(ModDamageTypes.DEHYDRATION), "rustcraft:dehydration missing: run ./gradlew runDatagen");
        check(damageTypes.entryOf(ModDamageTypes.DEHYDRATION).isIn(DamageTypeTags.BYPASSES_ARMOR),
                "dehydration should bypass armor");
        check(damageTypes.entryOf(ModDamageTypes.DEHYDRATION).isIn(ModTags.DamageTypes.NO_KNOCKBACK),
                "dehydration should be in #minecraft:no_knockback, or it pushes the player");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void exhaustionBuildsUpBeforeThirstDrops(TestContext context) {
        RustPlayerData result = ThirstMath.applyDrain(new RustPlayerData(20, 0), 100, 5000);
        checkEquals(20, result.thirst(), "thirst below the limit");
        checkEquals(100f, result.thirstExhaustion(), "exhaustion below the limit");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void reachingTheLimitDropsThirstAndResets(TestContext context) {
        RustPlayerData result = ThirstMath.applyDrain(new RustPlayerData(20, 4950), 100, 5000);
        checkEquals(19, result.thirst(), "thirst after the limit");
        checkEquals(0f, result.thirstExhaustion(), "exhaustion after the limit");
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
        RustPlayerData original = new RustPlayerData(7, 1234.5f);
        JsonElement saved = RustPlayerData.CODEC.encodeStart(JsonOps.INSTANCE, original).getOrThrow();
        RustPlayerData loaded = RustPlayerData.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();
        checkEquals(original, loaded, "round trip");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void oldSavesWithMissingFieldsStillLoad(TestContext context) {
        RustPlayerData loaded = RustPlayerData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString("{}")).getOrThrow();
        checkEquals(RustPlayerData.MAX_THIRST, loaded.thirst(), "default thirst");
        checkEquals(0f, loaded.thirstExhaustion(), "default exhaustion");
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

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void lowThirstBlocksSprint(TestContext context) {
        check(!ThirstMath.canSprint(0), "0 cannot sprint");
        check(!ThirstMath.canSprint(5), "5 cannot sprint");
        check(ThirstMath.canSprint(6), "6 can sprint");
        check(ThirstMath.canSprint(20), "20 can sprint");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void thirstBarKeepsItsRowAndOnlyMountHeartsRaiseIt(TestContext context) {
        checkEquals(0, HudLayout.thirstBarRaise(0), "not riding");
        checkEquals(0, HudLayout.thirstBarRaise(HudLayout.mountHeartCount(true, 10f)), "5 hearts = 1 row");
        checkEquals(10, HudLayout.thirstBarRaise(HudLayout.mountHeartCount(true, 30f)), "15 hearts = 2 rows");
        checkEquals(30, HudLayout.mountHeartCount(true, 100f), "capped at 30 hearts");
        checkEquals(20, HudLayout.thirstBarRaise(30), "30 hearts = 3 rows");
        checkEquals(0, HudLayout.mountHeartCount(false, 30f), "not riding");
        checkEquals(11, HudLayout.AIR_BUBBLES_RAISE, "bubbles go one row and 1 px up");
        context.complete();
    }

    @GameTest(templateName = FabricGameTest.EMPTY_STRUCTURE)
    public void displayedStatsIgnoreHiddenValuesAndRoundTrip(TestContext context) {
        RustPlayerData a = new RustPlayerData(12, 0f);
        RustPlayerData b = new RustPlayerData(12, 3.5f);
        check(a.displayed().equals(b.displayed()), "same thirst, different exhaustion are equal");
        check(!a.displayed().equals(a.withThirst(11).displayed()), "different thirst differs");
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        DisplayedStats.CODEC.encode(buf, a.displayed());
        checkEquals(a.displayed(), DisplayedStats.CODEC.decode(buf), "codec round trip");
        context.complete();
    }
}
