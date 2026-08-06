package su.nightexpress.quests.lore.data;

import org.jetbrains.annotations.NotNull;
import java.util.HashMap;
import java.util.Map;

public class LoreQuestData {

    private String questId;
    private Map<String, Integer> objectiveProgress;

    public LoreQuestData() {
        this.objectiveProgress = new HashMap<>();
    }

    public LoreQuestData(@NotNull String questId) {
        this.questId = questId;
        this.objectiveProgress = new HashMap<>();
    }

    public LoreQuestData(@NotNull String questId, @NotNull Map<String, Integer> objectiveProgress) {
        this.questId = questId;
        this.objectiveProgress = objectiveProgress;
    }

    @NotNull
    public String getQuestId() {
        return this.questId;
    }

    public void setQuestId(@NotNull String questId) {
        this.questId = questId;
    }

    @NotNull
    public Map<String, Integer> getObjectiveProgress() {
        return this.objectiveProgress;
    }

    public void setObjectiveProgress(@NotNull Map<String, Integer> objectiveProgress) {
        this.objectiveProgress = objectiveProgress;
    }

    public int getProgress(@NotNull String objectiveId) {
        return this.objectiveProgress.getOrDefault(objectiveId, 0);
    }

    public void setProgress(@NotNull String objectiveId, int progress) {
        this.objectiveProgress.put(objectiveId, progress);
    }

    public void addProgress(@NotNull String objectiveId, int amount) {
        int current = this.getProgress(objectiveId);
        this.setProgress(objectiveId, current + amount);
    }
}
