package su.nightexpress.quests.event;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import su.nightexpress.quests.event.definition.EventTier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventTierTest {

    @Test
    public void testEventTierUnlocking() {
        EventTier tier = new EventTier(
            1,
            10000L,
            "Palier I",
            Material.IRON_INGOT,
            0,
            List.of("money_5000", "[command] eco give %player% 5000")
        );

        assertEquals(1, tier.getTierNumber());
        assertEquals(10000L, tier.getRequirement());
        assertEquals("Palier I", tier.getDisplayName());
        assertEquals(Material.IRON_INGOT, tier.getIconMaterial());
        assertEquals(2, tier.getRewards().size());

        assertFalse(tier.isUnlocked(0));
        assertFalse(tier.isUnlocked(9999));
        assertTrue(tier.isUnlocked(10000));
        assertTrue(tier.isUnlocked(50000));
    }

    @Test
    public void testRewardParsingRawCommands() {
        EventTier tier = new EventTier(
            2,
            50000L,
            "Palier II",
            Material.GOLD_INGOT,
            1001,
            List.of("reward_quest_id", "[command] broadcast GG", "console: give %player% diamond 1")
        );

        List<EventTier.TierReward> rewards = tier.getParsedRewards();
        assertEquals(3, rewards.size());

        assertEquals(EventTier.RewardType.REWARD_ID, rewards.get(0).type());
        assertEquals("reward_quest_id", rewards.get(0).value());

        assertEquals(EventTier.RewardType.RAW_COMMAND, rewards.get(1).type());
        assertEquals("broadcast GG", rewards.get(1).value());

        assertEquals(EventTier.RewardType.RAW_COMMAND, rewards.get(2).type());
        assertEquals("give %player% diamond 1", rewards.get(2).value());
    }
}
