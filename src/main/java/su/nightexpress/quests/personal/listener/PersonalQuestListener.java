package su.nightexpress.quests.personal.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.personal.PersonalQuestManager;

public class PersonalQuestListener implements Listener {

    private final QuestsPlugin plugin;
    private final PersonalQuestManager manager;

    public PersonalQuestListener(@NotNull QuestsPlugin plugin, @NotNull PersonalQuestManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String blockType = event.getBlock().getType().name();
        this.manager.handleProgress(player, "BREAK_BLOCK", blockType, 1);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        String entityType = event.getEntityType().name();
        this.manager.handleProgress(killer, "KILL_MOB", entityType, 1);
    }
}
