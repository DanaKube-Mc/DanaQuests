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

    @Test
    public void testBuildProgressBarRoundsDownWhenNotComplete() {
        // 99.9% ne doit pas remplir tous les blocs si longueur = 10 (floor(10 * 0.999) = 9 blocs remplis, 1 bloc vide)
        String bar = MenuUtils.buildProgressBar(0.999);
        assertNotNull(bar);
        assertTrue(bar.contains("<gradient:#2ecc71:#a3cb38>"));
        assertTrue(bar.contains("<gray>")); // Contient encore une partie vide car pas 100%
    }

    @Test
    public void testFormatNumber() {
        assertEquals("10 000", MenuUtils.formatNumber(10000));
        assertEquals("1 000 000", MenuUtils.formatNumber(1000000));
        assertEquals("500", MenuUtils.formatNumber(500));
    }
}
