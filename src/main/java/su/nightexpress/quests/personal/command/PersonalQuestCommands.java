package su.nightexpress.quests.personal.command;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.personal.PersonalQuestManager;
import su.nightexpress.quests.personal.definition.RpgCategory;
import su.nightexpress.quests.user.QuestUser;

public class PersonalQuestCommands {

    private static QuestsPlugin plugin;

    public static void load(@NotNull QuestsPlugin questsPlugin) {
        plugin = questsPlugin;
    }

    public static void shutdown() {
        plugin = null;
    }

    public static boolean openPersonalMenu(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        PersonalQuestManager manager = plugin.getPersonalQuestManager();
        if (manager != null) {
            manager.openPersonalMenu(player);
        }
        return true;
    }

    public static boolean setLevel(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String playerName = arguments.getString("player");
        String categoryId = arguments.getString("category");
        int level = arguments.getInt("level");

        if (level < 1) {
            context.getSender().sendMessage("§cLe niveau doit être supérieur ou égal à 1.");
            return false;
        }

        PersonalQuestManager manager = plugin.getPersonalQuestManager();
        if (manager == null) return false;

        RpgCategory category = manager.getCategory(categoryId);
        if (category == null) {
            context.getSender().sendMessage("§cCatégorie RPG invalide !");
            return false;
        }

        plugin.getUserManager().manageUser(playerName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            user.getRpgCategoryLevels().put(category.getId(), level);
            user.getRpgCategoryXP().put(category.getId(), 0.0); // Reset validations progress
            plugin.getUserManager().save(user);

            context.getSender().sendMessage("§aNiveau de §e" + user.getName() + " §adans §e" + category.getDisplayName() + " §adéfini sur §e" + level + "§a.");
        });

        return true;
    }

    public static boolean addLevel(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        String playerName = arguments.getString("player");
        String categoryId = arguments.getString("category");
        int levelsToAdd = arguments.getInt("level");

        if (levelsToAdd < 1) {
            context.getSender().sendMessage("§cLe nombre de niveaux à ajouter doit être supérieur ou égal à 1.");
            return false;
        }

        PersonalQuestManager manager = plugin.getPersonalQuestManager();
        if (manager == null) return false;

        RpgCategory category = manager.getCategory(categoryId);
        if (category == null) {
            context.getSender().sendMessage("§cCatégorie RPG invalide !");
            return false;
        }

        plugin.getUserManager().manageUser(playerName, user -> {
            if (user == null) {
                context.errorBadPlayer();
                return;
            }

            int currentLevel = user.getRPGLevel(category.getId());
            int newLevel = currentLevel + levelsToAdd;
            user.getRpgCategoryLevels().put(category.getId(), newLevel);
            user.getRpgCategoryXP().put(category.getId(), 0.0); // Reset validations progress
            plugin.getUserManager().save(user);

            context.getSender().sendMessage("§aAjouté §e" + levelsToAdd + " §aniveaux à §e" + user.getName() + " §adans §e" + category.getDisplayName() + " §a(Nouveau niveau: §e" + newLevel + "§a).");
        });

        return true;
    }

    public static boolean executePersonalCommand(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!arguments.contains("action")) {
            return openPersonalMenu(context, arguments);
        }

        String action = arguments.getString("action");
        if (action.equalsIgnoreCase("setlevel")) {
            if (!arguments.contains("player") || !arguments.contains("category") || !arguments.contains("level")) {
                context.getSender().sendMessage("§cUsage: /quests personal setlevel <player> <category> <level>");
                return false;
            }
            return setLevel(context, arguments);
        } else if (action.equalsIgnoreCase("addlevel")) {
            if (!arguments.contains("player") || !arguments.contains("category") || !arguments.contains("level")) {
                context.getSender().sendMessage("§cUsage: /quests personal addlevel <player> <category> <level>");
                return false;
            }
            return addLevel(context, arguments);
        }

        return openPersonalMenu(context, arguments);
    }
}
