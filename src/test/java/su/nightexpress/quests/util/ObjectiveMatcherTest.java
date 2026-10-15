package su.nightexpress.quests.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectiveMatcherTest {

    @Test
    @DisplayName("Deepslate ore equivalence should match in both directions")
    void testDeepslateOreEquivalence() {
        // Iron ore
        assertTrue(ObjectiveMatcher.isMatch("IRON_ORE", "iron_ore"));
        assertTrue(ObjectiveMatcher.isMatch("IRON_ORE", "deepslate_iron_ore"));
        assertTrue(ObjectiveMatcher.isMatch("deepslate_iron_ore", "IRON_ORE"));

        // Coal ore
        assertTrue(ObjectiveMatcher.isMatch("COAL_ORE", "deepslate_coal_ore"));
        assertTrue(ObjectiveMatcher.isMatch("deepslate_coal_ore", "coal_ore"));

        // Copper ore
        assertTrue(ObjectiveMatcher.isMatch("COPPER_ORE", "deepslate_copper_ore"));
        assertTrue(ObjectiveMatcher.isMatch("deepslate_copper_ore", "copper_ore"));

        // Gold ore
        assertTrue(ObjectiveMatcher.isMatch("GOLD_ORE", "deepslate_gold_ore"));
        assertTrue(ObjectiveMatcher.isMatch("deepslate_gold_ore", "gold_ore"));

        // Redstone ore
        assertTrue(ObjectiveMatcher.isMatch("REDSTONE_ORE", "deepslate_redstone_ore"));
        assertTrue(ObjectiveMatcher.isMatch("deepslate_redstone_ore", "redstone_ore"));

        // Emerald ore
        assertTrue(ObjectiveMatcher.isMatch("EMERALD_ORE", "deepslate_emerald_ore"));
        assertTrue(ObjectiveMatcher.isMatch("deepslate_emerald_ore", "emerald_ore"));

        // Lapis ore
        assertTrue(ObjectiveMatcher.isMatch("LAPIS_ORE", "deepslate_lapis_ore"));
        assertTrue(ObjectiveMatcher.isMatch("deepslate_lapis_ore", "lapis_ore"));

        // Diamond ore
        assertTrue(ObjectiveMatcher.isMatch("DIAMOND_ORE", "deepslate_diamond_ore"));
        assertTrue(ObjectiveMatcher.isMatch("minecraft:diamond_ore", "minecraft:deepslate_diamond_ore"));
        assertTrue(ObjectiveMatcher.isMatch("DEEPSLATE_DIAMOND_ORE", "diamond_ore"));
    }

    @Test
    @DisplayName("Nether gold and nether quartz ores must remain distinct from overworld ores")
    void testNetherOresSeparate() {
        assertFalse(ObjectiveMatcher.isMatch("GOLD_ORE", "nether_gold_ore"));
        assertFalse(ObjectiveMatcher.isMatch("DEEPSLATE_GOLD_ORE", "nether_gold_ore"));
        assertFalse(ObjectiveMatcher.isMatch("nether_gold_ore", "gold_ore"));
        assertFalse(ObjectiveMatcher.isMatch("QUARTZ_ORE", "iron_ore"));
    }

    @Test
    @DisplayName("Generic ores wildcard should match any ore block")
    void testGenericOresGroup() {
        assertTrue(ObjectiveMatcher.isMatch("ores", "iron_ore"));
        assertTrue(ObjectiveMatcher.isMatch("ores", "deepslate_diamond_ore"));
        assertTrue(ObjectiveMatcher.isMatch("ores", "nether_gold_ore"));
        assertTrue(ObjectiveMatcher.isMatch("ores", "ancient_debris"));
        assertTrue(ObjectiveMatcher.isMatch("ore", "coal_ore"));
        assertTrue(ObjectiveMatcher.isMatch("any_ore", "deepslate_emerald_ore"));
        assertFalse(ObjectiveMatcher.isMatch("ores", "stone"));
    }

    @Test
    @DisplayName("Crop aliases should match singular and plural")
    void testCropAliases() {
        assertTrue(ObjectiveMatcher.isMatch("carrot", "carrots"));
        assertTrue(ObjectiveMatcher.isMatch("carrots", "carrot"));
        assertTrue(ObjectiveMatcher.isMatch("potato", "potatoes"));
        assertTrue(ObjectiveMatcher.isMatch("potatoes", "potato"));
        assertTrue(ObjectiveMatcher.isMatch("beetroot", "beetroots"));
        assertTrue(ObjectiveMatcher.isMatch("nether_wart", "nether_warts"));
    }

    @Test
    @DisplayName("Generic groups should match specific items/blocks")
    void testGenericGroups() {
        assertTrue(ObjectiveMatcher.isMatch("planks", "oak_planks"));
        assertTrue(ObjectiveMatcher.isMatch("planks", "birch_planks"));
        assertTrue(ObjectiveMatcher.isMatch("logs", "spruce_log"));
        assertTrue(ObjectiveMatcher.isMatch("tools", "diamond_pickaxe"));
        assertTrue(ObjectiveMatcher.isMatch("armor", "netherite_chestplate"));
        assertTrue(ObjectiveMatcher.isMatch("glass", "tinted_glass"));
    }

    @Test
    @DisplayName("Biome groups should match any biome in the group")
    void testBiomeGroups() {
        // Mountains
        assertTrue(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:jagged_peaks"));
        assertTrue(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:snowy_slopes"));
        assertTrue(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:frozen_peaks"));
        assertTrue(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:stony_peaks"));
        assertTrue(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:meadow"));
        assertTrue(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:grove"));
        assertTrue(ObjectiveMatcher.isMatch("biome:mountains", "biome:terralith:mountain_steppe"));
        assertFalse(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:plains"));
        assertFalse(ObjectiveMatcher.isMatch("biome:mountains", "biome:minecraft:desert"));

        // Caves
        assertTrue(ObjectiveMatcher.isMatch("biome:caves", "biome:minecraft:lush_caves"));
        assertTrue(ObjectiveMatcher.isMatch("biome:caves", "biome:minecraft:dripstone_caves"));
        assertTrue(ObjectiveMatcher.isMatch("biome:caves", "biome:terralith:caves/infested_caves"));

        // Oceans & Forests
        assertTrue(ObjectiveMatcher.isMatch("biome:oceans", "biome:minecraft:warm_ocean"));
        assertTrue(ObjectiveMatcher.isMatch("biome:forests", "biome:minecraft:birch_forest"));
    }

    @Test
    @DisplayName("Multi targets separated by comma should match any candidate")
    void testMultiTargetSeparators() {
        String multi = "biome:minecraft:jagged_peaks, biome:minecraft:snowy_slopes, biome:minecraft:frozen_peaks";
        assertTrue(ObjectiveMatcher.isMatch(multi, "biome:minecraft:jagged_peaks"));
        assertTrue(ObjectiveMatcher.isMatch(multi, "biome:minecraft:snowy_slopes"));
        assertTrue(ObjectiveMatcher.isMatch(multi, "biome:minecraft:frozen_peaks"));
        assertFalse(ObjectiveMatcher.isMatch(multi, "biome:minecraft:desert"));
    }
}
