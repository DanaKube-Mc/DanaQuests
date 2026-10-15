package su.nightexpress.quests.event;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import su.nightexpress.quests.event.data.EventLeaderboard;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;
import su.nightexpress.quests.event.definition.EventSchedule;
import su.nightexpress.quests.event.definition.EventTier;
import su.nightexpress.quests.event.placeholder.EventPlaceholders;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EventPlaceholdersTest {

    private ServerMock server;
    private EventQuestManager eventManager;
    private EventPlaceholders placeholders;

    @BeforeEach
    public void setUp() {
        if (MockBukkit.isMocked()) MockBukkit.unmock();
        server = MockBukkit.mock();
        eventManager = new EventQuestManager(null);
        placeholders = new EventPlaceholders(eventManager);
    }

    @AfterEach
    public void tearDown() {
        if (MockBukkit.isMocked()) MockBukkit.unmock();
    }

    private EventQuest createTestQuest(String id) {
        LocalDateTime now = LocalDateTime.now();
        String start = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = now.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        EventSchedule schedule = EventSchedule.createCalendar(start, end);
        EventTier tier = new EventTier(1, 1000L, "Palier 1", Material.IRON_INGOT, 0, List.of());

        return new EventQuest(
            id,
            "Fête d'Halloween",
            List.of(),
            Material.JACK_O_LANTERN,
            0,
            schedule,
            "PICKUP_ITEM",
            Set.of(Material.PUMPKIN),
            Map.of(1, tier),
            "GUI",
            54
        );
    }

    @Test
    public void testPlaceholdersParsing() {
        PlayerMock player = server.addPlayer("Arthur");
        String eventId = "halloween_pumpkin";
        EventQuest quest = createTestQuest(eventId);
        eventManager.registerQuest(quest);

        EventUserData userData = eventManager.getUserData(player.getUniqueId(), eventId);
        userData.addScore(42000L);

        EventLeaderboard leaderboard = new EventLeaderboard(eventId, null);
        leaderboard.setNameResolver(uuid -> "TopPlayer");
        eventManager.setLeaderboard(eventId, leaderboard);

        // Score
        assertEquals("42000", placeholders.parse(player, "event_" + eventId + "_score"));

        // Status
        assertEquals("Actif", placeholders.parse(player, "event_" + eventId + "_status"));

        // Time left
        assertNotNull(placeholders.parse(player, "event_" + eventId + "_time_left"));

        // Rank when ranked or unranked
        assertEquals("Non classé", placeholders.parse(player, "event_" + eventId + "_rank"));

        // Top name & score
        assertEquals("---", placeholders.parse(player, "event_" + eventId + "_top_name_1"));
        assertEquals("0", placeholders.parse(player, "event_" + eventId + "_top_score_1"));

        // Expired status
        quest.getSchedule().stop();
        assertEquals("Terminé", placeholders.parse(player, "event_" + eventId + "_status"));
    }
}
