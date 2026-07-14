package su.nightexpress.quests.hook.impl;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

import su.nightexpress.quests.hook.ISkyblockHook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SuperiorSkyblockHookImpl implements ISkyblockHook {

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
}
