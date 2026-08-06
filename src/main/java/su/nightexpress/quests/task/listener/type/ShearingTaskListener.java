package su.nightexpress.quests.task.listener.type;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.task.adapter.AdapterFamily;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.task.listener.TaskListener;
import su.nightexpress.quests.util.StackerHook;

public class ShearingTaskListener extends TaskListener<Entity, AdapterFamily<Entity>> {

    public ShearingTaskListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<Entity, AdapterFamily<Entity>> taskType) {
        super(plugin, manager, taskType);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTaskShear(PlayerShearEntityEvent event) {
        Player player = event.getPlayer();
        if (!this.manager.canDoTasks(player)) return;

        Entity entity = event.getEntity();
        if (this.manager.isSpawnerMob(entity)) return;

        int amount = StackerHook.getEntityStackAmount(entity);
        this.progressQuests(player, entity, amount);
    }
}
