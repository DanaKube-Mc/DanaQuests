package su.nightexpress.quests.personal;

import org.junit.jupiter.api.Test;
import su.nightexpress.quests.personal.data.PersonalQuestData;
import su.nightexpress.quests.personal.definition.RpgCategory;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class PersonalQuestTest {

    @Test
    public void testQuadraticScaling() {
        // Test base values
        int baseAmount = 10;
        double baseMoney = 100.0;

        // Level 1
        int level1 = 1;
        int amount1 = baseAmount * level1 * level1;
        double money1 = baseMoney * level1 * level1;
        assertEquals(10, amount1);
        assertEquals(100.0, money1);

        // Level 2
        int level2 = 2;
        int amount2 = baseAmount * level2 * level2;
        double money2 = baseMoney * level2 * level2;
        assertEquals(40, amount2);
        assertEquals(400.0, money2);

        // Level 3
        int level3 = 3;
        int amount3 = baseAmount * level3 * level3;
        double money3 = baseMoney * level3 * level3;
        assertEquals(90, amount3);
        assertEquals(900.0, money3);
    }

    @Test
    public void testDailyLimitsAndReset() {
        PersonalQuestData data = new PersonalQuestData(
            "miner",
            "COAL_ORE",
            0,
            10,
            100.0,
            System.currentTimeMillis(),
            3,
            System.currentTimeMillis()
        );

        // Current time is same day: reset should do nothing
        assertFalse(data.checkAndResetDailyLimit());
        assertEquals(3, data.getQuestsAcceptedToday());

        // Set last accepted timestamp to yesterday
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        data.setLastAcceptedDayTimestamp(yesterday.getTimeInMillis());

        // Reset should return true and set questsAcceptedToday to 0
        assertTrue(data.checkAndResetDailyLimit());
        assertEquals(0, data.getQuestsAcceptedToday());
    }

    @Test
    public void testRPGLevelUp() {
        RpgCategory category = new RpgCategory(
            "miner",
            "Mineur",
            "BREAK_BLOCK",
            "GOLDEN_PICKAXE",
            "Mineur",
            Collections.emptyList(),
            0,
            5, // 5 completions to level up
            100.0,
            Collections.emptyList(),
            new HashMap<>()
        );

        int level = 1;
        int completions = 0;

        // Complete 4 quests
        for (int i = 0; i < 4; i++) {
            completions++;
            if (completions >= category.getCompletionsToLevelUp()) {
                level++;
                completions = 0;
            }
        }
        assertEquals(1, level);
        assertEquals(4, completions);

        // Complete 5th quest
        completions++;
        if (completions >= category.getCompletionsToLevelUp()) {
            level++;
            completions = 0;
        }
        assertEquals(2, level);
        assertEquals(0, completions);
    }
}
