package su.nightexpress.quests.lore.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;

public class LoreCommands {

    public static final String DEFAULT_ALIAS = "lorequests";

    private static QuestsPlugin plugin;
    private static NightCommand command;

    public static void load(@NotNull QuestsPlugin questsPlugin) {
        plugin = questsPlugin;

        if (Config.FEATURES_LORE_ENABLED.get() && Config.FEATURES_LORE_STANDALONE_COMMAND.get()) {
            command = NightCommand.hub(plugin, Config.FEATURES_LORE_ALIASES.get(), builder -> builder
                .localized(Lang.COMMAND_QUESTS_LORE_NAME)
                .permission(Perms.COMMAND_QUESTS_LORE)
                .description(Lang.COMMAND_QUESTS_LORE_DESC)
                .executes(LoreCommands::openLoreMenu)
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

    public static boolean openLoreMenu(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        if (plugin.getLoreManager() != null) {
            plugin.getLoreManager().openLoreMenu(player);
        }
        return true;
    }
}
