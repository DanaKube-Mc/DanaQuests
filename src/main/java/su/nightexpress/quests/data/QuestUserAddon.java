package su.nightexpress.quests.data;

import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.Set;

public class QuestUserAddon {
    private final Set<String> completedLoreQuests;
    private final Map<String, Double> rpgCategoryXP;
    private final Map<String, Integer> rpgCategoryLevels;
    private final String trackerMode;

    public QuestUserAddon(@NotNull Set<String> completedLoreQuests,
                          @NotNull Map<String, Double> rpgCategoryXP,
                          @NotNull Map<String, Integer> rpgCategoryLevels,
                          @NotNull String trackerMode) {
        this.completedLoreQuests = completedLoreQuests;
        this.rpgCategoryXP = rpgCategoryXP;
        this.rpgCategoryLevels = rpgCategoryLevels;
        this.trackerMode = trackerMode;
    }

    @NotNull
    public Set<String> getCompletedLoreQuests() {
        return completedLoreQuests;
    }

    @NotNull
    public Map<String, Double> getRpgCategoryXP() {
        return rpgCategoryXP;
    }

    @NotNull
    public Map<String, Integer> getRpgCategoryLevels() {
        return rpgCategoryLevels;
    }

    @NotNull
    public String getTrackerMode() {
        return trackerMode;
    }
}
