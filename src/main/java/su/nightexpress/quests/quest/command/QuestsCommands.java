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
import su.nightexpress.quests.community.command.CommunityCommands;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;
import su.nightexpress.quests.quest.QuestManager;
import su.nightexpress.quests.tracker.QuestTrackerManager;
import su.nightexpress.quests.lore.command.LoreCommands;
import su.nightexpress.quests.island.command.IslandCommands;
import su.nightexpress.quests.personal.command.PersonalQuestCommands;

public class QuestsCommands {

    public static final String DEFAULT_ALIAS = "quests";

    private static final String ARG_PLAYER = "player";

    private static QuestsPlugin plugin;
    private static QuestManager manager;
    private static NightCommand command;
    private static NightCommand dailyCommand;

    public static void load(@NotNull QuestsPlugin questsPlugin, @NotNull QuestManager questManager) {
        plugin = questsPlugin;
        manager = questManager;

        String[] aliases = parseAliases(Config.GENERAL_COMMAND_ALIASES.get());

        command = NightCommand.hub(plugin, aliases, builder -> {
            builder
                .localized(Lang.COMMAND_QUESTS_NAME)
                .permission(Perms.COMMAND_QUESTS)
                .description(Lang.COMMAND_QUESTS_DESC)
                .branch(Commands.literal("daily")
                    .permission(Perms.COMMAND_QUESTS)
                    .description(Lang.COMMAND_QUESTS_DESC)
                    .executes(QuestsCommands::openDailyQuests)
                )
                .branch(Commands.literal("dailyquests")
                    .permission(Perms.COMMAND_QUESTS)
                    .description(Lang.COMMAND_QUESTS_DESC)
                    .executes(QuestsCommands::openDailyQuests)
                )
                .branch(Commands.literal("dquests")
                    .permission(Perms.COMMAND_QUESTS)
                    .description(Lang.COMMAND_QUESTS_DESC)
                    .executes(QuestsCommands::openDailyQuests)
                )
                .branch(Commands.literal("track")
                    .permission(Perms.COMMAND_TRACK_TOGGLE)
                    .withArguments(Arguments.string("mode").optional().suggestions((reader, context) -> su.nightexpress.nightcore.util.Lists.newList("BOSS_BAR", "ACTION_BAR", "CHAT", "NONE")))
                    .executes(QuestsCommands::trackMode)
                );

            if (Config.FEATURES_QUESTS_ENABLED.get()) {
                for (String alias : Config.FEATURES_QUESTS_ALIASES.get()) {
                    builder.branch(Commands.literal(alias)
                        .permission(Perms.COMMAND_QUESTS)
                        .description(Lang.COMMAND_QUESTS_DESC)
                        .executes(QuestsCommands::openDailyQuests)
                    );
                }
            }

            if (Config.FEATURES_BATTLE_PASS_ENABLED.get()) {
                for (String alias : Config.FEATURES_BATTLE_PASS_ALIASES.get()) {
                    builder.branch(Commands.literal(alias)
                        .permission(Perms.COMMAND_BATTLE_PASS)
                        .description(Lang.COMMAND_BATTLE_PASS_DESC)
                        .executes(QuestsCommands::openBattlePass)
                    );
                }
            }

            if (Config.FEATURES_MILESTONES_ENABLED.get()) {
                for (String alias : Config.FEATURES_MILESTONES_ALIASES.get()) {
                    builder.branch(Commands.literal(alias)
                        .permission(Perms.COMMAND_MILESTONES)
                        .executes(QuestsCommands::openMilestones)
                    );
                }
            }

            if (Config.FEATURES_LORE_ENABLED.get()) {
                LoreCommands.load(questsPlugin);
                for (String alias : Config.FEATURES_LORE_ALIASES.get()) {
                    builder.branch(Commands.literal(alias)
                        .permission(Perms.COMMAND_QUESTS_LORE)
                        .description(Lang.COMMAND_QUESTS_LORE_DESC)
                        .executes(LoreCommands::openLoreMenu)
                    );
                }
            }

            if (Config.FEATURES_ISLAND_QUESTS_ENABLED.get()) {
                IslandCommands.load(questsPlugin);
                for (String alias : Config.FEATURES_ISLAND_QUESTS_ALIASES.get()) {
                    builder.branch(Commands.literal(alias)
                        .permission(Perms.COMMAND_ISLAND)
                        .description(Lang.COMMAND_ISLAND_DESC)
                        .executes(IslandCommands::openIslandMenu)
                    );
                }
            }

            if (Config.FEATURES_PERSONAL_QUESTS_ENABLED.get()) {
                PersonalQuestCommands.load(questsPlugin);
                for (String alias : Config.FEATURES_PERSONAL_QUESTS_ALIASES.get()) {
                    builder.branch(Commands.literal(alias)
                        .permission(Perms.COMMAND_PERSONAL)
                        .description(Lang.COMMAND_PERSONAL_DESC)
                        .executes(PersonalQuestCommands::executePersonalCommand)
                    );
                }
            }

            if (Config.FEATURES_COMMUNITY_QUESTS_ENABLED.get()) {
                CommunityCommands.load(questsPlugin);
                for (String alias : Config.FEATURES_COMMUNITY_QUESTS_ALIASES.get()) {
                    builder.branch(Commands.literal(alias)
                        .permission(Perms.COMMAND_COMMUNITY)
                        .description(Lang.COMMAND_COMMUNITY_DESC)
                        .executes(CommunityCommands::openLeaderboardMenu)
                    );
                }
            }

            // Default execution for /quests (/quete, /q) -> opens Main Menu (quests.yml)
            builder.executes(QuestsCommands::openMainMenu);
        });
        command.register();

        if (Config.FEATURES_QUESTS_ENABLED.get() && Config.FEATURES_QUESTS_STANDALONE_COMMAND.get()) {
            dailyCommand = NightCommand.hub(plugin, Config.FEATURES_QUESTS_ALIASES.get(), builder -> builder
                .localized(Lang.COMMAND_QUESTS_NAME)
                .permission(Perms.COMMAND_QUESTS)
                .description(Lang.COMMAND_QUESTS_DESC)
                .executes(QuestsCommands::openDailyQuests)
            );
            dailyCommand.register();
        }
    }

    public static void shutdown() {
        LoreCommands.shutdown();
        IslandCommands.shutdown();
        PersonalQuestCommands.shutdown();
        CommunityCommands.shutdown();
        if (command != null) {
            command.unregister();
            command = null;
        }
        if (dailyCommand != null) {
            dailyCommand.unregister();
            dailyCommand = null;
        }
        manager = null;
        plugin = null;
    }

    public static boolean openMainMenu(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        if (manager != null) {
            manager.openMainMenu(player);
        } else {
            plugin.mainMenu().ifPresent(menu -> menu.open(player));
        }
        return true;
    }

    public static boolean openDailyQuests(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        if (manager != null) {
            manager.openQuests(player);
        }
        return true;
    }

    public static boolean openBattlePass(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        plugin.battlePassManager().ifPresent(bpm -> bpm.openBattlePass(player));
        return true;
    }

    public static boolean openMilestones(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        plugin.milestoneManager().ifPresent(mm -> mm.openCategories(player));
        return true;
    }

    public static boolean refreshQuests(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
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

    public static boolean trackMode(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
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

    private static String[] parseAliases(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new String[]{"quests", "quete", "q"};
        String[] split = raw.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }
}
