package su.nightexpress.quests.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.util.QuestUtils;

public class WorldGuardHook {

    private static Boolean available = null;

    public static boolean isAvailable() {
        if (available == null) {
            available = QuestUtils.isIntegrationAvailable("WorldGuard");
        }
        return available;
    }

    public static boolean isInRegion(@NotNull Player player, @NotNull String regionId) {
        if (!isAvailable()) return false;
        
        try {
            Location loc = player.getLocation();
            com.sk89q.worldedit.util.Location weLoc = BukkitAdapter.adapt(loc);
            RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
            RegionQuery query = container.createQuery();
            ApplicableRegionSet set = query.getApplicableRegions(weLoc);
            for (ProtectedRegion region : set) {
                if (region.getId().equalsIgnoreCase(regionId)) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        
        return false;
    }
}
