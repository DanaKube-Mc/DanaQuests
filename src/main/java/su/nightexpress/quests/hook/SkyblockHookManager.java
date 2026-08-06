package su.nightexpress.quests.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class SkyblockHookManager {

    private static ISkyblockHook hookInstance;
    private static boolean checked = false;

    @Nullable
    public static ISkyblockHook getHook() {
        if (!checked) {
            if (Bukkit.getPluginManager().getPlugin("SuperiorSkyblock2") != null) {
                try {
                    hookInstance = (ISkyblockHook) Class.forName("su.nightexpress.quests.hook.impl.SuperiorSkyblockHookImpl")
                            .getDeclaredConstructor()
                            .newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            checked = true;
        }
        return hookInstance;
    }

    public static boolean isHooked() {
        return getHook() != null;
    }

    @Nullable
    public static UUID getIslandUUID(@NotNull Player player) {
        ISkyblockHook hook = getHook();
        return hook != null ? hook.getIslandUUID(player) : null;
    }

    @NotNull
    public static List<Player> getOnlineMembers(@NotNull UUID islandUuid) {
        ISkyblockHook hook = getHook();
        return hook != null ? hook.getOnlineMembers(islandUuid) : Collections.emptyList();
    }
}
