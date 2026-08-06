package su.nightexpress.quests.task.listener.type;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.task.adapter.AdapterFamily;
import su.nightexpress.quests.task.listener.TaskListener;

public class StripLogTaskListener extends TaskListener<String, AdapterFamily<String>> {

    public StripLogTaskListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<String, AdapterFamily<String>> taskType) {
        super(plugin, manager, taskType);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        Player player = event.getPlayer();
        if (!this.manager.canDoTasks(player)) return;

        Material originalMaterial = block.getType();
        String matName = originalMaterial.name();

        if ((matName.endsWith("_LOG") || matName.endsWith("_WOOD") || matName.endsWith("_STEM") || matName.endsWith("_HYPHAE")) 
                && !matName.contains("STRIPPED")) {

            Bukkit.getScheduler().runTask(this.plugin, () -> {
                if (!block.getLocation().isChunkLoaded()) return;
                Material newMaterial = block.getType();
                if (newMaterial.name().contains("STRIPPED") && newMaterial.name().contains(matName.replace("LOG", "").replace("WOOD", "").replace("STEM", "").replace("HYPHAE", ""))) {
                    String targetKey = "minecraft:" + originalMaterial.name().toLowerCase();
                    this.progressQuests(player, targetKey);
                }
            });
        }
    }
}
