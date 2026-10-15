package su.nightexpress.quests.event;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;
import su.nightexpress.quests.event.definition.EventSchedule;
import su.nightexpress.quests.event.definition.EventTier;
import su.nightexpress.quests.event.menu.EventQuestMenu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EventQuestMenuTest {

    private ServerMock server;
    private EventQuestManager eventManager;

    @BeforeEach
    public void setUp() {
        if (MockBukkit.isMocked()) MockBukkit.unmock();
        server = MockBukkit.mock();
        eventManager = new EventQuestManager(null);
    }

    @AfterEach
    public void tearDown() {
        if (MockBukkit.isMocked()) MockBukkit.unmock();
    }

    private EventQuest createQuest(String id) {
        LocalDateTime now = LocalDateTime.now();
        String start = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = now.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        EventSchedule schedule = EventSchedule.createCalendar(start, end);
        EventTier tier1 = new EventTier(1, 1000L, "Palier I", Material.IRON_INGOT, 0, List.of("money_100"));
        EventTier tier2 = new EventTier(2, 5000L, "Palier II", Material.GOLD_INGOT, 0, List.of("money_500"));

        return new EventQuest(
            id,
            "Citrouilles d'Halloween",
            List.of("Description"),
            Material.JACK_O_LANTERN,
            0,
            schedule,
            "PICKUP_ITEM",
            Set.of(Material.PUMPKIN),
            Map.of(1, tier1, 2, tier2),
            "Événement : Citrouilles",
            54
        );
    }

    @Test
    public void testTierStatesAndDeferredClaiming() {
        PlayerMock player = server.addPlayer("Arthur");
        String eventId = "halloween_pumpkin";
        EventQuest quest = createQuest(eventId);
        eventManager.registerQuest(quest);

        EventUserData data = eventManager.getUserData(player.getUniqueId(), eventId);
        data.addScore(2500L); // Unlocks Tier 1 (1000), Tier 2 (5000) is locked

        // Verify tier 1 is claimable
        EventTier t1 = quest.getTier(1);
        assertNotNull(t1);
        assertEquals(EventQuestMenu.TierState.CLAIMABLE, EventQuestMenu.resolveTierState(data, t1));

        // Verify tier 2 is locked
        EventTier t2 = quest.getTier(2);
        assertNotNull(t2);
        assertEquals(EventQuestMenu.TierState.LOCKED, EventQuestMenu.resolveTierState(data, t2));

        // Claim tier 1
        assertTrue(eventManager.claimTier(player, quest, 1));
        assertEquals(EventQuestMenu.TierState.CLAIMED, EventQuestMenu.resolveTierState(data, t1));

        // Deferred claiming test: Expire event!
        quest.getSchedule().stop();
        assertTrue(quest.isExpired());

        // Player had unlocked tier 1 (already claimed). If player now reaches tier 2? (Progression is frozen)
        // But what if player had reached score before expiration without claiming?
        data.addScore(3000L); // Now score is 5500
        assertEquals(EventQuestMenu.TierState.CLAIMABLE, EventQuestMenu.resolveTierState(data, t2));
        assertTrue(eventManager.claimTier(player, quest, 2));
        assertEquals(EventQuestMenu.TierState.CLAIMED, EventQuestMenu.resolveTierState(data, t2));
    }
}
