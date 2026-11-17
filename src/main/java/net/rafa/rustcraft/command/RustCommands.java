package net.rafa.rustcraft.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.rafa.rustcraft.config.ConfigSync;
import net.rafa.rustcraft.config.RustConfig;
import net.rafa.rustcraft.data.PlayerStats;
import net.rafa.rustcraft.data.RustPlayerData;
import net.rafa.rustcraft.util.ModText;

/**
 * Every {@code /rustcraft} command (operators only). Debug and admin tools live here.
 * <pre>
 * /rustcraft reload                         re-read config/rustcraft.json
 * /rustcraft stats get &lt;player&gt;              show a player's stats
 * /rustcraft stats set thirst &lt;0-20&gt; [player] set thirst (yourself if no player is given)
 * </pre>
 * New stats (radiation, bleeding...) add one more {@code literal} under {@code stats set} when they exist.
 * Loot tables and crates can be tested with the vanilla {@code /loot} and {@code /give} for now.
 */
public final class RustCommands {
    private RustCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                dispatcher.register(CommandManager.literal("rustcraft")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(CommandManager.literal("reload").executes(RustCommands::reload))
                        .then(CommandManager.literal("stats")
                                .then(CommandManager.literal("get")
                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                .executes(RustCommands::statsGet)))
                                .then(CommandManager.literal("set")
                                        .then(CommandManager.literal("thirst")
                                                .then(CommandManager.argument("value",
                                                                IntegerArgumentType.integer(0, RustPlayerData.MAX_THIRST))
                                                        .executes(context -> setThirst(context,
                                                                context.getSource().getPlayerOrThrow()))
                                                        .then(CommandManager.argument("player", EntityArgumentType.player())
                                                                .executes(context -> setThirst(context,
                                                                        EntityArgumentType.getPlayer(context, "player"))))))))));
    }

    private static int reload(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!RustConfig.load()) {
            source.sendError(ModText.error("text.rustcraft.config_reload_failed"));
            return 0;
        }
        source.getServer().getPlayerManager().getPlayerList().forEach(ConfigSync::send);
        source.sendFeedback(() -> ModText.info("text.rustcraft.config_reloaded"), true);
        return 1;
    }

    private static int statsGet(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
        RustPlayerData data = PlayerStats.get(player);
        context.getSource().sendFeedback(() -> Text.translatable("text.rustcraft.stats_get",
                player.getDisplayName(), data.thirst(), RustPlayerData.MAX_THIRST).formatted(Formatting.GRAY), false);
        return data.thirst();
    }

    private static int setThirst(CommandContext<ServerCommandSource> context, ServerPlayerEntity player) {
        int value = IntegerArgumentType.getInteger(context, "value");
        PlayerStats.set(player, PlayerStats.get(player).withThirst(value));
        context.getSource().sendFeedback(() -> Text.translatable("text.rustcraft.stats_thirst_set",
                player.getDisplayName(), value).formatted(Formatting.GRAY), true);
        return value;
    }
}
