package su.nightexpress.quests.lore.definition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class LoreQuest {

    private final String id;
    private final String name;
    private final List<String> description;
    private final List<LoreObjective> objectives;
    private final List<String> rewards;
    private final String completionSound;
    private final String completionTitle;
    private final String completionSubtitle;
    private final String categoryId;
    private final String iconMaterial;
    private final List<String> iconLore;
    private final int iconCustomModelData;

    public LoreQuest(@NotNull String id,
                     @NotNull String name,
                     @NotNull List<String> description,
                     @NotNull List<LoreObjective> objectives,
                     @NotNull List<String> rewards,
                     @Nullable String completionSound,
                     @Nullable String completionTitle,
                     @Nullable String completionSubtitle,
                     @NotNull String categoryId) {
        this(id, name, description, objectives, rewards, completionSound, completionTitle, completionSubtitle, categoryId, "CHEST", Collections.emptyList(), 0);
    }

    public LoreQuest(@NotNull String id,
                     @NotNull String name,
                     @NotNull List<String> description,
                     @NotNull List<LoreObjective> objectives,
                     @NotNull List<String> rewards,
                     @Nullable String completionSound,
                     @Nullable String completionTitle,
                     @Nullable String completionSubtitle,
                     @NotNull String categoryId,
                     @NotNull String iconMaterial,
                     @NotNull List<String> iconLore,
                     int iconCustomModelData) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.objectives = objectives;
        this.rewards = rewards;
        this.completionSound = completionSound;
        this.completionTitle = completionTitle;
        this.completionSubtitle = completionSubtitle;
        this.categoryId = categoryId;
        this.iconMaterial = iconMaterial;
        this.iconLore = iconLore;
        this.iconCustomModelData = iconCustomModelData;
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
    public List<LoreObjective> getObjectives() {
        return this.objectives;
    }

    @NotNull
    public List<String> getRewards() {
        return this.rewards;
    }

    @Nullable
    public String getCompletionSound() {
        return this.completionSound;
    }

    @Nullable
    public String getCompletionTitle() {
        return this.completionTitle;
    }

    @Nullable
    public String getCompletionSubtitle() {
        return this.completionSubtitle;
    }

    @NotNull
    public String getCategoryId() {
        return this.categoryId;
    }

    @NotNull
    public String getIconMaterial() {
        return this.iconMaterial;
    }

    @NotNull
    public List<String> getIconLore() {
        return this.iconLore;
    }

    public int getIconCustomModelData() {
        return this.iconCustomModelData;
    }
}
