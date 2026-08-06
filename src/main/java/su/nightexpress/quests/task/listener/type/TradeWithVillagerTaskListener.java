package su.nightexpress.quests.task.listener.type;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import io.papermc.paper.event.player.PlayerTradeEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.task.adapter.AdapterFamily;
import su.nightexpress.quests.task.listener.TaskListener;

public class TradeWithVillagerTaskListener extends TaskListener<String, AdapterFamily<String>> {

    public TradeWithVillagerTaskListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<String, AdapterFamily<String>> taskType) {
        super(plugin, manager, taskType);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTrade(PlayerTradeEvent event) {
        Player player = event.getPlayer();
        if (!this.manager.canDoTasks(player)) return;

        if (event.getTrade() != null && event.getTrade().getResult() != null) {
            String itemKey = "minecraft:" + event.getTrade().getResult().getType().name().toLowerCase();
            this.progressQuests(player, itemKey, 1);
        }

        if (event.getMerchant() != null) {
            String entityKey = "minecraft:" + event.getMerchant().getType().name().toLowerCase();
            this.progressQuests(player, entityKey, 1);
        }
    }
}
