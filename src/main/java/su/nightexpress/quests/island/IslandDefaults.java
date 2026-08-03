package su.nightexpress.quests.island;

import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;

import java.io.File;
import java.util.Arrays;

public class IslandDefaults {

    public static void setupDefaults(QuestsPlugin plugin) {
        File dataFolder = plugin.getDataFolder();
        File islandDir = new File(dataFolder, Config.DIR_ISLAND);
        if (!islandDir.exists()) {
            islandDir.mkdirs();
        }

        File resourceGroupsFile = new File(islandDir, "resource_groups.yml");
        if (!resourceGroupsFile.exists()) {
            createResourceGroupsDefault(resourceGroupsFile);
        }

        File islandQuestsFile = new File(islandDir, "island_quests.yml");
        if (!islandQuestsFile.exists()) {
            createIslandQuestsDefault(islandQuestsFile);
        }

        File menuFile = new File(dataFolder, Config.DIR_MENU_ISLAND + "island_quests.yml");
        if (!menuFile.exists()) {
            createMenuDefault(menuFile);
        }

        File resourceGroupsMenuFile = new File(dataFolder, Config.DIR_MENU_ISLAND + "resource_groups.yml");
        if (!resourceGroupsMenuFile.exists()) {
            createResourceGroupsMenuDefault(resourceGroupsMenuFile);
        }
    }

    private static void createResourceGroupsDefault(File file) {
        try {
            FileConfig config = new FileConfig(file);
            config.set("resource_groups.minerals.name", "Minerais");
            config.set("resource_groups.minerals.materials.DIAMOND", 10.0);
            config.set("resource_groups.minerals.materials.GOLD_INGOT", 2.0);
            config.set("resource_groups.minerals.materials.IRON_INGOT", 1.0);
            config.set("resource_groups.minerals.materials.COAL", 0.5);

            config.set("resource_groups.farming.name", "Agriculture");
            config.set("resource_groups.farming.materials.WHEAT", 1.0);
            config.set("resource_groups.farming.materials.CARROT", 1.0);
            config.set("resource_groups.farming.materials.POTATO", 1.0);
            config.set("resource_groups.farming.materials.PUMPKIN", 2.0);

            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createIslandQuestsDefault(File file) {
        try {
            FileConfig config = new FileConfig(file);
            config.set("quests.level_1.name", "Niveau 1: Le Commencement");
            config.set("quests.level_1.order", 1);
            config.set("quests.level_1.description", Arrays.asList(
                "&7Récompenses au passage du niveau 1:",
                "&e- 5x Diamants",
                "&e- 1,000 $"
            ));
            config.set("quests.level_1.requirements.iron_deposit.name", "Dépôt de Fer");
            config.set("quests.level_1.requirements.iron_deposit.resource_group", "minerals");
            config.set("quests.level_1.requirements.iron_deposit.target", 1000);
            config.set("quests.level_1.requirements.wheat_deposit.name", "Dépôt de Blé");
            config.set("quests.level_1.requirements.wheat_deposit.resource_group", "farming");
            config.set("quests.level_1.requirements.wheat_deposit.target", 2000);
            config.set("quests.level_1.rewards", Arrays.asList(
                "give %player% diamond 5",
                "eco give %player% 1000"
            ));

            config.set("quests.level_2.name", "Niveau 2: L'Expansion");
            config.set("quests.level_2.order", 2);
            config.set("quests.level_2.description", Arrays.asList(
                "&7Récompenses au passage du niveau 2:",
                "&e- 15x Diamants",
                "&e- 5,000 $"
            ));
            config.set("quests.level_2.requirements.gold_deposit.name", "Dépôt d'Or");
            config.set("quests.level_2.requirements.gold_deposit.resource_group", "minerals");
            config.set("quests.level_2.requirements.gold_deposit.target", 5000);
            config.set("quests.level_2.rewards", Arrays.asList(
                "give %player% diamond 15",
                "eco give %player% 5000"
            ));

            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createMenuDefault(File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileConfig config = new FileConfig(file);
            config.set("Settings.Title", "Quêtes d'Île Coopératives");
            config.set("Settings.Size", 45);

            config.set("Decorations.black_glass.material", "BLACK_STAINED_GLASS_PANE");
            config.set("Decorations.black_glass.slots", "0,1,2,3,5,6,7,8,36,37,38,39,41,42,43,44");

            config.set("Decorations.gray_glass.material", "GRAY_STAINED_GLASS_PANE");
            config.set("Decorations.gray_glass.slots", "9,10,11,12,13,14,15,16,17,18,19,25,26,27,28,29,30,31,32,33,34,35");

            config.set("Requirement_Slots", "20,21,22,23,24");

            config.set("Requirement_Item.Item.Material", "CHEST");
            config.set("Requirement_Item.Item.Display_Name", "&d%requirement_name%");
            config.set("Requirement_Item.Item.Lore", Arrays.asList(
                "&7Groupe: &f%resource_group_name%",
                "&7Progression: &e%progress% / %target% &7(%percent%%)",
                "%progress_bar%",
                "",
                "&aClic Gauche: &7Déposer l'item tenu (1 stack)",
                "&aClic Droit: &7Déposer tout l'inventaire"
            ));

            config.set("Level_Info_Item.slot", 4);
            config.set("Level_Info_Item.Item.Material", "BOOK");
            config.set("Level_Info_Item.Item.Display_Name", "&6Quête Actuelle: &e%level_name%");
            config.set("Level_Info_Item.Item.Lore", Arrays.asList(
                "&7Progression globale de l'île",
                "&7Complétez tous les dépôts ci-dessous pour passer",
                "&7au niveau supérieur.",
                "",
                "%level_description%"
            ));

            config.set("Resource_Group_Info_Item.slot", 40);
            config.set("Resource_Group_Info_Item.Item.Material", "PAPER");
            config.set("Resource_Group_Info_Item.Item.Display_Name", "&ePoids des Ressources");
            config.set("Resource_Group_Info_Item.Item.Lore", Arrays.asList(
                "&7Cliquez pour consulter le taux de conversion",
                "&7et la valeur en points de chaque matériau."
            ));

            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createResourceGroupsMenuDefault(File file) {
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            FileConfig config = new FileConfig(file);
            config.set("Settings.Title", "Poids des Ressources");
            config.set("Settings.Size", 45);

            config.set("Group_Slots", "10,11,12,13,14,15,16,19,20,21,22,23,24,25");

            config.set("Group_Item.Item.Material", "GOLD_NUGGET");
            config.set("Group_Item.Item.Display_Name", "&eGroupe: &f%group_name%");
            config.set("Group_Item.Item.Lore", Arrays.asList(
                "&7Valeur des matériaux:",
                "%weight_lore%"
            ));

            config.set("Return_Item.slot", 40);
            config.set("Return_Item.Item.Material", "ARROW");
            config.set("Return_Item.Item.Display_Name", "&cRetour");
            config.set("Return_Item.Item.Lore", Arrays.asList(
                "&7Cliquez pour revenir au menu des quêtes d'île."
            ));

            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
