package su.nightexpress.quests.event.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EventDataDao {

    @FunctionalInterface
    public interface ConnectionProvider {
        Connection getConnection() throws SQLException;
    }

    public static final String TABLE_NAME = "danaquests_event_user_data";

    private final ConnectionProvider connectionProvider;

    public EventDataDao(@NotNull ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
            "player_uuid_event_id VARCHAR(100) PRIMARY KEY, " +
            "player_uuid VARCHAR(36) NOT NULL, " +
            "event_id VARCHAR(64) NOT NULL, " +
            "total_score BIGINT NOT NULL DEFAULT 0, " +
            "claimed_tiers TEXT, " +
            "last_updated BIGINT NOT NULL" +
            ")";
        try (Connection conn = connectionProvider.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveUserData(@NotNull EventUserData data) {
        String key = data.getPlayerUuid().toString() + ":" + data.getEventId();
        String sql = "INSERT INTO " + TABLE_NAME + " " +
            "(player_uuid_event_id, player_uuid, event_id, total_score, claimed_tiers, last_updated) " +
            "VALUES (?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT(player_uuid_event_id) DO UPDATE SET " +
            "total_score = excluded.total_score, " +
            "claimed_tiers = excluded.claimed_tiers, " +
            "last_updated = excluded.last_updated";

        try (Connection conn = connectionProvider.getConnection()) {
            // Check if SQLite supports UPSERT or if we do standard delete-then-insert for max compatibility (MySQL/SQLite)
            saveWithFallback(conn, key, data);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveWithFallback(@NotNull Connection conn, @NotNull String key, @NotNull EventUserData data) throws SQLException {
        // Universal approach compatible with both SQLite and MySQL: delete then insert
        String deleteSql = "DELETE FROM " + TABLE_NAME + " WHERE player_uuid_event_id = ?";
        try (PreparedStatement del = conn.prepareStatement(deleteSql)) {
            del.setString(1, key);
            del.executeUpdate();
        }

        String insertSql = "INSERT INTO " + TABLE_NAME + " " +
            "(player_uuid_event_id, player_uuid, event_id, total_score, claimed_tiers, last_updated) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
            ins.setString(1, key);
            ins.setString(2, data.getPlayerUuid().toString());
            ins.setString(3, data.getEventId());
            ins.setLong(4, data.getTotalScore());
            ins.setString(5, data.getClaimedTiersSerialized());
            ins.setLong(6, data.getLastUpdated());
            ins.executeUpdate();
        }
    }

    @Nullable
    public EventUserData loadUserData(@NotNull UUID playerUuid, @NotNull String eventId) {
        String key = playerUuid.toString() + ":" + eventId;
        String sql = "SELECT total_score, claimed_tiers, last_updated FROM " + TABLE_NAME + " WHERE player_uuid_event_id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    long score = rs.getLong("total_score");
                    String claimedRaw = rs.getString("claimed_tiers");
                    long updated = rs.getLong("last_updated");
                    Set<Integer> claimed = EventUserData.deserializeClaimedTiers(claimedRaw == null ? "" : claimedRaw);
                    return new EventUserData(playerUuid, eventId, score, claimed, updated);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new EventUserData(playerUuid, eventId);
    }

    @NotNull
    public List<LeaderboardEntry> loadTopScores(@NotNull String eventId, int limit) {
        List<LeaderboardEntry> list = new ArrayList<>();
        String sql = "SELECT player_uuid, total_score FROM " + TABLE_NAME +
            " WHERE event_id = ? AND total_score > 0 ORDER BY total_score DESC LIMIT ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, eventId);
            stmt.setInt(2, Math.max(1, limit));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID uuid = UUID.fromString(rs.getString("player_uuid"));
                    long score = rs.getLong("total_score");
                    list.add(new LeaderboardEntry(uuid, score));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int getPlayerRank(@NotNull UUID playerUuid, @NotNull String eventId, long totalScore) {
        if (totalScore <= 0) {
            return -1; // Unranked
        }
        String sql = "SELECT COUNT(*) + 1 AS rank FROM " + TABLE_NAME +
            " WHERE event_id = ? AND total_score > ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, eventId);
            stmt.setLong(2, totalScore);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("rank");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void resetUserData(@NotNull UUID playerUuid, @NotNull String eventId) {
        String key = playerUuid.toString() + ":" + eventId;
        String sql = "DELETE FROM " + TABLE_NAME + " WHERE player_uuid_event_id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
