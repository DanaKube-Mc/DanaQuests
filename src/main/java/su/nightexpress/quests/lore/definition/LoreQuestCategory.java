package su.nightexpress.quests.lore.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LoreQuestCategory {

    private final String id;
    private final String name;
    private final List<String> description;
    private final String iconMaterial;
    private final int iconCustomModelData;
    private final List<String> neededCompletedCategories;
    private final List<LoreQuest> quests;

    private final String activeIconMaterial;
    private final String activeIconName;
    private final List<String> activeIconLore;
    private final int activeIconCustomModelData;

    private final String inactiveIconMaterial;
    private final String inactiveIconName;
    private final List<String> inactiveIconLore;
    private final int inactiveIconCustomModelData;

    private final String finishedIconMaterial;
    private final String finishedIconName;
    private final List<String> finishedIconLore;
    private final int finishedIconCustomModelData;

    public LoreQuestCategory(@NotNull String id,
                             @NotNull String name,
                             @NotNull List<String> description,
                             @NotNull String iconMaterial,
                             int iconCustomModelData,
                             @NotNull List<String> neededCompletedCategories,
                             @NotNull List<LoreQuest> quests,
                             @Nullable String activeIconMaterial,
                             @Nullable String activeIconName,
                             @Nullable List<String> activeIconLore,
                             int activeIconCustomModelData,
                             @Nullable String inactiveIconMaterial,
                             @Nullable String inactiveIconName,
                             @Nullable List<String> inactiveIconLore,
                             int inactiveIconCustomModelData,
                             @Nullable String finishedIconMaterial,
                             @Nullable String finishedIconName,
                             @Nullable List<String> finishedIconLore,
                             int finishedIconCustomModelData) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.iconMaterial = iconMaterial;
        this.iconCustomModelData = iconCustomModelData;
        this.neededCompletedCategories = neededCompletedCategories;
        this.quests = quests;
        
        this.activeIconMaterial = activeIconMaterial;
        this.activeIconName = activeIconName;
        this.activeIconLore = activeIconLore;
        this.activeIconCustomModelData = activeIconCustomModelData;
        
        this.inactiveIconMaterial = inactiveIconMaterial;
        this.inactiveIconName = inactiveIconName;
        this.inactiveIconLore = inactiveIconLore;
        this.inactiveIconCustomModelData = inactiveIconCustomModelData;
        
        this.finishedIconMaterial = finishedIconMaterial;
        this.finishedIconName = finishedIconName;
        this.finishedIconLore = finishedIconLore;
        this.finishedIconCustomModelData = finishedIconCustomModelData;
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

    @Nullable
    public String getActiveIconMaterial() {
        return activeIconMaterial;
    }

    @Nullable
    public String getActiveIconName() {
        return activeIconName;
    }

    @Nullable
    public List<String> getActiveIconLore() {
        return activeIconLore;
    }

    public int getActiveIconCustomModelData() {
        return activeIconCustomModelData;
    }

    @Nullable
    public String getInactiveIconMaterial() {
        return inactiveIconMaterial;
    }

    @Nullable
    public String getInactiveIconName() {
        return inactiveIconName;
    }

    @Nullable
    public List<String> getInactiveIconLore() {
        return inactiveIconLore;
    }

    public int getInactiveIconCustomModelData() {
        return inactiveIconCustomModelData;
    }

    @Nullable
    public String getFinishedIconMaterial() {
        return finishedIconMaterial;
    }

    @Nullable
    public String getFinishedIconName() {
        return finishedIconName;
    }

    @Nullable
    public List<String> getFinishedIconLore() {
        return finishedIconLore;
    }

    public int getFinishedIconCustomModelData() {
        return finishedIconCustomModelData;
    }
}
