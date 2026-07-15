package su.nightexpress.quests.task.listener.type;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Beehive;
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

public class HarvestItemTaskListener extends TaskListener<String, AdapterFamily<String>> {

    public HarvestItemTaskListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<String, AdapterFamily<String>> taskType) {
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

        if (originalMaterial == Material.BEEHIVE || originalMaterial == Material.BEE_NEST) {
            if (block.getBlockData() instanceof Beehive beehive) {
                if (beehive.getHoneyLevel() == 5) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        if (!block.getLocation().isChunkLoaded()) return;
                        if (block.getBlockData() instanceof Beehive newBeehive) {
                            if (newBeehive.getHoneyLevel() == 0) {
                                this.progressQuests(player, "minecraft:honey_bottle");
                                this.progressQuests(player, "minecraft:honeycomb");
                                this.progressQuests(player, "minecraft:" + originalMaterial.name().toLowerCase());
                            }
                        }
                    });
                }
            }
        }
        else if (originalMaterial == Material.COMPOSTER) {
            if (block.getBlockData() instanceof Levelled composter) {
                if (composter.getLevel() == 8) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        if (!block.getLocation().isChunkLoaded()) return;
                        if (block.getBlockData() instanceof Levelled newComposter) {
                            if (newComposter.getLevel() == 0) {
                                this.progressQuests(player, "minecraft:bone_meal");
                                this.progressQuests(player, "minecraft:composter");
                            }
                        }
                    });
                }
            }
        }
        else if (originalMaterial == Material.SWEET_BERRY_BUSH) {
            if (block.getBlockData() instanceof Ageable bush) {
                if (bush.getAge() == 3) {
                    Bukkit.getScheduler().runTask(this.plugin, () -> {
                        if (!block.getLocation().isChunkLoaded()) return;
                        if (block.getBlockData() instanceof Ageable newBush) {
                            if (newBush.getAge() < 3) {
                                this.progressQuests(player, "minecraft:sweet_berries");
                                this.progressQuests(player, "minecraft:sweet_berry_bush");
                            }
                        }
                    });
                }
            }
        }
    }
}
