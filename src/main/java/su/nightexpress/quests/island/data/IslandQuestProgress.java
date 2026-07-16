package su.nightexpress.quests.island.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IslandQuestProgress {
    private final UUID islandUuid;
    private final String questId;
    private final Map<String, Integer> progress;
    private boolean completed;

    public IslandQuestProgress(UUID islandUuid, String questId) {
        this.islandUuid = islandUuid;
        this.questId = questId;
        this.progress = new HashMap<>();
        this.completed = false;
    }

    public IslandQuestProgress(UUID islandUuid, String questId, Map<String, Integer> progress, boolean completed) {
        this.islandUuid = islandUuid;
        this.questId = questId;
        this.progress = new HashMap<>(progress);
        this.completed = completed;
    }

    public UUID getIslandUuid() {
        return islandUuid;
    }

    public String getQuestId() {
        return questId;
    }

    public Map<String, Integer> getProgress() {
        return progress;
    }

    public int getRequirementProgress(String requirementId) {
        return progress.getOrDefault(requirementId, 0);
    }

    public void setRequirementProgress(String requirementId, int value) {
        progress.put(requirementId, value);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
