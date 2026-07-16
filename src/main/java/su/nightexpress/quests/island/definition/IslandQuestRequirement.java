package su.nightexpress.quests.island.definition;

public class IslandQuestRequirement {
    private final String id;
    private final String name;
    private final String resourceGroupId;
    private final int targetAmount;

    public IslandQuestRequirement(String id, String name, String resourceGroupId, int targetAmount) {
        this.id = id;
        this.name = name;
        this.resourceGroupId = resourceGroupId;
        this.targetAmount = targetAmount;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getResourceGroupId() {
        return resourceGroupId;
    }

    public int getTargetAmount() {
        return targetAmount;
    }
}
