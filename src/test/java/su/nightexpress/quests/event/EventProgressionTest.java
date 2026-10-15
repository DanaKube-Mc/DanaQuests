package su.nightexpress.quests.event;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import su.nightexpress.nightcore.util.PDCUtil;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;
import su.nightexpress.quests.event.definition.EventSchedule;
import su.nightexpress.quests.event.definition.EventTier;
import su.nightexpress.quests.event.listener.EventPickupListener;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class EventProgressionTest {

    private ServerMock server;
    private EventQuestManager eventManager;
    private EventPickupListener pickupListener;

    @BeforeEach
    public void setUp() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
        server = MockBukkit.mock();
        eventManager = new EventQuestManager(null);
        pickupListener = new EventPickupListener(null, eventManager);
    }

    @AfterEach
    public void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    private EventQuest createActiveHalloweenQuest() {
        LocalDateTime now = LocalDateTime.now();
        String start = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String end = now.plusDays(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        EventSchedule schedule = EventSchedule.createCalendar(start, end);
        EventTier tier1 = new EventTier(1, 1000L, "Palier 1", Material.IRON_INGOT, 0, List.of());

        return new EventQuest(
            "halloween_pumpkin",
            "Halloween",
            List.of(),
            Material.JACK_O_LANTERN,
            0,
            schedule,
            "PICKUP_ITEM",
            Set.of(Material.PUMPKIN, Material.CARVED_PUMPKIN),
            Map.of(1, tier1),
            "GUI",
            54
        );
    }

    @Test
    public void testNaturalPickupProgressesScore() {
        PlayerMock player = server.addPlayer();
        EventQuest quest = createActiveHalloweenQuest();
        eventManager.registerQuest(quest);

        ItemStack naturalPumpkin = new ItemStack(Material.PUMPKIN, 64);
        Item itemEntity = player.getWorld().dropItem(player.getLocation(), naturalPumpkin);

        pickupListener.handlePickup(player, itemEntity, 64);

        EventUserData userData = eventManager.getUserData(player.getUniqueId(), "halloween_pumpkin");
        assertEquals(64L, userData.getTotalScore());

        // Second pickup
        pickupListener.handlePickup(player, itemEntity, 36);
        assertEquals(100L, userData.getTotalScore());
    }

    @Test
    public void testPlayerOriginatedPickupIgnored() {
        PlayerMock player = server.addPlayer();
        EventQuest quest = createActiveHalloweenQuest();
        eventManager.registerQuest(quest);

        ItemStack playerPumpkin = new ItemStack(Material.PUMPKIN, 64);
        Item itemEntity = player.getWorld().dropItem(player.getLocation(), playerPumpkin);
        NamespacedKey key = new NamespacedKey("danaquests", "player_originated");
        PDCUtil.set(itemEntity, key, true);

        pickupListener.handlePickup(player, itemEntity, 64);

        EventUserData userData = eventManager.getUserData(player.getUniqueId(), "halloween_pumpkin");
        assertEquals(0L, userData.getTotalScore());
    }

    @Test
    public void testUnrelatedMaterialIgnored() {
        PlayerMock player = server.addPlayer();
        EventQuest quest = createActiveHalloweenQuest();
        eventManager.registerQuest(quest);

        ItemStack diamond = new ItemStack(Material.DIAMOND, 10);
        Item itemEntity = player.getWorld().dropItem(player.getLocation(), diamond);

        pickupListener.handlePickup(player, itemEntity, 10);

        EventUserData userData = eventManager.getUserData(player.getUniqueId(), "halloween_pumpkin");
        assertEquals(0L, userData.getTotalScore());
    }

    @Test
    public void testExpiredEventDoesNotProgress() {
        PlayerMock player = server.addPlayer();
        EventQuest quest = createActiveHalloweenQuest();
        quest.getSchedule().stop(); // Expire event
        eventManager.registerQuest(quest);

        ItemStack naturalPumpkin = new ItemStack(Material.PUMPKIN, 64);
        Item itemEntity = player.getWorld().dropItem(player.getLocation(), naturalPumpkin);

        pickupListener.handlePickup(player, itemEntity, 64);

        EventUserData userData = eventManager.getUserData(player.getUniqueId(), "halloween_pumpkin");
        assertEquals(0L, userData.getTotalScore());
    }
}
