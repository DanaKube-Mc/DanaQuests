package su.nightexpress.quests.event;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventTier;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class EventUserDataTest {

    @Test
    public void testScoreAccumulationAndTierClaiming() {
        UUID uuid = UUID.randomUUID();
        String eventId = "halloween_pumpkin";

        EventUserData userData = new EventUserData(uuid, eventId);

        assertEquals(uuid, userData.getPlayerUuid());
        assertEquals(eventId, userData.getEventId());
        assertEquals(0L, userData.getTotalScore());
        assertTrue(userData.getClaimedTiers().isEmpty());

        userData.addScore(15000);
        assertEquals(15000L, userData.getTotalScore());

        EventTier tier1 = new EventTier(1, 10000L, "Palier 1", Material.IRON_INGOT, 0, List.of());
        EventTier tier2 = new EventTier(2, 50000L, "Palier 2", Material.GOLD_INGOT, 0, List.of());

        assertTrue(userData.canClaim(tier1));
        assertFalse(userData.canClaim(tier2));

        assertTrue(userData.claimTier(1));
        assertTrue(userData.isTierClaimed(1));
        assertFalse(userData.canClaim(tier1)); // Déjà réclamé

        // Ne peut pas réclamer une deuxième fois
        assertFalse(userData.claimTier(1));

        // Score cumulatif supplémentaire
        userData.addScore(40000); // Total 55000
        assertEquals(55000L, userData.getTotalScore());
        assertTrue(userData.canClaim(tier2));
        assertTrue(userData.claimTier(2));
        assertTrue(userData.isTierClaimed(2));
    }

    @Test
    public void testSerializationAndDeserialization() {
        UUID uuid = UUID.randomUUID();
        EventUserData userData = new EventUserData(uuid, "event_test", 42000L, Set.of(1, 2), 123456789L);

        String claimedTiersString = userData.getClaimedTiersSerialized();
        Set<Integer> parsed = EventUserData.deserializeClaimedTiers(claimedTiersString);

        assertEquals(2, parsed.size());
        assertTrue(parsed.contains(1));
        assertTrue(parsed.contains(2));
    }
}
