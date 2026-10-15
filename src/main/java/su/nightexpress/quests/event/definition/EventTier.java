package su.nightexpress.quests.event.definition;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.reward.Reward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventTier {

    public enum RewardType {
        REWARD_ID,
        RAW_COMMAND
    }

    public record TierReward(@NotNull RewardType type, @NotNull String value) {}

    private final int tierNumber;
    private final long requirement;
    private final String displayName;
    private final Material iconMaterial;
    private final int customModelData;
    private final List<String> rewards;
    private final List<TierReward> parsedRewards;

    public EventTier(
        int tierNumber,
        long requirement,
        @NotNull String displayName,
        @NotNull Material iconMaterial,
        int customModelData,
        @NotNull List<String> rewards
    ) {
        this.tierNumber = tierNumber;
        this.requirement = requirement;
        this.displayName = displayName;
        this.iconMaterial = iconMaterial;
        this.customModelData = customModelData;
        this.rewards = new ArrayList<>(rewards);
        this.parsedRewards = parseRewards(rewards);
    }

    private static List<TierReward> parseRewards(@NotNull List<String> rawList) {
        List<TierReward> parsed = new ArrayList<>();
        for (String raw : rawList) {
            String trimmed = raw.trim();
            if (trimmed.startsWith("[command]")) {
                String cmd = trimmed.substring("[command]".length()).trim();
                parsed.add(new TierReward(RewardType.RAW_COMMAND, cmd));
            } else if (trimmed.startsWith("console:")) {
                String cmd = trimmed.substring("console:".length()).trim();
                parsed.add(new TierReward(RewardType.RAW_COMMAND, cmd));
            } else {
                parsed.add(new TierReward(RewardType.REWARD_ID, trimmed));
            }
        }
        return Collections.unmodifiableList(parsed);
    }

    public boolean isUnlocked(long currentScore) {
        return currentScore >= this.requirement;
    }

    public void giveRewards(@NotNull Player player, @NotNull QuestsPlugin plugin) {
        for (TierReward reward : this.parsedRewards) {
            if (reward.type() == RewardType.RAW_COMMAND) {
                String cmd = reward.value().replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            } else {
                Reward pluginReward = plugin.getRewardManager().getRewardById(reward.value());
                if (pluginReward != null) {
                    pluginReward.runCommands(player, 1, 1, 1.0);
                }
            }
        }
    }

    public int getTierNumber() {
        return tierNumber;
    }

    public long getRequirement() {
        return requirement;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public Material getIconMaterial() {
        return iconMaterial;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    @NotNull
    public List<String> getRewards() {
        return Collections.unmodifiableList(rewards);
    }

    @NotNull
    public List<TierReward> getParsedRewards() {
        return parsedRewards;
    }
}
