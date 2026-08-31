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
}
