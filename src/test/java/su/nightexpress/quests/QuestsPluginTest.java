package su.nightexpress.quests;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import su.nightexpress.quests.util.MenuUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QuestsPluginTest {

    private ServerMock server;

    @BeforeEach
    public void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    public void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    public void testMockBukkitStartup() {
        assertNotNull(server);
    }

    @Test
    public void testBuildProgressBar() {
        String bar = MenuUtils.buildProgressBar(0.5);
        assertNotNull(bar);
        assertTrue(bar.contains("<gradient:#2ecc71:#a3cb38>"));
        assertTrue(bar.contains("</gradient>"));
        assertTrue(bar.contains("<gray>"));
        assertTrue(bar.contains("</gray>"));
    }
}
