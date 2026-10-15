package su.nightexpress.quests.event;

import org.bukkit.Material;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import su.nightexpress.quests.event.command.EventCommands;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;
import su.nightexpress.quests.event.definition.EventSchedule;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EventCommandsTest {

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
        EventSchedule schedule = EventSchedule.createDuration(48);
        return new EventQuest(
            id,
            "Event " + id,
            List.of(),
            Material.PUMPKIN,
            0,
            schedule,
            "PICKUP_ITEM",
            Set.of(Material.PUMPKIN),
            Map.of(),
            "GUI",
            54
        );
    }

    @Test
    public void testEventCommandsExecution() {
        PlayerMock admin = server.addPlayer("Admin");
        PlayerMock player = server.addPlayer("TestUser");

        String eventId = "halloween_pumpkin";
        EventQuest quest = createQuest(eventId);
        eventManager.registerQuest(quest);

        assertFalse(quest.isActive());

        // Test start
        EventCommands.handleStart(admin, eventManager, eventId, 24);
        assertTrue(quest.isActive());
        assertTrue(quest.getSchedule().getTimeLeftMillis() > 0);

        // Test addscore
        EventCommands.handleAddScore(admin, eventManager, eventId, player.getName(), 15000L);
        EventUserData data = eventManager.getUserData(player.getUniqueId(), eventId);
        assertEquals(15000L, data.getTotalScore());

        // Test reset
        EventCommands.handleReset(admin, eventManager, eventId, player.getName());
        assertEquals(0L, data.getTotalScore());

        // Test stop
        EventCommands.handleStop(admin, eventManager, eventId);
        assertFalse(quest.isActive());
        assertTrue(quest.isExpired());
    }
}
