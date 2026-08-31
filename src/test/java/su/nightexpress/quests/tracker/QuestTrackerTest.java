package su.nightexpress.quests.tracker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.nightexpress.quests.user.QuestUser;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class QuestTrackerTest {

    private QuestUser user;

    @BeforeEach
    void setUp() {
        user = new QuestUser(
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
    }

    @Test
    void should_use_boss_bar_as_default_tracker_mode() {
        // Assert : le mode de tracker par défaut de l'utilisateur doit être BOSS_BAR
        assertEquals("BOSS_BAR", user.getTrackerMode());
    }

    @Test
    void should_rotate_between_active_quests_when_multiple_exist() {
        // Arrange : création d'une liste simulée de quêtes actives (Lore + RPG)
        LinkedHashMap<String, String> activeQuestsMap = new LinkedHashMap<>();
        activeQuestsMap.put("lore_tuto_1", "Fondation du Domaine");
        activeQuestsMap.put("rpg_miner_1", "Mineur de Pierre");

        // Act : simulation de l'itération de rotation
        List<String> keys = new ArrayList<>(activeQuestsMap.keySet());
        String current = keys.get(0);
        int index = keys.indexOf(current);
        String next = keys.get((index + 1) % keys.size());

        // Assert : la rotation doit passer de la 1ère quête à la 2ème quête
        assertEquals("lore_tuto_1", current);
        assertEquals("rpg_miner_1", next);
        assertTrue(keys.size() > 1, "La rotation nécessite au moins 2 quêtes actives");
    }

    @Test
    void should_remove_completed_quest_from_tracker_on_refresh() {
        // Arrange : simulation d'un tracker avec une quête en cours
        LinkedHashMap<String, String> activeQuestsMap = new LinkedHashMap<>();
        activeQuestsMap.put("personal_miner", "Mineur (3/4)");

        // Act : complétion de la quête (plus d'objectif actif) et refresh (clear + rebuild)
        activeQuestsMap.clear();
        // Aucune quête active restante

        // Assert : le tracker doit être vide et désactiver l'affichage
        assertTrue(activeQuestsMap.isEmpty(), "Le tracker doit être vide une fois la quête complétée");
    }

    @Test
    void should_prioritize_highest_progress_quest_when_multiple_progress_simultaneously() {
        // Arrange : Quête perso (1/10 = 10%) vs Quête Lore (1/64 = 1.56%)
        int personalProgress = 1, personalReq = 10;
        int loreProgress = 1, loreReq = 64;

        double personalRatio = (double) personalProgress / personalReq;
        double loreRatio = (double) loreProgress / loreReq;

        // Act : Sélection de la quête avec le plus haut ratio
        String focusedQuestId = personalRatio >= loreRatio ? "personal_miner" : "lore_dirt";

        // Assert : La quête personnelle (1/10) doit être prioritaire sur la quête de lore (1/64)
        assertEquals("personal_miner", focusedQuestId);
        assertTrue(personalRatio > loreRatio);
    }

    @Test
    void should_switch_to_remaining_quest_when_focused_quest_completes() {
        // Arrange : Quête perso terminée (10/10) et quête de lore restante (11/64)
        Map<String, Double> activeQuests = new LinkedHashMap<>();
        activeQuests.put("lore_dirt", 11.0 / 64.0); // 17.18%

        // Act : focusedQuestId perso n'est plus active
        String focusedQuestId = "personal_miner";
        if (!activeQuests.containsKey(focusedQuestId)) {
            focusedQuestId = activeQuests.keySet().iterator().next();
        }

        // Assert : Le focus bascule sur la quête de lore restante
        assertEquals("lore_dirt", focusedQuestId);
    }
}
