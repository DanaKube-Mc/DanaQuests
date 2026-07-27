package su.nightexpress.quests.community;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import su.nightexpress.quests.community.data.CommunityActiveEvent;
import su.nightexpress.quests.community.definition.CommunityQuest;
import su.nightexpress.quests.community.definition.RankingRewards;
import su.nightexpress.quests.community.log.CommunityLogger;
import su.nightexpress.quests.community.webhook.DiscordWebhookSender;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class CommunityQuestTest {

    @Test
    public void testEventSerializationAndPersistence() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        Map<UUID, Double> contributions = new HashMap<>();
        contributions.put(u1, 50000.0);
        contributions.put(u2, 150000.0);

        long now = System.currentTimeMillis();
        long end = now + 86400000L;

        CommunityActiveEvent event = new CommunityActiveEvent(
            "lumens_deposit",
            1000000.0,
            200000.0,
            now,
            end,
            contributions,
            false,
            true
        );

        String json = event.toJson();
        assertNotNull(json);
        assertTrue(json.contains("lumens_deposit"));

        CommunityActiveEvent deserialized = CommunityActiveEvent.fromJson(json);
        assertNotNull(deserialized);
        assertEquals("lumens_deposit", deserialized.getQuestId());
        assertEquals(1000000.0, deserialized.getTargetAmount());
        assertEquals(200000.0, deserialized.getCurrentAmount());
        assertEquals(now, deserialized.getStartTimestamp());
        assertEquals(end, deserialized.getEndTimestamp());
        assertFalse(deserialized.isCompleted());
        assertTrue(deserialized.isActive());
        assertEquals(2, deserialized.getContributions().size());
        assertEquals(50000.0, deserialized.getContribution(u1));
        assertEquals(150000.0, deserialized.getContribution(u2));
    }

    @Test
    public void testDepositAndCappingLogic() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();

        CommunityActiveEvent event = new CommunityActiveEvent();
        event.setQuestId("blocks_break");
        event.setTargetAmount(50000.0);
        event.setCurrentAmount(0.0);

        event.addContribution(u1, 20000.0);
        assertEquals(20000.0, event.getCurrentAmount());
        assertEquals(20000.0, event.getContribution(u1));
        assertFalse(event.isCompleted());

        // Deposit exceeding remaining target: target 50000, adding 40000
        event.addContribution(u2, 40000.0);
        assertEquals(50000.0, event.getCurrentAmount());
        assertEquals(40000.0, event.getContribution(u2));
        assertTrue(event.isCompleted());
    }

    @Test
    public void testRankingsCalculation() {
        UUID u1 = UUID.randomUUID();
        UUID u2 = UUID.randomUUID();
        UUID u3 = UUID.randomUUID();
        UUID u4 = UUID.randomUUID();

        CommunityActiveEvent event = new CommunityActiveEvent();
        event.setTargetAmount(10000.0);
        event.addContribution(u1, 1000.0);
        event.addContribution(u2, 5000.0);
        event.addContribution(u3, 3000.0);
        event.addContribution(u4, 50.0);

        List<Map.Entry<UUID, Double>> sorted = event.getContributions().entrySet().stream()
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .toList();

        assertEquals(4, sorted.size());
        assertEquals(u2, sorted.get(0).getKey());
        assertEquals(5000.0, sorted.get(0).getValue());

        assertEquals(u3, sorted.get(1).getKey());
        assertEquals(3000.0, sorted.get(1).getValue());

        assertEquals(u1, sorted.get(2).getKey());
        assertEquals(1000.0, sorted.get(2).getValue());

        assertEquals(u4, sorted.get(3).getKey());
        assertEquals(50.0, sorted.get(3).getValue());

        // Test min participation threshold (e.g. 500.0)
        double minParticipation = 500.0;
        List<UUID> eligibleForParticipation = sorted.stream()
            .filter(e -> e.getValue() >= minParticipation)
            .map(Map.Entry::getKey)
            .toList();

        assertEquals(3, eligibleForParticipation.size());
        assertFalse(eligibleForParticipation.contains(u4));
    }

    @Test
    public void testDiscordWebhookJsonFormatting() {
        CommunityQuest quest = new CommunityQuest(
            "lumens_deposit",
            "Dépôt de Lumens Communautaire",
            List.of("Description 1", "Description 2"),
            "MONEY_DEPOSIT",
            1000000.0,
            48,
            null,
            0,
            List.of("broadcast win"),
            new RankingRewards()
        );

        CommunityActiveEvent event = new CommunityActiveEvent(
            "lumens_deposit",
            1000000.0,
            1000000.0,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 3600000,
            new HashMap<>(),
            true,
            false
        );

        List<Map.Entry<String, Double>> top = List.of(
            new AbstractMap.SimpleEntry<>("Player1", 500000.0),
            new AbstractMap.SimpleEntry<>("Player2", 300000.0),
            new AbstractMap.SimpleEntry<>("Player3", 200000.0)
        );

        String startJson = DiscordWebhookSender.buildStartJson(quest, event);
        assertNotNull(startJson);
        assertTrue(startJson.contains("Nouvel Événement Communautaire"));
        assertTrue(startJson.contains("1000000"));

        String victoryJson = DiscordWebhookSender.buildVictoryJson(quest, event, top);
        assertNotNull(victoryJson);
        assertTrue(victoryJson.contains("VICTOIRE"));
        assertTrue(victoryJson.contains("Player1"));

        String failureJson = DiscordWebhookSender.buildFailureJson(quest, event, top);
        assertNotNull(failureJson);
        assertTrue(failureJson.contains("ÉCHEC"));
    }

    @Test
    public void testCommunityLoggerContentAndWriting(@TempDir Path tempDir) throws Exception {
        CommunityQuest quest = new CommunityQuest(
            "mobs_kill",
            "Chasse aux Monstres Globale",
            List.of("Kill mobs together"),
            "KILL_MOB_GLOBAL",
            10000.0,
            24,
            null,
            0,
            List.of("broadcast mobs killed"),
            new RankingRewards()
        );

        CommunityActiveEvent event = new CommunityActiveEvent(
            "mobs_kill",
            10000.0,
            10000.0,
            1700000000000L,
            1700086400000L,
            new HashMap<>(),
            true,
            false
        );

        List<Map.Entry<String, Double>> top = List.of(
            new AbstractMap.SimpleEntry<>("HeroPlayer", 6000.0),
            new AbstractMap.SimpleEntry<>("SidekickPlayer", 4000.0)
        );

        String logContent = CommunityLogger.buildLogContent(quest, event, top);
        assertNotNull(logContent);
        assertTrue(logContent.contains("COMMUNITY EVENT REPORT - LOG"));
        assertTrue(logContent.contains("mobs_kill"));
        assertTrue(logContent.contains("HeroPlayer"));

        File tempFolder = tempDir.toFile();
        File writtenFile = CommunityLogger.writeLogFile(tempFolder, logContent, event.getStartTimestamp());
        assertNotNull(writtenFile);
        assertTrue(writtenFile.exists());

        String fileText = Files.readString(writtenFile.toPath());
        assertEquals(logContent, fileText);
    }
}
