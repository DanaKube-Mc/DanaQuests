package su.nightexpress.quests.island.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.island.IslandManager;

public class IslandGenericListener implements Listener {

    private final QuestsPlugin plugin;
    private final IslandManager islandManager;

    public IslandGenericListener(@NotNull QuestsPlugin plugin, @NotNull IslandManager islandManager) {
        this.plugin = plugin;
        this.islandManager = islandManager;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        islandManager.handleInventoryClose(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        islandManager.handlePlayerQuit(player);
    }
}
