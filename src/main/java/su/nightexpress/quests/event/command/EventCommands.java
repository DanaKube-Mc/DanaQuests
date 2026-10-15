package su.nightexpress.quests.event.command;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.commands.context.CommandContext;
import su.nightexpress.nightcore.commands.context.ParsedArguments;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.event.EventQuestManager;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;

import java.util.Collection;

public class EventCommands {

    public static boolean listEvents(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        QuestsPlugin plugin = QuestsPlugin.getPlugin(QuestsPlugin.class);
        EventQuestManager manager = plugin.getEventQuestManager();
        if (manager == null) return false;

        handleList(context.getSender(), manager);
        return true;
    }

    public static void handleList(@NotNull CommandSender sender, @NotNull EventQuestManager manager) {
        Collection<EventQuest> quests = manager.getQuests().values();
        sender.sendMessage("§6--- Événements de Quêtes (" + quests.size() + ") ---");
        for (EventQuest q : quests) {
            String status = q.isActive() ? "§a[Actif]" : "§c[Inactif/Terminé]";
            sender.sendMessage(" §8• §e" + q.getId() + " §7: " + status + " §7(Temps : §f" + q.getSchedule().getTimeLeftFormatted() + "§7)");
        }
    }

    public static boolean openMenu(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        if (!context.isPlayer()) {
            context.errorPlayerOnly();
            return false;
        }

        Player player = context.getPlayerOrThrow();
        QuestsPlugin plugin = QuestsPlugin.getPlugin(QuestsPlugin.class);
        EventQuestManager manager = plugin.getEventQuestManager();
        if (manager == null) return false;

        String id = arguments.contains("id") ? arguments.getString("id") : null;
        handleMenu(player, manager, id);
        return true;
    }

    public static void handleMenu(@NotNull Player player, @NotNull EventQuestManager manager, @Nullable String questId) {
        if (questId == null || questId.isBlank()) {
            manager.openMainMenu(player);
        } else {
            manager.openEventMenu(player, questId);
        }
    }

    public static boolean startEvent(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        QuestsPlugin plugin = QuestsPlugin.getPlugin(QuestsPlugin.class);
        EventQuestManager manager = plugin.getEventQuestManager();
        if (manager == null) return false;

        String questId = arguments.getString("id");
        Integer duration = arguments.contains("duration") ? arguments.getInt("duration") : null;

        handleStart(context.getSender(), manager, questId, duration);
        return true;
    }

    public static void handleStart(@NotNull CommandSender sender, @NotNull EventQuestManager manager, @NotNull String questId, @Nullable Integer durationHours) {
        EventQuest quest = manager.getQuest(questId);
        if (quest == null) {
            sender.sendMessage("§cÉvénement introuvable : " + questId);
            return;
        }

        int duration = durationHours != null && durationHours > 0 ? durationHours : (quest.getSchedule().getDurationHours() > 0 ? quest.getSchedule().getDurationHours() : 24);
        quest.getSchedule().start(System.currentTimeMillis(), duration);

        sender.sendMessage("§aL'événement §e" + quest.getName() + " §aa été démarré pour une durée de §e" + duration + "h §a!");
    }

    public static boolean stopEvent(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        QuestsPlugin plugin = QuestsPlugin.getPlugin(QuestsPlugin.class);
        EventQuestManager manager = plugin.getEventQuestManager();
        if (manager == null) return false;

        String questId = arguments.getString("id");
        handleStop(context.getSender(), manager, questId);
        return true;
    }

    public static void handleStop(@NotNull CommandSender sender, @NotNull EventQuestManager manager, @NotNull String questId) {
        EventQuest quest = manager.getQuest(questId);
        if (quest == null) {
            sender.sendMessage("§cÉvénement introuvable : " + questId);
            return;
        }

        quest.getSchedule().stop();
        sender.sendMessage("§cLe déroulement de l'événement §e" + quest.getName() + " §ca été arrêté. Les récompenses restent réclamables.");
    }

    public static boolean resetPlayer(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        QuestsPlugin plugin = QuestsPlugin.getPlugin(QuestsPlugin.class);
        EventQuestManager manager = plugin.getEventQuestManager();
        if (manager == null) return false;

        String questId = arguments.getString("id");
        String playerName = arguments.getString("player");

        handleReset(context.getSender(), manager, questId, playerName);
        return true;
    }

    public static void handleReset(@NotNull CommandSender sender, @NotNull EventQuestManager manager, @NotNull String questId, @NotNull String playerName) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        manager.resetUserData(target.getUniqueId(), questId);
        sender.sendMessage("§aProgression réinitialisée pour §e" + playerName + " §adans l'événement §e" + questId + "§a.");
    }

    public static boolean addScore(@NotNull CommandContext context, @NotNull ParsedArguments arguments) {
        QuestsPlugin plugin = QuestsPlugin.getPlugin(QuestsPlugin.class);
        EventQuestManager manager = plugin.getEventQuestManager();
        if (manager == null) return false;

        String questId = arguments.getString("id");
        String playerName = arguments.getString("player");
        long amount = arguments.getInt("amount");

        handleAddScore(context.getSender(), manager, questId, playerName, amount);
        return true;
    }

    public static void handleAddScore(@NotNull CommandSender sender, @NotNull EventQuestManager manager, @NotNull String questId, @NotNull String playerName, long amount) {
        OfflinePlayer target = Bukkit.getOfflinePlayer(playerName);
        EventUserData data = manager.getUserData(target.getUniqueId(), questId);
        data.addScore(amount);
        manager.saveUserData(data);

        sender.sendMessage("§a+" + amount + " points ajoutés à §e" + playerName + " §adans l'événement §e" + questId + " §7(Nouveau total : §6" + data.getTotalScore() + "§7).");
    }
}
