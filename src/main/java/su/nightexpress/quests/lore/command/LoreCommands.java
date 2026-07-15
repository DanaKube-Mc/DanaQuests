package su.nightexpress.quests.lore.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.quests.QuestsPlugin;

public class LoreCommands {

    private static QuestsPlugin plugin;

    public static void load(@NotNull QuestsPlugin questsPlugin) {
        plugin = questsPlugin;
    }

    public static void shutdown() {
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
