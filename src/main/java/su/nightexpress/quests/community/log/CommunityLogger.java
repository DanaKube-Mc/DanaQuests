package su.nightexpress.quests.community.log;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.community.data.CommunityActiveEvent;
import su.nightexpress.quests.community.definition.CommunityQuest;
import su.nightexpress.quests.config.Config;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class CommunityLogger {

    public static File logEvent(@NotNull QuestsPlugin plugin, CommunityQuest quest, @NotNull CommunityActiveEvent event, List<Map.Entry<String, Double>> sortedRankings) {
        String logContent = buildLogContent(quest, event, sortedRankings);
        return writeLogFile(plugin.getDataFolder(), logContent, event.getStartTimestamp());
    }

    public static String buildLogContent(CommunityQuest quest, CommunityActiveEvent event, List<Map.Entry<String, Double>> sortedRankings) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        StringBuilder sb = new StringBuilder();

        sb.append("==================================================\n");
        sb.append("        COMMUNITY EVENT REPORT - LOG              \n");
        sb.append("==================================================\n");
        sb.append("Logged Date: ").append(sdf.format(new Date())).append("\n");
        sb.append("Quest ID: ").append(event.getQuestId()).append("\n");
        sb.append("Display Name: ").append(quest != null ? quest.getDisplayName() : event.getQuestId()).append("\n");
        sb.append("Status: ").append(event.isCompleted() ? "COMPLETED (SUCCESS)" : "FAILED (TIME EXPIRED)").append("\n");

        double percent = event.getTargetAmount() > 0 ? (event.getCurrentAmount() * 100.0 / event.getTargetAmount()) : 0.0;
        sb.append("Target Amount: ").append(String.format("%.2f", event.getTargetAmount())).append("\n");
        sb.append("Current Amount: ").append(String.format("%.2f", event.getCurrentAmount())).append(" (").append(String.format("%.1f", percent)).append("%)\n");
        sb.append("Start Date: ").append(sdf.format(new Date(event.getStartTimestamp()))).append("\n");
        sb.append("End Date: ").append(sdf.format(new Date(event.getEndTimestamp()))).append("\n");
        sb.append("Total Unique Contributors: ").append(event.getContributions().size()).append("\n");
        sb.append("--------------------------------------------------\n");
        sb.append("CONTRIBUTOR RANKINGS:\n");
        sb.append("--------------------------------------------------\n");

        if (sortedRankings != null && !sortedRankings.isEmpty()) {
            int rank = 1;
            for (Map.Entry<String, Double> entry : sortedRankings) {
                double amount = entry.getValue();
                double share = event.getCurrentAmount() > 0 ? (amount * 100.0 / event.getCurrentAmount()) : 0.0;
                sb.append(String.format("#%-3d | Player: %-25s | Contribution: %-12.2f (%.1f%%)\n",
                    rank++, entry.getKey(), amount, share));
            }
        } else {
            sb.append("No contributions recorded.\n");
        }

        sb.append("==================================================\n");
        return sb.toString();
    }

    public static File writeLogFile(File dataFolder, String content, long timestamp) {
        String subPath = Config.COMMUNITY_LOGS_PATH.get();
        File dir = new File(dataFolder, subPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File logFile = new File(dir, "event_" + timestamp + ".txt");
        try (FileWriter writer = new FileWriter(logFile)) {
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logFile;
    }
}
