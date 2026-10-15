package su.nightexpress.quests.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.nightexpress.quests.event.data.EventDataDao;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.data.LeaderboardEntry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EventDataHandlerTest {

    private Connection connection;
    private EventDataDao dao;

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        // Wrap connection so try-with-resources in DAO doesn't close the in-memory SQLite database
        Connection proxyConn = (Connection) java.lang.reflect.Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) {
                    return null; // Don't close the single in-memory connection
                }
                return method.invoke(connection, args);
            }
        );
        dao = new EventDataDao(() -> proxyConn);
        dao.initTable();
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testSaveAndLoadUserData() {
        UUID uuid = UUID.randomUUID();
        String eventId = "halloween_pumpkin";

        EventUserData data = new EventUserData(uuid, eventId, 45000L, Set.of(1, 2), System.currentTimeMillis());
        dao.saveUserData(data);

        EventUserData loaded = dao.loadUserData(uuid, eventId);
        assertNotNull(loaded);
        assertEquals(uuid, loaded.getPlayerUuid());
        assertEquals(eventId, loaded.getEventId());
        assertEquals(45000L, loaded.getTotalScore());
        assertEquals(2, loaded.getClaimedTiers().size());
        assertTrue(loaded.isTierClaimed(1));
        assertTrue(loaded.isTierClaimed(2));
    }

    @Test
    public void testTopScoresAndPlayerRank() {
        String eventId = "halloween_pumpkin";
        UUID p1 = UUID.randomUUID();
        UUID p2 = UUID.randomUUID();
        UUID p3 = UUID.randomUUID();

        dao.saveUserData(new EventUserData(p1, eventId, 10000L, Set.of(), System.currentTimeMillis()));
        dao.saveUserData(new EventUserData(p2, eventId, 75000L, Set.of(), System.currentTimeMillis()));
        dao.saveUserData(new EventUserData(p3, eventId, 30000L, Set.of(), System.currentTimeMillis()));

        List<LeaderboardEntry> top = dao.loadTopScores(eventId, 10);
        assertEquals(3, top.size());
        assertEquals(p2, top.get(0).playerUuid());
        assertEquals(75000L, top.get(0).score());
        assertEquals(p3, top.get(1).playerUuid());
        assertEquals(30000L, top.get(1).score());
        assertEquals(p1, top.get(2).playerUuid());
        assertEquals(10000L, top.get(2).score());

        // Test ranks
        assertEquals(1, dao.getPlayerRank(p2, eventId, 75000L));
        assertEquals(2, dao.getPlayerRank(p3, eventId, 30000L));
        assertEquals(3, dao.getPlayerRank(p1, eventId, 10000L));

        // Player with 0 score (not participated yet)
        UUID p4 = UUID.randomUUID();
        assertEquals(-1, dao.getPlayerRank(p4, eventId, 0L));
    }

    @Test
    public void testResetUserData() {
        UUID uuid = UUID.randomUUID();
        String eventId = "halloween_pumpkin";

        dao.saveUserData(new EventUserData(uuid, eventId, 50000L, Set.of(1), System.currentTimeMillis()));
        assertNotNull(dao.loadUserData(uuid, eventId));

        dao.resetUserData(uuid, eventId);
        EventUserData resetLoaded = dao.loadUserData(uuid, eventId);
        assertNotNull(resetLoaded);
        assertEquals(0L, resetLoaded.getTotalScore());
        assertTrue(resetLoaded.getClaimedTiers().isEmpty());
    }
}
