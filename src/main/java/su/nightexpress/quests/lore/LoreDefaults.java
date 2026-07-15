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
            config.set("name", "Chapitre I: Le commencement");
            config.set("description", Arrays.asList("Débutez votre aventure", "Suivez l'histoire principale"));
            config.set("icon.material", "BOOK");
            config.set("icon.custom_model_data", 1001);
            config.set("needed_completed_categories", new ArrayList<String>());

            config.set("quests.lore_quest_1.name", "La rencontre");
            config.set("quests.lore_quest_1.description", Arrays.asList("Allez voir le Maire au village"));
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
