package su.nightexpress.quests.event.data;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.event.definition.EventTier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventUserData {

    private final UUID playerUuid;
    private final String eventId;
    private long totalScore;
    private final Set<Integer> claimedTiers;
    private long lastUpdated;

    public EventUserData(@NotNull UUID playerUuid, @NotNull String eventId) {
        this(playerUuid, eventId, 0L, new HashSet<>(), System.currentTimeMillis());
    }

    public EventUserData(@NotNull UUID playerUuid, @NotNull String eventId, long totalScore, @NotNull Set<Integer> claimedTiers, long lastUpdated) {
        this.playerUuid = playerUuid;
        this.eventId = eventId;
        this.totalScore = totalScore;
        this.claimedTiers = new HashSet<>(claimedTiers);
        this.lastUpdated = lastUpdated;
    }

    public void addScore(long amount) {
        if (amount <= 0) return;
        this.totalScore += amount;
        this.lastUpdated = System.currentTimeMillis();
    }

    public void setScore(long score) {
        this.totalScore = Math.max(0, score);
        this.lastUpdated = System.currentTimeMillis();
    }

    public boolean canClaim(@NotNull EventTier tier) {
        return tier.isUnlocked(this.totalScore) && !isTierClaimed(tier.getTierNumber());
    }

    public boolean claimTier(int tierNumber) {
        if (this.claimedTiers.contains(tierNumber)) {
            return false;
        }
        boolean added = this.claimedTiers.add(tierNumber);
        if (added) {
            this.lastUpdated = System.currentTimeMillis();
        }
        return added;
    }

    public boolean isTierClaimed(int tierNumber) {
        return this.claimedTiers.contains(tierNumber);
    }

    public void reset() {
        this.totalScore = 0L;
        this.claimedTiers.clear();
        this.lastUpdated = System.currentTimeMillis();
    }

    @NotNull
    public String getClaimedTiersSerialized() {
        if (this.claimedTiers.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int tier : this.claimedTiers) {
            if (!sb.isEmpty()) sb.append(",");
            sb.append(tier);
        }
        return sb.toString();
    }

    @NotNull
    public static Set<Integer> deserializeClaimedTiers(@NotNull String serialized) {
        Set<Integer> set = new HashSet<>();
        if (serialized.trim().isEmpty()) return set;
        String[] parts = serialized.split(",");
        for (String part : parts) {
            try {
                set.add(Integer.parseInt(part.trim()));
            } catch (NumberFormatException ignored) {}
        }
        return set;
    }

    @NotNull
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @NotNull
    public String getEventId() {
        return eventId;
    }

    public long getTotalScore() {
        return totalScore;
    }

    @NotNull
    public Set<Integer> getClaimedTiers() {
        return Collections.unmodifiableSet(claimedTiers);
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
