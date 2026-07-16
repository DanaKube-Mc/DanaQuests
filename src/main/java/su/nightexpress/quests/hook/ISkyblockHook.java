package su.nightexpress.quests.hook;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface ISkyblockHook {
    @Nullable
    UUID getIslandUUID(@NotNull Player player);

    @NotNull
    List<Player> getOnlineMembers(@NotNull UUID islandUuid);

    @Nullable
    String getIslandLeaderName(@NotNull UUID islandUuid);
}
