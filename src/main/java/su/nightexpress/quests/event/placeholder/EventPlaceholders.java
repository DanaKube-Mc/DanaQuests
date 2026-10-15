package su.nightexpress.quests.event.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.quests.event.EventQuestManager;
import su.nightexpress.quests.event.data.EventLeaderboard;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;

public class EventPlaceholders extends PlaceholderExpansion {

    private final EventQuestManager eventManager;

    public EventPlaceholders(@NotNull EventQuestManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    @NotNull
    public String getIdentifier() {
        return "danaquests";
    }

    @Override
    @NotNull
    public String getAuthor() {
        return "BaByDev";
    }

    @Override
    @NotNull
    public String getVersion() {
        return "5.0.3";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    @Nullable
    public String onRequest(@Nullable OfflinePlayer player, @NotNull String params) {
        return parse(player, params);
    }

    @Nullable
    public String parse(@Nullable OfflinePlayer player, @NotNull String params) {
        // Expected format: event_<id>_<type>
        if (!params.startsWith("event_")) {
            return null;
        }

        String sub = params.substring("event_".length());

        // Check for top_name_<N> or top_score_<N>
        // Example sub: "halloween_pumpkin_top_name_1" or "halloween_pumpkin_score"
        int topNameIdx = sub.indexOf("_top_name_");
        if (topNameIdx != -1) {
            String eventId = sub.substring(0, topNameIdx);
            int rank = parseRank(sub.substring(topNameIdx + "_top_name_".length()));
            EventLeaderboard lb = this.eventManager.getLeaderboard(eventId);
            return lb.getTopPlayerName(rank);
        }

        int topScoreIdx = sub.indexOf("_top_score_");
        if (topScoreIdx != -1) {
            String eventId = sub.substring(0, topScoreIdx);
            int rank = parseRank(sub.substring(topScoreIdx + "_top_score_".length()));
            EventLeaderboard lb = this.eventManager.getLeaderboard(eventId);
            return String.valueOf(lb.getTopPlayerScore(rank));
        }

        int lastUnderscore = sub.lastIndexOf('_');
        if (lastUnderscore == -1) return null;

        String eventId = sub.substring(0, lastUnderscore);
        String type = sub.substring(lastUnderscore + 1);

        // Specific multi-word types like time_left
        if (sub.endsWith("_time_left")) {
            eventId = sub.substring(0, sub.length() - "_time_left".length());
            type = "time_left";
        }

        EventQuest quest = this.eventManager.getQuest(eventId);

        switch (type.toLowerCase()) {
            case "score" -> {
                if (player == null) return "0";
                EventUserData data = this.eventManager.getUserData(player.getUniqueId(), eventId);
                return String.valueOf(data.getTotalScore());
            }
            case "rank" -> {
                if (player == null) return "Non classé";
                EventUserData data = this.eventManager.getUserData(player.getUniqueId(), eventId);
                long score = data.getTotalScore();
                if (score <= 0) return "Non classé";
                EventLeaderboard lb = this.eventManager.getLeaderboard(eventId);
                int rank = lb.getPlayerRank(player.getUniqueId(), score);
                return rank > 0 ? String.valueOf(rank) : "Non classé";
            }
            case "time_left" -> {
                if (quest == null) return "0s";
                return quest.getSchedule().getTimeLeftFormatted();
            }
            case "status" -> {
                if (quest == null) return "Inconnu";
                return quest.isActive() ? "Actif" : "Terminé";
            }
            default -> {
                return null;
            }
        }
    }

    private static int parseRank(@NotNull String str) {
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
