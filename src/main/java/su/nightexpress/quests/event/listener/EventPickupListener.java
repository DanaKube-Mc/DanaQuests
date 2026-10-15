package su.nightexpress.quests.event.listener;

import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.manager.AbstractListener;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.event.EventQuestManager;
import su.nightexpress.quests.event.definition.EventQuest;

public class EventPickupListener extends AbstractListener<QuestsPlugin> {

    private final EventQuestManager eventManager;

    public EventPickupListener(@NotNull QuestsPlugin plugin, @NotNull EventQuestManager eventManager) {
        super(plugin);
        this.eventManager = eventManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!this.plugin.getTaskManager().canDoTasks(player)) return;

        Item itemEntity = event.getItem();
        ItemStack itemStack = itemEntity.getItemStack();
        int remaining = event.getRemaining();
        int pickedUpAmount = itemStack.getAmount() - remaining;
        if (pickedUpAmount <= 0) {
            pickedUpAmount = itemStack.getAmount();
        }

        handlePickup(player, itemEntity, pickedUpAmount);
    }

    public boolean isIgnoredDrop(@NotNull Item itemEntity) {
        if (this.plugin != null && this.plugin.getTaskManager() != null) {
            return this.plugin.getTaskManager().isPlayerOriginated(itemEntity);
        }
        org.bukkit.NamespacedKey key = new org.bukkit.NamespacedKey("danaquests", "player_originated");
        return su.nightexpress.nightcore.util.PDCUtil.getBoolean(itemEntity, key).orElse(false)
            || su.nightexpress.nightcore.util.PDCUtil.getBoolean(itemEntity.getItemStack(), key).orElse(false);
    }

    public void handlePickup(@NotNull Player player, @NotNull Item itemEntity, int amount) {
        if (isIgnoredDrop(itemEntity)) {
            return;
        }

        ItemStack itemStack = itemEntity.getItemStack();
        Material mat = itemStack.getType();

        for (EventQuest quest : this.eventManager.getActiveQuests()) {
            if ("PICKUP_ITEM".equalsIgnoreCase(quest.getTargetType()) && quest.matchesMaterial(mat)) {
                this.eventManager.progressEvent(player, quest, amount);
            }
        }
    }
}
