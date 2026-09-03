package su.nightexpress.quests.task.listener.type;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.task.adapter.AdapterFamily;
import su.nightexpress.quests.task.listener.TaskListener;

public class PickupItemTaskListener extends TaskListener<ItemStack, AdapterFamily<ItemStack>> {

    private final NamespacedKey droppedKey;

    public PickupItemTaskListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<ItemStack, AdapterFamily<ItemStack>> taskType) {
        super(plugin, manager, taskType);
        this.droppedKey = new NamespacedKey(plugin, "player_dropped_item");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Item item = event.getItemDrop();
        PDCUtil.set(item, this.droppedKey, true);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!this.manager.canDoTasks(player)) return;

        Item itemEntity = event.getItem();
        if (!Config.ANTI_ABUSE_COUNT_PLAYER_BLOCKS.get()) {
            if (PDCUtil.getBoolean(itemEntity, this.droppedKey).orElse(false)) {
                return;
            }
        }

        ItemStack itemStack = itemEntity.getItemStack();
        int remaining = event.getRemaining();
        int pickedUpAmount = itemStack.getAmount() - remaining;
        if (pickedUpAmount <= 0) {
            pickedUpAmount = itemStack.getAmount();
        }

        this.progressQuests(player, itemStack, pickedUpAmount);
    }
}
