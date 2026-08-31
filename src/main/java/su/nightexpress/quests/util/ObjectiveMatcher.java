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
     * @param questObjective L'objectif défini dans la quête (ex: IRON_ORE, minecraft:deepslate_iron_ore, carrots, planks)
     * @param eventObjective L'identifiant issu de l'événement en jeu (ex: minecraft:deepslate_iron_ore, OAK_PLANKS)
     * @return true si l'événement valide l'objectif
     */
    public static boolean isMatch(@Nullable String questObjective, @Nullable String eventObjective) {
        if (questObjective == null || eventObjective == null) return false;
        if (questObjective.equalsIgnoreCase(eventObjective)) return true;

        String cleanQuest = normalize(questObjective);
        String cleanEvent = normalize(eventObjective);

        if (cleanQuest.isEmpty() || cleanEvent.isEmpty()) return false;
        if (cleanQuest.equals(cleanEvent)) return true;

        // 1. Équivalence des minerais (Deepslate <-> Normal)
        if (isOreMatch(cleanQuest, cleanEvent)) {
            return true;
        }

        // 2. Alias spécifiques (cultures, entités, pluriels singuliers)
        if (isAliasMatch(cleanQuest, cleanEvent)) {
            return true;
        }

        // 3. Groupes d'objectifs génériques (planks, logs, tools, etc.)
        if (isGroupMatch(cleanQuest, cleanEvent)) {
            return true;
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
