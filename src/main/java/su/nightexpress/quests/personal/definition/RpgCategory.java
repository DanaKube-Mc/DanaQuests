package su.nightexpress.quests.personal.definition;

import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;

public class RpgCategory {

    private final String id;
    private final String displayName;
    private final String type;
    private final String iconMaterial;
    private final String iconName;
    private final List<String> iconLore;
    private final int iconCustomModelData;
    private final int completionsToLevelUp;
    private final double baseMoney;
    private final List<String> commands;
    private final Map<String, Integer> objectives;

    public RpgCategory(@NotNull String id,
                       @NotNull String displayName,
                       @NotNull String type,
                       @NotNull String iconMaterial,
                       @NotNull String iconName,
                       @NotNull List<String> iconLore,
                       int iconCustomModelData,
                       int completionsToLevelUp,
                       double baseMoney,
                       @NotNull List<String> commands,
                       @NotNull Map<String, Integer> objectives) {
        this.id = id;
        this.displayName = displayName;
        this.type = type;
        this.iconMaterial = iconMaterial;
        this.iconName = iconName;
        this.iconLore = iconLore;
        this.iconCustomModelData = iconCustomModelData;
        this.completionsToLevelUp = completionsToLevelUp;
        this.baseMoney = baseMoney;
        this.commands = commands;
        this.objectives = objectives;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public String getType() {
        return type;
    }

    @NotNull
    public String getIconMaterial() {
        return iconMaterial;
    }

    @NotNull
    public String getIconName() {
        return iconName;
    }

    @NotNull
    public List<String> getIconLore() {
        return iconLore;
    }

    public int getIconCustomModelData() {
        return iconCustomModelData;
    }

    public int getCompletionsToLevelUp() {
        return completionsToLevelUp;
    }

    public double getBaseMoney() {
        return baseMoney;
    }

    @NotNull
    public List<String> getCommands() {
        return commands;
    }

    @NotNull
    public Map<String, Integer> getObjectives() {
        return objectives;
    }
}
