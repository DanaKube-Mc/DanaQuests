package su.nightexpress.quests.lore;

import org.junit.jupiter.api.Test;
import su.nightexpress.quests.user.QuestUser;
import su.nightexpress.quests.lore.definition.LoreObjective;
import su.nightexpress.quests.lore.definition.LoreQuest;
import su.nightexpress.quests.lore.definition.LoreQuestCategory;
import su.nightexpress.quests.lore.data.LoreQuestData;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class LoreQuestTest {

    @Test
    public void testLoreQuestLinearOrder() {
        // Create a list of linear lore quests
        LoreObjective obj1 = new LoreObjective("obj_1", "talk_to_npc", "citizens:12", 1, "Parler au Maire");
        LoreQuest quest1 = new LoreQuest("lore_1", "Rencontre", Collections.emptyList(), Collections.singletonList(obj1), Collections.emptyList(), null, null, null, "chapitre1");

        LoreObjective obj2 = new LoreObjective("obj_2", "visit_location", "worldguard:spawn", 1, "Aller au spawn");
        LoreQuest quest2 = new LoreQuest("lore_2", "Visite", Collections.emptyList(), Collections.singletonList(obj2), Collections.emptyList(), null, null, null, "chapitre1");

        List<LoreQuest> quests = Arrays.asList(quest1, quest2);
        LoreQuestCategory category = new LoreQuestCategory("chapitre1", "Chapitre 1", Collections.emptyList(), "BOOK", 0, Collections.emptyList(), quests);

        // Mock QuestUser
        QuestUser user = new QuestUser(
                UUID.randomUUID(),
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
                new HashSet<>(),
                new HashMap<>(),
                "BOSS_BAR"
        );

        // Check first active quest (should be quest1 since user has completed nothing)
        assertFalse(user.hasCompletedLore("lore_1"));
        assertFalse(user.hasCompletedLore("lore_2"));

        // Helper simulation of getting active quest in chapter 1
        LoreQuest active = getActiveQuest(user, category);
        assertNotNull(active);
        assertEquals("lore_1", active.getId());

        // Complete first quest
        user.completeLoreQuest("lore_1");
        assertTrue(user.hasCompletedLore("lore_1"));

        // Check active quest now (should be quest2)
        active = getActiveQuest(user, category);
        assertNotNull(active);
        assertEquals("lore_2", active.getId());

        // Complete second quest
        user.completeLoreQuest("lore_2");
        assertTrue(user.hasCompletedLore("lore_2"));

        // All quests in category completed, active quest should be null
        active = getActiveQuest(user, category);
        assertNull(active);
    }

    @Test
    public void testIntermediateProgressSaving() {
        QuestUser user = new QuestUser(
                UUID.randomUUID(),
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
                new HashSet<>(),
                new HashMap<>(),
                "BOSS_BAR"
        );

        // Modify intermediate progression maps
        Map<String, LoreQuestData> progressMap = user.getLoreQuestsProgress();
        assertTrue(progressMap.isEmpty());

        LoreQuestData questData = new LoreQuestData("lore_quest_1");
        questData.setProgress("obj_1", 3);
        progressMap.put("lore_quest_1", questData);

        // Verify values are stored correctly
        assertEquals(3, user.getLoreQuestsProgress().get("lore_quest_1").getProgress("obj_1"));
    }

    @Test
    public void testTrackerCategoryExclusion() {
        QuestUser user = new QuestUser(
                UUID.randomUUID(),
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
                new HashSet<>(),
                new HashMap<>(),
                "BOSS_BAR"
        );

        // Test category tracking state
        assertFalse(user.isCategoryTrackerDisabled("chapitre1"));

        // Disable category tracker
        user.toggleCategoryTracker("chapitre1", true);
        assertTrue(user.isCategoryTrackerDisabled("chapitre1"));

        // Re-enable category tracker
        user.toggleCategoryTracker("chapitre1", false);
        assertFalse(user.isCategoryTrackerDisabled("chapitre1"));
    }

    private LoreQuest getActiveQuest(QuestUser user, LoreQuestCategory category) {
        for (LoreQuest quest : category.getQuests()) {
            if (!user.hasCompletedLore(quest.getId())) {
                return quest;
            }
        }
        return null;
    }
}
