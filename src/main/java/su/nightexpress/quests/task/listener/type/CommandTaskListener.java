package su.nightexpress.quests.task.listener.type;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.task.adapter.AdapterFamily;
import su.nightexpress.quests.task.listener.TaskListener;

public class CommandTaskListener extends TaskListener<String, AdapterFamily<String>> {

    public CommandTaskListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<String, AdapterFamily<String>> taskType) {
        super(plugin, manager, taskType);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!this.manager.canDoTasks(player)) return;

        String rawMessage = event.getMessage().trim();
        if (rawMessage.isEmpty()) return;

        String cmdNoSlash = rawMessage.startsWith("/") ? rawMessage.substring(1).trim().toLowerCase() : rawMessage.toLowerCase();
        String cmdWithSlash = "/" + cmdNoSlash;

        // Progress for both with and without leading slash to match any config style
        this.progressQuests(player, cmdNoSlash, 1);
        this.progressQuests(player, cmdWithSlash, 1);
    }
}
