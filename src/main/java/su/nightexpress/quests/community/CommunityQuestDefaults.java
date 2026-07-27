package su.nightexpress.quests.community;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;

import java.io.File;
import java.io.FileWriter;

public class CommunityQuestDefaults {

    public static void createDefaults(@NotNull QuestsPlugin plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        File questsFile = new File(dataFolder, "community_quests.yml");
        if (!questsFile.exists()) {
            try (FileWriter writer = new FileWriter(questsFile)) {
                writer.write(getQuestsYamlContent());
            } catch (Exception e) {
                plugin.error("Failed to create default community_quests.yml: " + e.getMessage());
            }
        }

        File menuFolder = new File(dataFolder, "menu");
        if (!menuFolder.exists()) {
            menuFolder.mkdirs();
        }

        File menuFile = new File(menuFolder, "community_leaderboard.yml");
        if (!menuFile.exists()) {
            try (FileWriter writer = new FileWriter(menuFile)) {
                writer.write(getLeaderboardMenuYamlContent());
            } catch (Exception e) {
                plugin.error("Failed to create default menu/community_leaderboard.yml: " + e.getMessage());
            }
        }
    }

    public static String getQuestsYamlContent() {
        return """
            quests:
              lumens_deposit:
                display_name: "&6&lDépôt de Lumens Communautaire"
                description:
                  - "&7Déposez vos Lumens pour atteindre"
                  - "&7l'objectif commun et débloquer des récompenses !"
                type: "MONEY_DEPOSIT"
                default_target: 1000000.0
                default_duration_hours: 48
                icon: "GOLD_INGOT"
                custom_model_data: 0
                global_rewards:
                  - "broadcast &a[Événement] Objectif de Lumens atteint par la communauté !"
                ranking_rewards:
                  top_1:
                    - "broadcast &6%player% a fini #1 du classement communautaire !"
                  top_2:
                    - "broadcast &e%player% a fini #2 du classement communautaire !"
                  top_3:
                    - "broadcast &c%player% a fini #3 du classement communautaire !"
                  participation_min_contribution: 10000.0
                  participation:
                    - "broadcast &aMerci à %player% pour sa participation !"

              blocks_break:
                display_name: "&a&lCasse de Minerais Globale"
                description:
                  - "&7Cassez tous ensemble des minerais"
                  - "&7pour compléter l'événement mondial !"
                type: "BLOCK_BREAK_GLOBAL"
                default_target: 50000.0
                default_duration_hours: 24
                icon: "DIAMOND_PICKAXE"
                custom_model_data: 0
                global_rewards:
                  - "broadcast &a[Événement] 50,000 blocs ont été cassés !"
                ranking_rewards:
                  top_1:
                    - "give %player% diamond 64"
                  top_2:
                    - "give %player% diamond 32"
                  top_3:
                    - "give %player% diamond 16"
                  participation_min_contribution: 100.0
                  participation:
                    - "give %player% iron_ingot 32"

              mobs_kill:
                display_name: "&c&lChasse aux Monstres Globale"
                description:
                  - "&7Éliminez collectivement les monstres"
                  - "&7du monde pour sauver le royaume !"
                type: "KILL_MOB_GLOBAL"
                default_target: 10000.0
                default_duration_hours: 24
                icon: "NETHERITE_SWORD"
                custom_model_data: 0
                global_rewards:
                  - "broadcast &c[Événement] La menace monstrueuse a été éradiquée !"
                ranking_rewards:
                  top_1:
                    - "exp give %player% 5000"
                  top_2:
                    - "exp give %player% 2500"
                  top_3:
                    - "exp give %player% 1000"
                  participation_min_contribution: 50.0
                  participation:
                    - "exp give %player% 250"
            """;
    }

    public static String getLeaderboardMenuYamlContent() {
        return """
            Settings:
              Title: "&8&lQuêtes Communautaires - Leaderboard"

            Decorations:
              border_dark:
                material: "BLACK_STAINED_GLASS_PANE"
                slots: "0,1,2,3,5,6,7,8,9,17,18,26,27,35,36,44,45,46,47,48,50,51,52,53"
              border_gray:
                material: "GRAY_STAINED_GLASS_PANE"
                slots: "11,15,20,24,29,33,38,42"

            Progress_Item:
              slot: 4
              Item:
                Material: "NETHER_STAR"
                Display_Name: "&6&lÉvénement Actif: &e%quest_name%"
                Lore:
                  - "&7%quest_description%"
                  - ""
                  - "&e&lProgression Globale:"
                  - "&7- Avancement: &a%current% &7/ &a%target%"
                  - "&7- Pourcentage: &e%percent%%"
                  - "&7- Barre: %progress_bar%"
                  - ""
                  - "&c&lTemps Restant: &f%time_left%"
                  - "&7- Début: &f%start_date%"
                  - "&7- Fin: &f%end_date%"

            Contribute_Button:
              slot: 49
              Item:
                Material: "GOLD_NUGGET"
                Display_Name: "&a&lFaire une Contribution"
                Lore:
                  - "&7Contribuez au succès de la communauté !"
                  - ""
                  - "&eClic Gauche: &fDéposer 10 000 Lumens"
                  - "&eClic Droit: &fDéposer 100 000 Lumens"
                  - "&eShift + Clic Droit: &fDéposer le solde maximum"
                  - ""
                  - "&7Votre contribution actuelle: &a%your_contribution% Lumens"

            Leaderboard_Slots: "12,13,14,21,22,23,30,31,32,40"

            Leaderboard_Item:
              Item:
                Material: "PLAYER_HEAD"
                Display_Name: "&6#%rank% &e%player%"
                Lore:
                  - "&7Contribution: &a%amount%"
                  - "&7Part du total: &e%share%%"

            Empty_Leaderboard_Item:
              Item:
                Material: "BARRIER"
                Display_Name: "&7#%rank% &cEmplacement Vide"
                Lore:
                  - "&7Aucune contribution enregistrée pour ce rang."
            """;
    }
}
