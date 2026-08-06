package su.nightexpress.quests.hook.impl;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import su.nightexpress.quests.hook.ISkyblockHook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import com.bgsoftware.superiorskyblock.api.events.IslandDisbandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandKickEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandQuitEvent;
import su.nightexpress.quests.QuestsPlugin;

public class SuperiorSkyblockHookImpl implements ISkyblockHook, Listener {

    private QuestsPlugin plugin;

    @Override
    public void registerListeners(@NotNull QuestsPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandDisband(IslandDisbandEvent event) {
        if (plugin == null) return;
        UUID islandUuid = event.getIsland().getUniqueId();
        plugin.getIslandManager().purgeIslandData(islandUuid);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandQuit(IslandQuitEvent event) {
        if (plugin == null) return;
        plugin.getIslandManager().invalidatePlayer(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onIslandKick(IslandKickEvent event) {
        if (plugin == null) return;
        plugin.getIslandManager().invalidatePlayer(event.getTarget().getUniqueId());
    }

    @Override
    @Nullable
    public UUID getIslandUUID(@NotNull Player player) {
        var superiorPlayer = SuperiorSkyblockAPI.getPlayer(player.getUniqueId());
        if (superiorPlayer == null) return null;
        var island = superiorPlayer.getIsland();
        return island != null ? island.getUniqueId() : null;
    }

    @Override
    @NotNull
    public List<Player> getOnlineMembers(@NotNull UUID islandUuid) {
        var island = SuperiorSkyblockAPI.getGrid().getIslandByUUID(islandUuid);
        if (island == null) return Collections.emptyList();

        List<Player> players = new ArrayList<>();
        for (var member : island.getIslandMembers(true)) {
            Player p = member.asPlayer();
            if (p != null && p.isOnline()) {
                players.add(p);
            }
        }
        return players;
    }

    @Override
    @Nullable
    public String getIslandLeaderName(@NotNull UUID islandUuid) {
        var island = SuperiorSkyblockAPI.getGrid().getIslandByUUID(islandUuid);
        if (island == null) return null;
        var owner = island.getOwner();
        if (owner == null) return null;
        var name = owner.getName();
        if (name == null || name.isEmpty()) {
            var offlinePlayer = Bukkit.getOfflinePlayer(owner.getUniqueId());
            return offlinePlayer.getName();
        }
        return name;
    }
}
