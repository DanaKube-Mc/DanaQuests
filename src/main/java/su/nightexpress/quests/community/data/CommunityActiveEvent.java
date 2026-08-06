package su.nightexpress.quests.community.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;

public class CommunityActiveEvent {

    private String questId;
    private double targetAmount;
    private double currentAmount;
    private long startTimestamp;
    private long endTimestamp;
    private Map<UUID, Double> contributions;
    private boolean completed;
    private boolean active;

    public CommunityActiveEvent() {
        this.contributions = new HashMap<>();
        this.active = true;
        this.completed = false;
    }

    public CommunityActiveEvent(String questId, double targetAmount, double currentAmount,
                                long startTimestamp, long endTimestamp,
                                Map<UUID, Double> contributions, boolean completed, boolean active) {
        this.questId = questId;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.contributions = contributions != null ? contributions : new HashMap<>();
        this.completed = completed;
        this.active = active;
    }

    public String getQuestId() {
        return questId;
    }

    public void setQuestId(String questId) {
        this.questId = questId;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(double currentAmount) {
        this.currentAmount = currentAmount;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Map<UUID, Double> getContributions() {
        if (this.contributions == null) {
            this.contributions = new HashMap<>();
        }
        return contributions;
    }

    public void setContributions(Map<UUID, Double> contributions) {
        this.contributions = contributions != null ? contributions : new HashMap<>();
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getContribution(UUID uuid) {
        return getContributions().getOrDefault(uuid, 0.0);
    }

    public void addContribution(UUID uuid, double amount) {
        if (uuid == null || amount <= 0) return;
        Map<UUID, Double> map = getContributions();
        map.put(uuid, map.getOrDefault(uuid, 0.0) + amount);
        this.currentAmount += amount;
        if (this.currentAmount >= this.targetAmount) {
            this.currentAmount = this.targetAmount;
            this.completed = true;
        }
    }

    private static Gson createGson() {
        return new GsonBuilder()
            .enableComplexMapKeySerialization()
            .setPrettyPrinting()
            .create();
    }

    public String toJson() {
        return createGson().toJson(this);
    }

    public static CommunityActiveEvent fromJson(String json) {
        if (json == null || json.trim().isEmpty()) return null;
        CommunityActiveEvent event = createGson().fromJson(json, CommunityActiveEvent.class);
        if (event != null && event.contributions != null) {
            Map<UUID, Double> fixedMap = new HashMap<>();
            for (Map.Entry<?, Double> entry : event.contributions.entrySet()) {
                Object key = entry.getKey();
                if (key instanceof UUID u) {
                    fixedMap.put(u, entry.getValue());
                } else if (key != null) {
                    try {
                        fixedMap.put(UUID.fromString(key.toString()), entry.getValue());
                    } catch (Exception ignored) {}
                }
            }
            event.contributions = fixedMap;
        }
        return event;
    }
}
