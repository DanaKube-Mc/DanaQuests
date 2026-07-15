package su.nightexpress.quests.lore.definition;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LoreQuestCategory {

    private final String id;
    private final String name;
    private final List<String> description;
    private final String iconMaterial;
    private final int iconCustomModelData;
    private final List<String> neededCompletedCategories;
    private final List<LoreQuest> quests;

    public LoreQuestCategory(@NotNull String id,
                             @NotNull String name,
                             @NotNull List<String> description,
                             @NotNull String iconMaterial,
                             int iconCustomModelData,
                             @NotNull List<String> neededCompletedCategories,
                             @NotNull List<LoreQuest> quests) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconMaterial = iconMaterial;
        this.iconCustomModelData = iconCustomModelData;
        this.neededCompletedCategories = neededCompletedCategories;
        this.quests = quests;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getName() {
        return this.name;
    }

    @NotNull
    public List<String> getDescription() {
        return this.description;
    }

    @NotNull
    public String getIconMaterial() {
        return this.iconMaterial;
    }

    public int getIconCustomModelData() {
        return this.iconCustomModelData;
    }

    @NotNull
    public List<String> getNeededCompletedCategories() {
        return this.neededCompletedCategories;
    }

    @NotNull
    public List<LoreQuest> getQuests() {
        return this.quests;
    }
}
