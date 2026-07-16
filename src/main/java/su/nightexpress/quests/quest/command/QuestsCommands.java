package su.nightexpress.quests.quest.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.quests.QuestsPlaceholders;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;
import su.nightexpress.quests.quest.QuestManager;
import su.nightexpress.quests.tracker.QuestTrackerManager;
import su.nightexpress.quests.lore.command.LoreCommands;
import su.nightexpress.quests.island.command.IslandCommands;

public class QuestsCommands {

    public static final String DEFAULT_ALIAS = "quests";

    private static final String ARG_PLAYER = "player";

    private static QuestsPlugin plugin;
    private static QuestManager manager;
    private static NightCommand command;

    public static void load(@NotNull QuestsPlugin questsPlugin, @NotNull QuestManager questManager) {
        plugin = questsPlugin;
        manager = questManager;

        command = NightCommand.hub(plugin, Config.FEATURES_QUESTS_ALIASES.get(), builder -> {
            builder
                .localized(Lang.COMMAND_QUESTS_NAME)
                .permission(Perms.COMMAND_QUESTS)
                .description(Lang.COMMAND_QUESTS_DESC)
                .branch(Commands.literal("refresh")
                    .permission(Perms.COMMAND_QUESTS_REFRESH)
                    .description(Lang.COMMAND_QUESTS_REFRESH_DESC)
                    .withArguments(Arguments.playerName(ARG_PLAYER))
                    .executes(QuestsCommands::refreshQuests)
                )
                .branch(Commands.literal("track")
                    .permission(Perms.COMMAND_TRACK_TOGGLE)
                    .withArguments(Arguments.string("mode").optional().suggestions((reader, context) -> su.nightexpress.nightcore.util.Lists.newList("BOSS_BAR", "ACTION_BAR", "CHAT", "NONE")))
                    .executes(QuestsCommands::trackMode)
                );

            if (Config.FEATURES_LORE_ENABLED.get()) {
                LoreCommands.load(questsPlugin);
                builder.branch(Commands.literal("lore")
                    .permission(Perms.COMMAND_QUESTS_LORE)
                    .description(Lang.COMMAND_QUESTS_LORE_DESC)
                    .executes(LoreCommands::openLoreMenu)
                );
            }

            if (Config.FEATURES_ISLAND_QUESTS_ENABLED.get()) {
                IslandCommands.load(questsPlugin);
                builder.branch(Commands.literal("island")
                    .permission(Perms.COMMAND_ISLAND)
                    .description(Lang.COMMAND_ISLAND_DESC)
                    .executes(IslandCommands::openIslandMenu)
                );
                builder.branch(Commands.literal("is")
                    .permission(Perms.COMMAND_ISLAND)
                    .description(Lang.COMMAND_ISLAND_DESC)
                    .executes(IslandCommands::openIslandMenu)
                );
            }

            builder.executes(QuestsCommands::openQuests);
        });
        command.register();
    }

    public static void shutdown() {
        LoreCommands.shutdown();
        IslandCommands.shutdown();
        command.unregister();
        command = null;
        manager = null;
        plugin = null;
    }

    private static boolean openQuests(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        manager.openQuests(player);
        return true;
    }

    private static boolean refreshQuests(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String playerName = arguments.getString(ARG_PLAYER);
        plugin.getUserManager().manageUser(playerName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            Player player = user.getPlayer();
            if (player != null) {
                manager.refreshQuests(player);
            }
            else {
                user.setNewQuestsDate(0L);
                plugin.getUserManager().save(user);
            }
            context.send(Lang.QUESTS_REFRESHED_FOR, replacer -> replacer.replace(QuestsPlaceholders.PLAYER_NAME, user.getName()));
        });
        return true;
    }

    private static boolean trackMode(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();

        if (!arguments.contains("mode")) {
            plugin.getUserManager().manageUser(player.getUniqueId(), user -> {
                if (user == null) {
                    context.errorBadPlayer();
                    return;
                }
                String currentMode = user.getTrackerMode();
                Lang.COMMAND_TRACK_STATUS.message().send(player, replacer -> replacer.replace(QuestsPlaceholders.GENERIC_INPUT, currentMode));
            });
            return true;
        }

        String modeInput = arguments.getString("mode");
        String matchedMode = null;
        for (String m : new String[]{"BOSS_BAR", "ACTION_BAR", "CHAT", "NONE"}) {
            if (m.equalsIgnoreCase(modeInput)) {
                matchedMode = m;
                break;
            }
        }

        if (matchedMode == null) {
            Lang.COMMAND_TRACK_INVALID_MODE.message().send(player);
            return false;
        }

        final String finalMode = matchedMode;
        plugin.getUserManager().manageUser(player.getUniqueId(), user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            user.setTrackerMode(finalMode);
            plugin.getUserManager().save(user);

            Lang.COMMAND_TRACK_MODE_CHANGED.message().send(player, replacer -> replacer.replace(QuestsPlaceholders.GENERIC_INPUT, finalMode));

            if (QuestTrackerManager.getInstance() != null) {
                QuestTrackerManager.getInstance().cleanup(player);
            }
        });
        return true;
    }
}
