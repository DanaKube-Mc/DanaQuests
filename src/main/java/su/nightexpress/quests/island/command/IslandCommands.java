package su.nightexpress.quests.island.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;

public class IslandCommands {

    private static QuestsPlugin plugin;
    private static NightCommand command;

    public static void load(@NotNull QuestsPlugin questsPlugin) {
        plugin = questsPlugin;

        if (Config.FEATURES_ISLAND_QUESTS_ENABLED.get()) {
            command = NightCommand.hub(plugin, Config.FEATURES_ISLAND_QUESTS_ALIASES.get(), builder -> builder
                .localized(Lang.COMMAND_ISLAND_NAME)
                .permission(Perms.COMMAND_ISLAND)
                .description(Lang.COMMAND_ISLAND_DESC)
                .executes(IslandCommands::openIslandMenu)
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

    public static boolean openIslandMenu(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        if (plugin.getIslandManager() != null) {
            plugin.getIslandManager().openMenu(player);
        }
        return true;
    }
}
