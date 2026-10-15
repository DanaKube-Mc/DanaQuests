package su.nightexpress.quests.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.nightexpress.quests.event.data.EventDataDao;
import su.nightexpress.quests.event.data.EventLeaderboard;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.data.LeaderboardEntry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EventLeaderboardTest {

    private Connection connection;
    private EventDataDao dao;
    private EventLeaderboard leaderboard;
    private final String eventId = "halloween_pumpkin";

    @BeforeEach
    public void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        Connection proxyConn = (Connection) java.lang.reflect.Proxy.newProxyInstance(
            Connection.class.getClassLoader(),
            new Class<?>[]{Connection.class},
            (proxy, method, args) -> {
                if ("close".equals(method.getName())) return null;
                return method.invoke(connection, args);
            }
        );
        dao = new EventDataDao(() -> proxyConn);
        dao.initTable();

        leaderboard = new EventLeaderboard(eventId, dao);
    }

    @AfterEach
    public void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    @Test
    public void testLeaderboardTopAndRanks() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        UUID u3 = UUID.randomUUID();

        dao.saveUserData(new EventUserData(u1, eventId, 10000L, Set.of(), System.currentTimeMillis()));
        dao.saveUserData(new EventUserData(u2, eventId, 50000L, Set.of(), System.currentTimeMillis()));
        dao.saveUserData(new EventUserData(u3, eventId, 25000L, Set.of(), System.currentTimeMillis()));

        // Set name resolver mock
        leaderboard.setNameResolver(uuid -> {
            if (uuid.equals(u1)) return "PlayerOne";
            if (uuid.equals(u2)) return "PlayerTwo";
            if (uuid.equals(u3)) return "PlayerThree";
            return "Unknown";
        });

        // Trigger refresh
        leaderboard.refreshSync();

        List<LeaderboardEntry> top = leaderboard.getTopEntries();
        assertEquals(3, top.size());

        // Rank 1
        assertEquals("PlayerTwo", leaderboard.getTopPlayerName(1));
        assertEquals(50000L, leaderboard.getTopPlayerScore(1));

        // Rank 2
        assertEquals("PlayerThree", leaderboard.getTopPlayerName(2));
        assertEquals(25000L, leaderboard.getTopPlayerScore(2));

        // Rank 3
        assertEquals("PlayerOne", leaderboard.getTopPlayerName(3));
        assertEquals(10000L, leaderboard.getTopPlayerScore(3));

        // Rank 4 (empty)
        assertEquals("---", leaderboard.getTopPlayerName(4));
        assertEquals(0L, leaderboard.getTopPlayerScore(4));

        // Player ranks
        assertEquals(1, leaderboard.getPlayerRank(u2, 50000L));
        assertEquals(2, leaderboard.getPlayerRank(u3, 25000L));
        assertEquals(3, leaderboard.getPlayerRank(u1, 10000L));
        assertEquals(-1, leaderboard.getPlayerRank(UUID.randomUUID(), 0L));
    }
}
