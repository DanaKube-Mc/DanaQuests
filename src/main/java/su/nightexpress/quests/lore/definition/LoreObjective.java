package su.nightexpress.quests.lore.definition;

import org.jetbrains.annotations.NotNull;

public class LoreObjective {

    private final String id;
    private final String taskType;
    private final String target;
    private final int required;
    private final String description;

    public LoreObjective(@NotNull String id, @NotNull String taskType, @NotNull String target, int required, @NotNull String description) {
        this.id = id;
        this.taskType = taskType;
        this.target = target;
        this.required = required;
        this.description = description;
    }

    @NotNull
    public String getId() {
        return this.id;
    }

    @NotNull
    public String getTaskType() {
        return this.taskType;
    }

    @NotNull
    public String getTarget() {
        return this.target;
    }

    public int getRequired() {
        return this.required;
    }

    @NotNull
    public String getDescription() {
        return this.description;
    }
}
