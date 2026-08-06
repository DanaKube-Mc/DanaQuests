package su.nightexpress.quests.task.listener.type;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.papermc.paper.math.Position;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.structure.Structure;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.hook.WorldGuardHook;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.task.TaskType;
import su.nightexpress.quests.task.adapter.AdapterFamily;
import su.nightexpress.quests.task.listener.TaskListener;
import su.nightexpress.quests.user.QuestUser;
import su.nightexpress.quests.quest.data.QuestData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LocationListener extends TaskListener<String, AdapterFamily<String>> {

    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public LocationListener(@NotNull QuestsPlugin plugin, @NotNull TaskManager manager, @NotNull TaskType<String, AdapterFamily<String>> taskType) {
        super(plugin, manager, taskType);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!this.manager.canDoTasks(player)) return;

        Location to = event.getTo();
        if (to == null) return;

        Location from = lastLocations.get(player.getUniqueId());
        if (from != null && from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return; 
        }
        lastLocations.put(player.getUniqueId(), to.clone());

        Biome biome = to.getBlock().getBiome();
        String biomeKey = "biome:" + biome.getKey().toString();
        this.progressQuests(player, biomeKey);

        if (WorldGuardHook.isAvailable()) {
            try {
                com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(to);
                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                RegionQuery query = container.createQuery();
                ApplicableRegionSet set = query.getApplicableRegions(weLoc);
                for (ProtectedRegion region : set) {
                    this.progressQuests(player, "worldguard:" + region.getId());
                }
            } catch (Throwable ignored) {}
        }

        World world = to.getWorld();
        this.progressQuests(player, "world:" + world.getName());

        Position paperPos = Position.block(to);
        for (Structure structure : Registry.STRUCTURE) {
            try {
                if (world.hasStructureAt(paperPos, structure)) {
                    String structKey = "structure:" + Registry.STRUCTURE.getKey(structure).toString();
                    this.progressQuests(player, structKey);
                }
            } catch (Throwable ignored) {}
        }

        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        for (QuestData questData : user.getQuestDatas()) {
            if (questData.isActive() && !questData.isCompleted() && !questData.isExpired()) {
                for (String target : questData.getObjectiveCounterMap().keySet()) {
                    checkCoordsTarget(player, target, to);
                }
            }
        }
        
        if (this.plugin.getLoreManager() != null) {
            this.plugin.getLoreManager().getActiveQuestTargets(player).forEach(target -> checkCoordsTarget(player, target, to));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        if (!this.manager.canDoTasks(player)) return;

        World world = player.getWorld();
        this.progressQuests(player, "world:" + world.getName());
    }

    private void checkCoordsTarget(@NotNull Player player, @NotNull String target, @NotNull Location playerLoc) {
        if (target.startsWith("coords:")) {
            try {
                String[] parts = target.substring(7).split(",");
                if (parts.length >= 5) {
                    String worldName = parts[0];
                    double x = Double.parseDouble(parts[1]);
                    double y = Double.parseDouble(parts[2]);
                    double z = Double.parseDouble(parts[3]);
                    double radius = Double.parseDouble(parts[4]);

                    if (playerLoc.getWorld().getName().equalsIgnoreCase(worldName)) {
                        double dx = playerLoc.getX() - x;
                        double dy = playerLoc.getY() - y;
                        double dz = playerLoc.getZ() - z;
                        double distSq = dx * dx + dy * dy + dz * dz;
                        if (distSq <= radius * radius) {
                            this.progressQuests(player, target);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }
}
