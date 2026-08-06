package su.nightexpress.quests.island.definition;

import java.util.List;

public class IslandQuest {
    private final String id;
    private final String name;
    private final int order;
    private final List<String> description;
    private final List<IslandQuestRequirement> requirements;
    private final List<String> rewards;

    public IslandQuest(String id, String name, int order, List<String> description, List<IslandQuestRequirement> requirements, List<String> rewards) {
        this.id = id;
        this.name = name;
        this.order = order;
        this.description = description;
        this.requirements = requirements;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<IslandQuestRequirement> getRequirements() {
        return requirements;
    }

    public List<String> getRewards() {
        return rewards;
    }
}
