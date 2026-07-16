package su.nightexpress.quests.island;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import su.nightexpress.quests.island.data.IslandQuestProgress;
import su.nightexpress.quests.island.definition.IslandQuest;
import su.nightexpress.quests.island.definition.IslandQuestRequirement;
import su.nightexpress.quests.island.definition.IslandResourceGroup;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class IslandQuestTest {

    private ServerMock server;
    private TestSkyblockHook testHook;

    private static class TestSkyblockHook implements su.nightexpress.quests.hook.ISkyblockHook {
        public UUID islandUuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        public List<Player> members = new ArrayList<>();

        @Override
        public UUID getIslandUUID(Player player) {
            return islandUuid;
        }

        @Override
        public List<Player> getOnlineMembers(UUID islandUuid) {
            return members;
        }

        @Override
        public String getIslandLeaderName(UUID islandUuid) {
            return "Leader";
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
        server = MockBukkit.mock();
        testHook = new TestSkyblockHook();

        java.lang.reflect.Field field = su.nightexpress.quests.hook.SkyblockHookManager.class.getDeclaredField("hookInstance");
        field.setAccessible(true);
        field.set(null, testHook);
        
        java.lang.reflect.Field checkedField = su.nightexpress.quests.hook.SkyblockHookManager.class.getDeclaredField("checked");
        checkedField.setAccessible(true);
        checkedField.set(null, true);
    }

    @AfterEach
    public void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    public void testLockConcurrent() {
        IslandLockManager lockManager = new IslandLockManager();
        UUID island = UUID.randomUUID();
        UUID player1 = UUID.randomUUID();
        UUID player2 = UUID.randomUUID();

        assertTrue(lockManager.acquireLock(island, player1));
        assertEquals(player1, lockManager.getLockHolder(island));
        assertEquals(island, lockManager.getPlayerLockedIsland(player1));

        assertFalse(lockManager.acquireLock(island, player2));

        assertTrue(lockManager.acquireLock(island, player1));

        lockManager.releasePlayerLock(player1);
        assertNull(lockManager.getLockHolder(island));

        assertTrue(lockManager.acquireLock(island, player2));
        assertEquals(player2, lockManager.getLockHolder(island));

        lockManager.releaseLock(island);
        assertNull(lockManager.getLockHolder(island));
    }

    @Test
    public void testWeightConversion() {
        Map<Material, Double> materials = new HashMap<>();
        materials.put(Material.DIAMOND, 10.0);
        materials.put(Material.GOLD_INGOT, 2.0);
        materials.put(Material.IRON_INGOT, 1.0);

        IslandResourceGroup group = new IslandResourceGroup("minerals", "Minerals", materials);

        assertEquals("minerals", group.getId());
        assertEquals("Minerals", group.getName());
        assertEquals(10.0, group.getWeight(Material.DIAMOND));
        assertEquals(2.0, group.getWeight(Material.GOLD_INGOT));
        assertEquals(1.0, group.getWeight(Material.IRON_INGOT));
        assertEquals(0.0, group.getWeight(Material.COAL));

        assertTrue(group.contains(Material.DIAMOND));
        assertFalse(group.contains(Material.COAL));
    }

    @Test
    public void testSerialization() {
        com.google.gson.Gson gson = new com.google.gson.Gson();
        Map<String, Integer> progressMap = new HashMap<>();
        progressMap.put("iron_deposit", 50);
        progressMap.put("wheat_deposit", 120);

        String json = gson.toJson(progressMap);
        java.lang.reflect.Type type = new com.google.common.reflect.TypeToken<Map<String, Integer>>(){}.getType();
        Map<String, Integer> deserialized = gson.fromJson(json, type);

        assertEquals(progressMap, deserialized);
        assertEquals(50, deserialized.get("iron_deposit"));
        assertEquals(120, deserialized.get("wheat_deposit"));
    }

    @Test
    public void testDepositPartialAndComplete() {
        IslandManager manager = new IslandManager(null) {
            private final Map<String, IslandQuestProgress> testProgress = new HashMap<>();

            @Override
            public UUID getPlayerIsland(Player player) {
                return testHook.islandUuid;
            }

            @Override
            public IslandQuestProgress getProgress(UUID islandUuid, String questId) {
                String key = islandUuid.toString() + "_" + questId;
                return testProgress.computeIfAbsent(key, k -> new IslandQuestProgress(islandUuid, questId));
            }

            @Override
            public void saveProgress(IslandQuestProgress progress) {
                // No-op for testing
            }

            @Override
            protected void handleQuestCompletion(Player player, UUID islandUuid, IslandQuest quest) {
                // No-op to bypass calling uninitialized sendTitle and Lang systems
            }
        };

        Map<Material, Double> materials = new HashMap<>();
        materials.put(Material.IRON_INGOT, 1.0);
        IslandResourceGroup group = new IslandResourceGroup("minerals_test", "Minerals Test", materials);
        manager.getResourceGroups().put("minerals_test", group);

        IslandQuestRequirement req = new IslandQuestRequirement("iron_test_deposit", "Iron Test Deposit", "minerals_test", 100);
        IslandQuest quest = new IslandQuest("quest_test_1", "Quest Test 1", 1, Collections.singletonList(req), Collections.singletonList("eco give %player% 500"));
        manager.getQuests().put("quest_test_1", quest);

        Player player = server.addPlayer("Tester");
        testHook.members.add(player);

        player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 64));

        assertTrue(manager.getLockManager().acquireLock(testHook.islandUuid, player.getUniqueId()));

        IslandQuestProgress progress = manager.getProgress(testHook.islandUuid, "quest_test_1");
        progress.setRequirementProgress("iron_test_deposit", 0);

        manager.deposit(player, req, false);

        assertEquals(64, progress.getRequirementProgress("iron_test_deposit"));
        assertFalse(player.getInventory().contains(Material.IRON_INGOT));

        player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 64));
        player.getInventory().addItem(new ItemStack(Material.IRON_INGOT, 64));

        manager.deposit(player, req, true);

        assertEquals(100, progress.getRequirementProgress("iron_test_deposit"));
        assertTrue(progress.isCompleted());

        int remainingIron = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.IRON_INGOT) {
                remainingIron += item.getAmount();
            }
        }
        assertEquals(92, remainingIron);
    }
}
