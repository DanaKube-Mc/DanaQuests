package su.nightexpress.quests.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class ObjectiveMatcher {

    private static final Set<String> DEEPSLATE_ORES = Set.of(
        "coal_ore",
        "copper_ore",
        "iron_ore",
        "gold_ore",
        "redstone_ore",
        "emerald_ore",
        "lapis_ore",
        "diamond_ore"
    );

    private static final Set<String> MOUNTAIN_BIOMES = Set.of(
        "jagged_peaks",
        "frozen_peaks",
        "stony_peaks",
        "snowy_slopes",
        "meadow",
        "grove",
        "windswept_hills",
        "windswept_gravelly_hills",
        "windswept_forest"
    );

    private static final Set<String> CAVE_BIOMES = Set.of(
        "lush_caves",
        "dripstone_caves",
        "deep_dark"
    );

    private static final Set<String> OCEAN_BIOMES = Set.of(
        "ocean",
        "deep_ocean",
        "warm_ocean",
        "lukewarm_ocean",
        "deep_lukewarm_ocean",
        "cold_ocean",
        "deep_cold_ocean",
        "frozen_ocean",
        "deep_frozen_ocean"
    );

    private static final Set<String> FOREST_BIOMES = Set.of(
        "forest",
        "flower_forest",
        "birch_forest",
        "old_growth_birch_forest",
        "dark_forest",
        "pale_garden"
    );

    private static final Set<String> TAIGA_BIOMES = Set.of(
        "taiga",
        "snowy_taiga",
        "old_growth_pine_taiga",
        "old_growth_spruce_taiga"
    );

    private static final Set<String> BADLANDS_BIOMES = Set.of(
        "badlands",
        "eroded_badlands",
        "wooded_badlands"
    );

    private static final Set<String> SWAMP_BIOMES = Set.of(
        "swamp",
        "mangrove_swamp"
    );

    private static final Set<String> JUNGLE_BIOMES = Set.of(
        "jungle",
        "sparse_jungle",
        "bamboo_jungle"
    );

    private static final Set<String> NETHER_BIOMES = Set.of(
        "nether_wastes",
        "soul_sand_valley",
        "crimson_forest",
        "warped_forest",
        "basalt_deltas"
    );

    private static final Set<String> END_BIOMES = Set.of(
        "the_end",
        "small_end_islands",
        "end_midlands",
        "end_highlands",
        "end_barrens"
    );

    private ObjectiveMatcher() {}

    /**
     * Nettoie et normalise un identifiant d'objectif (retire le namespace minecraft: et passe en minuscules).
     */
    @NotNull
    public static String normalize(@Nullable String key) {
        if (key == null) return "";
        return key.trim().replace("minecraft:", "").toLowerCase();
    }

    /**
     * Vérifie si un événement correspond à l'objectif demandé.
     *
     * @param questObjective L'objectif défini dans la quête (ex: IRON_ORE, minecraft:deepslate_iron_ore, biome:mountains, planks)
     * @param eventObjective L'identifiant issu de l'événement en jeu (ex: minecraft:deepslate_iron_ore, biome:minecraft:jagged_peaks)
     * @return true si l'événement valide l'objectif
     */
    public static boolean isMatch(@Nullable String questObjective, @Nullable String eventObjective) {
        if (questObjective == null || eventObjective == null) return false;
        if (questObjective.equalsIgnoreCase(eventObjective)) return true;

        // Support de plusieurs objectifs séparés par virgule, point-virgule ou pipe
        if (questObjective.contains(",") || questObjective.contains(";") || questObjective.contains("|")) {
            String[] subTargets = questObjective.split("[,;|]");
            for (String sub : subTargets) {
                String trimmed = sub.trim();
                if (!trimmed.isEmpty() && isMatch(trimmed, eventObjective)) {
                    return true;
                }
            }
            return false;
        }

        String cleanQuest = normalize(questObjective);
        String cleanEvent = normalize(eventObjective);

        if (cleanQuest.isEmpty() || cleanEvent.isEmpty()) return false;
        if (cleanQuest.equals(cleanEvent)) return true;

        // 1. Groupes de biomes (ex: biome:mountains <-> biome:jagged_peaks)
        if (isBiomeMatch(cleanQuest, cleanEvent)) {
            return true;
        }

        // 2. Équivalence des minerais (Deepslate <-> Normal)
        if (isOreMatch(cleanQuest, cleanEvent)) {
            return true;
        }

        // 3. Alias spécifiques (cultures, entités, pluriels singuliers)
        if (isAliasMatch(cleanQuest, cleanEvent)) {
            return true;
        }

        // 4. Groupes d'objectifs génériques (planks, logs, tools, etc.)
        if (isGroupMatch(cleanQuest, cleanEvent)) {
            return true;
        }

        return false;
    }

    /**
     * Vérifie si un événement de biome correspond à un groupe de biome demandé.
     */
    public static boolean isBiomeMatch(@NotNull String cleanQuest, @NotNull String cleanEvent) {
        String questBiome = cleanQuest.startsWith("biome:") ? cleanQuest.substring(6) : cleanQuest;
        String eventBiome = cleanEvent.startsWith("biome:") ? cleanEvent.substring(6) : cleanEvent;

        if (questBiome.equals("mountains") || questBiome.equals("mountain") || questBiome.equals("montagne") || questBiome.equals("montagnes")) {
            return MOUNTAIN_BIOMES.contains(eventBiome) || eventBiome.contains("peak") || eventBiome.contains("slopes") || eventBiome.contains("mountain") || eventBiome.contains("cliff");
        }
        if (questBiome.equals("caves") || questBiome.equals("cave") || questBiome.equals("grotte") || questBiome.equals("grottes") || questBiome.equals("cavernes")) {
            return CAVE_BIOMES.contains(eventBiome) || eventBiome.contains("cave");
        }
        if (questBiome.equals("oceans") || questBiome.equals("ocean")) {
            return OCEAN_BIOMES.contains(eventBiome) || eventBiome.contains("ocean");
        }
        if (questBiome.equals("forests") || questBiome.equals("forest") || questBiome.equals("foret") || questBiome.equals("forêt")) {
            return FOREST_BIOMES.contains(eventBiome) || eventBiome.contains("forest");
        }
        if (questBiome.equals("taiga") || questBiome.equals("taigas")) {
            return TAIGA_BIOMES.contains(eventBiome) || eventBiome.contains("taiga");
        }
        if (questBiome.equals("badlands") || questBiome.equals("mesa")) {
            return BADLANDS_BIOMES.contains(eventBiome) || eventBiome.contains("badlands");
        }
        if (questBiome.equals("swamps") || questBiome.equals("swamp") || questBiome.equals("marais")) {
            return SWAMP_BIOMES.contains(eventBiome) || eventBiome.contains("swamp");
        }
        if (questBiome.equals("jungles") || questBiome.equals("jungle")) {
            return JUNGLE_BIOMES.contains(eventBiome) || eventBiome.contains("jungle");
        }
        if (questBiome.equals("nether")) {
            return NETHER_BIOMES.contains(eventBiome);
        }
        if (questBiome.equals("end")) {
            return END_BIOMES.contains(eventBiome);
        }

        return false;
    }

    /**
     * Vérifie l'équivalence entre les minerais normaux et Deepslate.
     */
    public static boolean isOreMatch(@NotNull String cleanQuest, @NotNull String cleanEvent) {
        String baseQuest = cleanQuest.startsWith("deepslate_") ? cleanQuest.substring(10) : cleanQuest;
        String baseEvent = cleanEvent.startsWith("deepslate_") ? cleanEvent.substring(10) : cleanEvent;

        if (baseQuest.equals(baseEvent) && DEEPSLATE_ORES.contains(baseQuest)) {
            return true;
        }

        return false;
    }

    /**
     * Vérifie les alias communs (pluriels, cultures, types de mobs spécifiques).
     */
    public static boolean isAliasMatch(@NotNull String cleanQuest, @NotNull String cleanEvent) {
        if ((cleanQuest.equals("carrot") && cleanEvent.equals("carrots")) || (cleanQuest.equals("carrots") && cleanEvent.equals("carrot"))) return true;
        if ((cleanQuest.equals("potato") && cleanEvent.equals("potatoes")) || (cleanQuest.equals("potatoes") && cleanEvent.equals("potato"))) return true;
        if ((cleanQuest.equals("beetroot") && cleanEvent.equals("beetroots")) || (cleanQuest.equals("beetroots") && cleanEvent.equals("beetroot"))) return true;
        if ((cleanQuest.equals("nether_warts") && cleanEvent.equals("nether_wart")) || (cleanQuest.equals("nether_wart") && cleanEvent.equals("nether_warts"))) return true;
        if (cleanQuest.contains("brown_mushroom") && cleanEvent.contains("brown_mushroom")) return true;
        if (cleanQuest.contains("red_mushroom") && cleanEvent.contains("red_mushroom")) return true;
        if ((cleanQuest.equals("mooshroom") && cleanEvent.equals("mushroom_cow")) || (cleanQuest.equals("mushroom_cow") && cleanEvent.equals("mooshroom"))) return true;

        if (cleanQuest.endsWith("s") && cleanQuest.substring(0, cleanQuest.length() - 1).equals(cleanEvent)) return true;
        if (cleanEvent.endsWith("s") && cleanEvent.substring(0, cleanEvent.length() - 1).equals(cleanQuest)) return true;

        return false;
    }

    /**
     * Vérifie les groupes d'objectifs génériques (wildcards, bois, blocs, outils, minerais généraux, etc.).
     */
    public static boolean isGroupMatch(@NotNull String cleanQuest, @NotNull String cleanEvent) {
        // Wildcard universel
        if (cleanQuest.equals("blocks") || cleanQuest.equals("block") || cleanQuest.equals("any") ||
            cleanQuest.equals("any_block") || cleanQuest.equals("all") || cleanQuest.equals("building_blocks")) {
            return true;
        }

        // Groupe de minerais généraux
        if (cleanQuest.equals("ores") || cleanQuest.equals("ore") || cleanQuest.equals("any_ore")) {
            return cleanEvent.endsWith("_ore") || cleanEvent.equals("ancient_debris");
        }

        if (cleanQuest.equals("planks") || cleanQuest.equals("plank")) {
            return cleanEvent.endsWith("_planks") || cleanEvent.equals("planks");
        }
        if (cleanQuest.equals("stairs") || cleanQuest.equals("stair")) {
            return cleanEvent.endsWith("_stairs") || cleanEvent.equals("stairs");
        }
        if (cleanQuest.equals("slabs") || cleanQuest.equals("slab")) {
            return cleanEvent.endsWith("_slab") || cleanEvent.endsWith("_slabs") || cleanEvent.equals("slab");
        }
        if (cleanQuest.equals("logs") || cleanQuest.equals("log") || cleanQuest.equals("wood")) {
            return cleanEvent.endsWith("_log") || cleanEvent.endsWith("_logs") || cleanEvent.endsWith("_wood") || cleanEvent.endsWith("_hyphae") || cleanEvent.endsWith("_stem");
        }
        if (cleanQuest.equals("tools") || cleanQuest.equals("tool")) {
            return cleanEvent.endsWith("_pickaxe") || cleanEvent.endsWith("_axe") || cleanEvent.endsWith("_shovel") || cleanEvent.endsWith("_hoe") || cleanEvent.endsWith("_sword");
        }
        if (cleanQuest.equals("armor") || cleanQuest.equals("armour")) {
            return cleanEvent.endsWith("_helmet") || cleanEvent.endsWith("_chestplate") || cleanEvent.endsWith("_leggings") || cleanEvent.endsWith("_boots");
        }
        if (cleanQuest.equals("wool")) {
            return cleanEvent.endsWith("_wool") || cleanEvent.equals("wool");
        }
        if (cleanQuest.equals("beds") || cleanQuest.equals("bed")) {
            return cleanEvent.endsWith("_bed") || cleanEvent.equals("bed");
        }
        if (cleanQuest.equals("glass")) {
            return cleanEvent.endsWith("_glass") || cleanEvent.equals("glass");
        }
        if (cleanQuest.equals("glass_pane") || cleanQuest.equals("glass_panes")) {
            return cleanEvent.endsWith("_glass_pane") || cleanEvent.equals("glass_pane");
        }
        if (cleanQuest.equals("terracotta")) {
            return cleanEvent.endsWith("_terracotta") || cleanEvent.equals("terracotta");
        }
        if (cleanQuest.equals("concrete")) {
            return cleanEvent.endsWith("_concrete") || cleanEvent.equals("concrete");
        }
        if (cleanQuest.equals("candle") || cleanQuest.equals("candles")) {
            return cleanEvent.endsWith("_candle") || cleanEvent.equals("candle");
        }
        if (cleanQuest.equals("shulker_box") || cleanQuest.equals("shulker_boxes")) {
            return cleanEvent.endsWith("_shulker_box") || cleanEvent.equals("shulker_box");
        }

        return false;
    }
}
