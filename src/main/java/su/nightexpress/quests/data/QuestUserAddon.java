package su.nightexpress.quests.data;

import org.jetbrains.annotations.NotNull;
import java.util.Map;
import java.util.Set;

public class QuestUserAddon {
    private final Set<String> completedLoreQuests;
    private final Map<String, Double> rpgCategoryXP;
    private final Map<String, Integer> rpgCategoryLevels;
    private final boolean trackerDisabled;

    public QuestUserAddon(@NotNull Set<String> completedLoreQuests,
                          @NotNull Map<String, Double> rpgCategoryXP,
                          @NotNull Map<String, Integer> rpgCategoryLevels,
                          boolean trackerDisabled) {
        this.completedLoreQuests = completedLoreQuests;
        this.rpgCategoryXP = rpgCategoryXP;
        this.rpgCategoryLevels = rpgCategoryLevels;
        this.trackerDisabled = trackerDisabled;
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

    public boolean isTrackerDisabled() {
        return trackerDisabled;
    }
}
