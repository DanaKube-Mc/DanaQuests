package su.nightexpress.quests.event;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import su.nightexpress.quests.event.definition.EventQuest;
import su.nightexpress.quests.event.definition.EventTier;

import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class EventQuestTest {

    private static final String SAMPLE_YAML = """
        id: "halloween_pumpkin"
        name: "<#ff7b00><b>Fête de la Citrouille</b>"
        description:
          - "<gray>Récoltez un maximum de citrouilles !"
          - "<gray>Grimpez dans le classement et franchissez les paliers."
        icon:
          material: "JACK_O_LANTERN"
          custom-model-data: 10003
        
        schedule:
          mode: "CALENDAR"
          start-date: "2026-10-20 00:00:00"
          end-date: "2026-11-05 23:59:59"
        
        target:
          type: "PICKUP_ITEM"
          materials:
            - "PUMPKIN"
            - "CARVED_PUMPKIN"
            - "JACK_O_LANTERN"
        
        tiers:
          1:
            requirement: 10000
            display-name: "<#ffaa00>Palier I : Apprenti Récolteur"
            icon:
              material: "IRON_INGOT"
            rewards:
              - "money_5000"
              - "event_key_1"
          2:
            requirement: 50000
            display-name: "<#ff7b00>Palier II : Maître Citrouille"
            icon:
              material: "GOLD_INGOT"
            rewards:
              - "money_25000"
              - "title_pumpkin_lord"
        
        gui:
          title: "<#ff7b00>Événement : Citrouilles"
          size: 54
        """;

    @Test
    public void testEventQuestYamlParsing() {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new StringReader(SAMPLE_YAML));
        EventQuest quest = EventQuest.fromYaml(config);

        assertNotNull(quest);
        assertEquals("halloween_pumpkin", quest.getId());
        assertEquals("<#ff7b00><b>Fête de la Citrouille</b>", quest.getName());
        assertEquals(Material.JACK_O_LANTERN, quest.getIconMaterial());
        assertEquals(10003, quest.getCustomModelData());

        // Target matching
        assertEquals("PICKUP_ITEM", quest.getTargetType());
        assertTrue(quest.matchesMaterial(Material.PUMPKIN));
        assertTrue(quest.matchesMaterial(Material.CARVED_PUMPKIN));
        assertTrue(quest.matchesMaterial(Material.JACK_O_LANTERN));
        assertFalse(quest.matchesMaterial(Material.DIAMOND));

        // Tiers
        Map<Integer, EventTier> tiers = quest.getTiers();
        assertEquals(2, tiers.size());

        EventTier tier1 = quest.getTier(1);
        assertNotNull(tier1);
        assertEquals(10000L, tier1.getRequirement());
        assertEquals("<#ffaa00>Palier I : Apprenti Récolteur", tier1.getDisplayName());
        assertEquals(Material.IRON_INGOT, tier1.getIconMaterial());
        assertEquals(2, tier1.getRewards().size());

        EventTier tier2 = quest.getTier(2);
        assertNotNull(tier2);
        assertEquals(50000L, tier2.getRequirement());

        // GUI
        assertEquals("<#ff7b00>Événement : Citrouilles", quest.getGuiTitle());
        assertEquals(54, quest.getGuiSize());
    }
}
