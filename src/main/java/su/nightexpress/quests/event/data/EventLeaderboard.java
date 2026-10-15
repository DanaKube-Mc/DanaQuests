package su.nightexpress.quests.event.data;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class EventLeaderboard {

    private final String eventId;
    private final EventDataDao dao;
    private List<LeaderboardEntry> topEntries;
    private final Map<UUID, Integer> rankCache;
    private Function<UUID, String> nameResolver;

    public EventLeaderboard(@NotNull String eventId, @Nullable EventDataDao dao) {
        this.eventId = eventId;
        this.dao = dao;
        this.topEntries = new ArrayList<>();
        this.rankCache = new ConcurrentHashMap<>();
        this.nameResolver = uuid -> {
            try {
                OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                String name = op.getName();
                return name != null ? name : uuid.toString().substring(0, 8);
            } catch (Exception e) {
                return uuid.toString().substring(0, 8);
            }
        };
    }

    public void setNameResolver(@NotNull Function<UUID, String> resolver) {
        this.nameResolver = resolver;
    }

    public void refreshSync() {
        if (this.dao == null) return;
        List<LeaderboardEntry> entries = this.dao.loadTopScores(this.eventId, 10);
        this.topEntries = new ArrayList<>(entries);
        this.rankCache.clear();
        for (int i = 0; i < entries.size(); i++) {
            this.rankCache.put(entries.get(i).playerUuid(), i + 1);
        }
    }

    public int getPlayerRank(@NotNull UUID playerUuid, long totalScore) {
        if (totalScore <= 0) return -1;
        Integer cached = this.rankCache.get(playerUuid);
        if (cached != null) return cached;
        if (this.dao != null) {
            int rank = this.dao.getPlayerRank(playerUuid, this.eventId, totalScore);
            if (rank > 0) {
                this.rankCache.put(playerUuid, rank);
            }
            return rank;
        }
        return -1;
    }

    @NotNull
    public List<LeaderboardEntry> getTopEntries() {
        return Collections.unmodifiableList(topEntries);
    }

    @NotNull
    public String getTopPlayerName(int rank) {
        int index = rank - 1;
        if (index < 0 || index >= this.topEntries.size()) {
            return "---";
        }
        UUID uuid = this.topEntries.get(index).playerUuid();
        return this.nameResolver.apply(uuid);
    }

    public long getTopPlayerScore(int rank) {
        int index = rank - 1;
        if (index < 0 || index >= this.topEntries.size()) {
            return 0L;
        }
        return this.topEntries.get(index).score();
    }

    @NotNull
    public String getEventId() {
        return eventId;
    }
}
