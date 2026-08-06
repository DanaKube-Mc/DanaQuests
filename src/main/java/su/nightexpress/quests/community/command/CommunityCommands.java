package su.nightexpress.quests.community.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.quests.community.CommunityQuestManager;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;

public class CommunityCommands {

    private static QuestsPlugin plugin;
    private static NightCommand command;

    public static void load(@NotNull QuestsPlugin questsPlugin) {
        plugin = questsPlugin;

        if (Config.FEATURES_COMMUNITY_QUESTS_ENABLED.get() && Config.FEATURES_COMMUNITY_QUESTS_STANDALONE_COMMAND.get()) {
            command = NightCommand.hub(plugin, Config.FEATURES_COMMUNITY_QUESTS_ALIASES.get(), builder -> builder
                .localized(Lang.COMMAND_COMMUNITY_NAME)
                .permission(Perms.COMMAND_COMMUNITY)
                .description(Lang.COMMAND_COMMUNITY_DESC)
                .executes(CommunityCommands::openLeaderboardMenu)
            );
            command.register();
        }
    }

    public static void shutdown() {
        if (command != null) {
            command.unregister();
            command = null;
        }
        plugin = null;
    }

    public static boolean openLeaderboardMenu(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        CommunityQuestManager manager = plugin.getCommunityQuestManager();
        if (manager != null) {
            manager.openLeaderboardMenu(player);
        }
        return true;
    }

    public static boolean startEvent(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String questId = arguments.getString("quest_id");
        Integer durationHours = arguments.contains("duration") ? arguments.getInt("duration") : null;
        Double target = arguments.contains("target") ? arguments.getDouble("target") : null;

        CommunityQuestManager manager = plugin.getCommunityQuestManager();
        if (manager != null) {
            manager.startEvent(context.getSender(), questId, durationHours, target);
        }
        return true;
    }

    public static boolean stopEvent(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        CommunityQuestManager manager = plugin.getCommunityQuestManager();
        if (manager != null) {
            manager.stopEvent(context.getSender());
        }
        return true;
    }

    public static boolean contribute(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        double amount = arguments.getDouble("amount");

        if (amount <= 0) {
            Lang.COMMUNITY_DEPOSIT_INVALID_AMOUNT.message().send(player);
            return false;
        }

        CommunityQuestManager manager = plugin.getCommunityQuestManager();
        if (manager != null) {
            manager.deposit(player, amount);
        }
        return true;
    }
}
