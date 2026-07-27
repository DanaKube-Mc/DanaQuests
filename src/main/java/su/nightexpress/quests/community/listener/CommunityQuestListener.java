package su.nightexpress.quests.community.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.community.CommunityQuestManager;

public class CommunityQuestListener implements Listener {

    private final QuestsPlugin plugin;
    private final CommunityQuestManager manager;

    public CommunityQuestListener(@NotNull QuestsPlugin plugin, @NotNull CommunityQuestManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        this.manager.addGlobalProgress(player, 1.0, "BLOCK_BREAK_GLOBAL");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(@NotNull EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        this.manager.addGlobalProgress(killer, 1.0, "KILL_MOB_GLOBAL");
    }
}
