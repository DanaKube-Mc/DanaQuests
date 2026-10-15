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

import static org.junit.jupiter.api.Assertions.*;

public class UnifiedAntiAbuseTest {

    private ServerMock server;
    private NamespacedKey key;

    @BeforeEach
    public void setUp() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
        server = MockBukkit.mock();
        key = new NamespacedKey("danaquests", "player_originated");
    }

    @AfterEach
    public void tearDown() {
        if (MockBukkit.isMocked()) {
            MockBukkit.unmock();
        }
    }

    @Test
    public void testPlayerOriginatedTaggingOnEntityAndStack() {
        PlayerMock player = server.addPlayer();
        ItemStack stack = new ItemStack(Material.PUMPKIN, 5);
        Item itemEntity = player.getWorld().dropItem(player.getLocation(), stack);

        assertFalse(PDCUtil.getBoolean(itemEntity, key).orElse(false));

        PDCUtil.set(itemEntity, key, true);
        assertTrue(PDCUtil.getBoolean(itemEntity, key).orElse(false));

        // Test marking on ItemStack directly
        ItemStack stack2 = new ItemStack(Material.PUMPKIN, 2);
        org.bukkit.inventory.meta.ItemMeta meta = stack2.getItemMeta();
        assertNotNull(meta);
        meta.getPersistentDataContainer().set(key, org.bukkit.persistence.PersistentDataType.BOOLEAN, true);
        stack2.setItemMeta(meta);

        Item itemEntity2 = player.getWorld().dropItem(player.getLocation(), stack2);

        Boolean isOriginated = itemEntity2.getItemStack().getItemMeta()
            .getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.BOOLEAN);
        assertTrue(Boolean.TRUE.equals(isOriginated));
    }
}
