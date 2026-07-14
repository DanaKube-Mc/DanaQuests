package su.nightexpress.quests.user;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public class QuestUserTest {

    @Test
    public void testQuestUserLore() {
        UUID uuid = UUID.randomUUID();
        QuestUser user = new QuestUser(
            uuid,
            "TestPlayer",
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            0L,
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            false
        );

        assertFalse(user.hasCompletedLore("lore_quest_1"));
        user.completeLoreQuest("lore_quest_1");
        assertTrue(user.hasCompletedLore("lore_quest_1"));
    }

    @Test
    public void testQuestUserRPGXP() {
        UUID uuid = UUID.randomUUID();
        QuestUser user = new QuestUser(
            uuid,
            "TestPlayer",
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            0L,
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            false
        );

        // Default level and XP
        assertEquals(1, user.getRPGLevel("minage"));
        assertEquals(0.0, user.getRPGXP("minage"));

        // Add XP below threshold (threshold for level 1 is 100 * 1 = 100)
        user.addRPGXP("minage", 50.0);
        assertEquals(1, user.getRPGLevel("minage"));
        assertEquals(50.0, user.getRPGXP("minage"));

        // Level up to level 2 (added 60 -> total 110. Threshold is 100. Remaining XP: 10)
        user.addRPGXP("minage", 60.0);
        assertEquals(2, user.getRPGLevel("minage"));
        assertEquals(10.0, user.getRPGXP("minage"));

        // Level up to level 3 (remaining 10. Added 250 -> total 260. Level 2 threshold is 200. Remaining XP: 60)
        user.addRPGXP("minage", 250.0);
        assertEquals(3, user.getRPGLevel("minage"));
        assertEquals(60.0, user.getRPGXP("minage"));
    }

    @Test
    public void testQuestUserTracker() {
        UUID uuid = UUID.randomUUID();
        QuestUser user = new QuestUser(
            uuid,
            "TestPlayer",
            System.currentTimeMillis(),
            System.currentTimeMillis(),
            0L,
            new HashMap<>(),
            new HashMap<>(),
            new HashMap<>(),
            new HashSet<>(),
            new HashMap<>(),
            new HashMap<>(),
            false
        );

        assertFalse(user.isTrackerDisabled());
        user.setTrackerDisabled(true);
        assertTrue(user.isTrackerDisabled());
    }
}
