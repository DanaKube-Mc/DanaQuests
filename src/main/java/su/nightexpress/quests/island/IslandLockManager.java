package su.nightexpress.quests.island;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IslandLockManager {
    private final Map<UUID, UUID> locks = new ConcurrentHashMap<>();

    public boolean acquireLock(UUID islandUuid, UUID playerUuid) {
        UUID currentHolder = locks.putIfAbsent(islandUuid, playerUuid);
        return currentHolder == null || currentHolder.equals(playerUuid);
    }

    public void releaseLock(UUID islandUuid) {
        locks.remove(islandUuid);
    }

    public void releasePlayerLock(UUID playerUuid) {
        locks.entrySet().removeIf(entry -> entry.getValue().equals(playerUuid));
    }

    public UUID getLockHolder(UUID islandUuid) {
        return locks.get(islandUuid);
    }

    public UUID getPlayerLockedIsland(UUID playerUuid) {
        for (Map.Entry<UUID, UUID> entry : locks.entrySet()) {
            if (entry.getValue().equals(playerUuid)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void clear() {
        locks.clear();
    }
}
