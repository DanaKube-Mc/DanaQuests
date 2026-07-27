package su.nightexpress.quests.community.definition;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CommunityQuest {

    private String id;
    private String displayName;
    private List<String> description;
    private String type; // MONEY_DEPOSIT, ITEM_DEPOSIT, BLOCK_BREAK_GLOBAL, KILL_MOB_GLOBAL
    private double defaultTarget;
    private int defaultDurationHours;
    private Material icon;
    private int customModelData;
    private List<String> globalRewardCommands;
    private RankingRewards rankingRewards;

    public CommunityQuest() {
        this.description = new ArrayList<>();
        this.globalRewardCommands = new ArrayList<>();
        this.rankingRewards = new RankingRewards();
        this.icon = Material.PAPER;
    }

    public CommunityQuest(String id, String displayName, List<String> description, String type,
                          double defaultTarget, int defaultDurationHours, Material icon,
                          int customModelData, List<String> globalRewardCommands, RankingRewards rankingRewards) {
        this.id = id;
        this.displayName = displayName;
        this.description = description != null ? description : new ArrayList<>();
        this.type = type;
        this.defaultTarget = defaultTarget;
        this.defaultDurationHours = defaultDurationHours;
        this.icon = icon != null ? icon : Material.PAPER;
        this.customModelData = customModelData;
        this.globalRewardCommands = globalRewardCommands != null ? globalRewardCommands : new ArrayList<>();
        this.rankingRewards = rankingRewards != null ? rankingRewards : new RankingRewards();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public void setDescription(List<String> description) {
        this.description = description != null ? description : new ArrayList<>();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(double defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public int getDefaultDurationHours() {
        return defaultDurationHours;
    }

    public void setDefaultDurationHours(int defaultDurationHours) {
        this.defaultDurationHours = defaultDurationHours;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon != null ? icon : Material.PAPER;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public void setCustomModelData(int customModelData) {
        this.customModelData = customModelData;
    }

    public List<String> getGlobalRewardCommands() {
        return globalRewardCommands;
    }

    public void setGlobalRewardCommands(List<String> globalRewardCommands) {
        this.globalRewardCommands = globalRewardCommands != null ? globalRewardCommands : new ArrayList<>();
    }

    public RankingRewards getRankingRewards() {
        return rankingRewards;
    }

    public void setRankingRewards(RankingRewards rankingRewards) {
        this.rankingRewards = rankingRewards != null ? rankingRewards : new RankingRewards();
    }
}
