package su.nightexpress.quests.personal;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.quests.QuestsPlugin;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import su.nightexpress.quests.config.Config;

public class PersonalQuestDefaults {

    public static void createDefaults(@NotNull QuestsPlugin plugin) {
        // 1. rpg_categories.yml
        File personalDir = new File(plugin.getDataFolder(), Config.DIR_PERSONAL);
        if (!personalDir.exists()) {
            personalDir.mkdirs();
        }
        File categoriesFile = new File(personalDir, "rpg_categories.yml");
        if (!categoriesFile.exists()) {
            try {
                FileConfig config = new FileConfig(categoriesFile);

                // Miner Category
                config.set("miner.name", "&eMineur");
                config.set("miner.type", "BREAK_BLOCK");
                config.set("miner.icon.material", "GOLDEN_PICKAXE");
                config.set("miner.icon.name", "&eMineur");
                config.set("miner.icon.lore", Arrays.asList(
                        "&7Minez des blocs pour accomplir vos tâches.",
                        "&7Niveau actuel: &e%level%",
                        "&7Quêtes terminées au niveau actuel: &a%completions%/%required_completions%"));
                config.set("miner.icon.custom_model_data", 1001);
                config.set("miner.completions_to_level_up", 5);
                config.set("miner.base_money", 100.0);
                config.set("miner.commands", Collections.singletonList("give %player% coal 16"));
                config.set("miner.objectives.COAL_ORE", 10);
                config.set("miner.objectives.IRON_ORE", 5);
                config.set("miner.objectives.DIAMOND_ORE", 1);

                // Hunter Category
                config.set("hunter.name", "&cChasseur");
                config.set("hunter.type", "KILL_MOB");
                config.set("hunter.icon.material", "IRON_SWORD");
                config.set("hunter.icon.name", "&cChasseur");
                config.set("hunter.icon.lore", Arrays.asList(
                        "&7Tuez des monstres pour accomplir vos tâches.",
                        "&7Niveau actuel: &c%level%",
                        "&7Quêtes terminées au niveau actuel: &a%completions%/%required_completions%"));
                config.set("hunter.icon.custom_model_data", 1002);
                config.set("hunter.completions_to_level_up", 5);
                config.set("hunter.base_money", 120.0);
                config.set("hunter.commands", Collections.singletonList("give %player% arrow 16"));
                config.set("hunter.objectives.ZOMBIE", 5);
                config.set("hunter.objectives.SKELETON", 5);
                config.set("hunter.objectives.CREEPER", 3);

                config.save();
            } catch (Exception e) {
                plugin.error("Failed to generate rpg_categories.yml: " + e.getMessage());
            }
        }

        // Create menu directory if not exist
        File menuDir = new File(plugin.getDataFolder(), Config.DIR_MENU_PERSONAL);
        if (!menuDir.exists()) {
            menuDir.mkdirs();
        }

        // 2. menu/personal.yml
        File personalMenuFile = new File(menuDir, "personal.yml");
        if (!personalMenuFile.exists()) {
            try {
                FileConfig config = new FileConfig(personalMenuFile);
                config.set("Settings.MenuType", "minecraft:generic_9x5");
                config.set("Settings.Title", "Quêtes RPG");
                config.set("Settings.Auto_Refresh", 1);
                config.set("Settings.PlaceholderAPI.Enabled", false);

                config.set("Quest.SlotsByCount.1", "22");
                config.set("Quest.SlotsByCount.2", "21,23");
                config.set("Quest.SlotsByCount.3", "21,22,23");
                config.set("Quest.SlotsByCount.4", "21,22,24,25");
                config.set("Quest.SlotsByCount.5", "20,21,22,23,24");

                // Content
                config.set("Content.back.Priority", 10);
                config.set("Content.back.Slots", "40");
                config.set("Content.back.Item.Material", "PLAYER_HEAD");
                config.set("Content.back.Item.Skull-Texture", "%player%");
                config.set("Content.back.Item.Display_Name", "<#ffeea2><b>Profile");
                config.set("Content.back.Item.Lore",
                        Collections.singletonList("<#d4d9d8>Cliquez pour retourner à votre profil."));
                config.set("Content.back.Item.Hide_Components", true);

                config.set("Content.black_stained_glass_pane.Priority", -1);
                config.set("Content.black_stained_glass_pane.Slots", "0,1,2,3,4,5,6,7,8,36,37,38,39,41,42,43,44");
                config.set("Content.black_stained_glass_pane.Item.Material", "minecraft:black_stained_glass_pane");
                config.set("Content.black_stained_glass_pane.Item.Hide_Tooltip", true);

                config.set("Content.gray_stained_glass_pane.Priority", -1);
                config.set("Content.gray_stained_glass_pane.Slots",
                        "9,10,11,12,13,14,15,16,17,18,19,25,26,27,28,29,30,31,32,33,34,35");
                config.set("Content.gray_stained_glass_pane.Item.Material", "minecraft:gray_stained_glass_pane");
                config.set("Content.gray_stained_glass_pane.Item.Hide_Tooltip", true);

                config.save();
            } catch (Exception e) {
                plugin.error("Failed to generate menu/personal.yml: " + e.getMessage());
            }
        }

        // 3. menu/personal_categories.yml
        File categoriesMenuFile = new File(menuDir, "personal_categories.yml");
        if (!categoriesMenuFile.exists()) {
            try {
                FileConfig config = new FileConfig(categoriesMenuFile);
                config.set("Settings.MenuType", "minecraft:generic_9x5");
                config.set("Settings.Title", "Objectifs: %category_name%");
                config.set("Settings.Auto_Refresh", 1);
                config.set("Quest.SlotsByCount.1", "22");
                config.set("Quest.SlotsByCount.2", "21,23");
                config.set("Quest.SlotsByCount.3", "21,22,23");
                config.set("Quest.SlotsByCount.4", "21,22,24,25");
                config.set("Quest.SlotsByCount.5", "20,21,22,23,24");

                config.set("Quest.Item.Display_Name", "&d%material_name%");
                config.set("Quest.Item.Lore", Collections.singletonList("Quantité requise: %material_quantite%"));

                // Content
                config.set("Content.back.Priority", 10);
                config.set("Content.back.Slots", "40");
                config.set("Content.back.Item.Material", "PLAYER_HEAD");
                config.set("Content.back.Item.Skull-Texture", "%player%");
                config.set("Content.back.Item.Display_Name", "<#ffeea2><b>Retour");
                config.set("Content.back.Item.Lore",
                        Collections.singletonList("<#d4d9d8>Cliquez pour retourner aux catégories."));
                config.set("Content.back.Item.Hide_Components", true);

                config.set("Content.black_stained_glass_pane.Priority", -1);
                config.set("Content.black_stained_glass_pane.Slots", "0,1,2,3,4,5,6,7,8,36,37,38,39,41,42,43,44");
                config.set("Content.black_stained_glass_pane.Item.Material", "minecraft:black_stained_glass_pane");
                config.set("Content.black_stained_glass_pane.Item.Hide_Tooltip", true);

                config.set("Content.gray_stained_glass_pane.Priority", -1);
                config.set("Content.gray_stained_glass_pane.Slots",
                        "9,10,11,12,13,14,15,16,17,18,19,25,26,27,28,29,30,31,32,33,34,35");
                config.set("Content.gray_stained_glass_pane.Item.Material", "minecraft:gray_stained_glass_pane");
                config.set("Content.gray_stained_glass_pane.Item.Hide_Tooltip", true);

                config.save();
            } catch (Exception e) {
                plugin.error("Failed to generate menu/personal_categories.yml: " + e.getMessage());
            }
        }
    }
}
