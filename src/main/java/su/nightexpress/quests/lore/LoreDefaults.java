package su.nightexpress.quests.lore;

import su.nightexpress.nightcore.config.FileConfig;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class LoreDefaults {

    public static void createDefaultDemoCategory(String dirPath) {
        File file = new File(dirPath, "chapitre1.yml");
        try {
            FileConfig config = new FileConfig(file);
            config.set("id", "chapitre1");
            config.set("category-name", "Chapitre I: Le commencement");
            config.set("prerequisite", "");

            config.set("icon.active.material", "WRITABLE_BOOK");
            config.set("icon.active.name", "&9Chapitre I: Le commencement");
            config.set("icon.active.lore", Arrays.asList("&7Débutez votre aventure", "&7Suivez l'histoire principale"));
            config.set("icon.active.custom_model_data", 1001);

            config.set("icon.inactive.material", "WRITTEN_BOOK");
            config.set("icon.inactive.name", "&c[Verrouillé] &7Chapitre I: Le commencement");
            config.set("icon.inactive.lore", Arrays.asList("&7Complétez les chapitres précédents."));
            config.set("icon.inactive.custom_model_data", 1002);

            config.set("icon.finished.material", "KNOWLEDGE_BOOK");
            config.set("icon.finished.name", "&a[Complété] &fChapitre I: Le commencement");
            config.set("icon.finished.lore", Arrays.asList("&7Chapitre terminé !"));
            config.set("icon.finished.custom_model_data", 1003);

            config.set("quests.lore_quest_1.name", "La rencontre");
            config.set("quests.lore_quest_1.description", Arrays.asList("Allez voir le Maire au village"));

            config.set("quests.lore_quest_1.icon.material", "CHEST");
            config.set("quests.lore_quest_1.icon.lore", Arrays.asList("&7Statut : %status%"));

            config.set("quests.lore_quest_1.objectives.obj_1.type", "talk_to_npc");
            config.set("quests.lore_quest_1.objectives.obj_1.target", "citizens:12");
            config.set("quests.lore_quest_1.objectives.obj_1.required", 1);
            config.set("quests.lore_quest_1.objectives.obj_1.description", "Parler au Maire");
            config.set("quests.lore_quest_1.rewards", Arrays.asList("give %player% diamond 1"));
            config.set("quests.lore_quest_1.completion.sound", "entity.player.levelup");
            config.set("quests.lore_quest_1.completion.title", "Quête narrative complétée !");
            config.set("quests.lore_quest_1.completion.subtitle", "La rencontre");

            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
