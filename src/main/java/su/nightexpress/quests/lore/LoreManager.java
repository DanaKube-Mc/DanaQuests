package su.nightexpress.quests.lore;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.FileUtil;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.lore.data.LoreQuestData;
import su.nightexpress.quests.lore.definition.LoreObjective;
import su.nightexpress.quests.lore.definition.LoreQuest;
import su.nightexpress.quests.lore.definition.LoreQuestCategory;
import su.nightexpress.quests.lore.menu.LoreMenu;
import su.nightexpress.quests.lore.menu.LoreCategoriesMenu;
import su.nightexpress.quests.lore.menu.LoreProgressionMenu;
import su.nightexpress.quests.lore.listener.LoreGenericListener;
import su.nightexpress.quests.tracker.QuestTrackerManager;
import su.nightexpress.quests.user.QuestUser;

import java.io.File;
import java.util.*;

import static su.nightexpress.quests.config.Config.*;

public class LoreManager extends AbstractManager<QuestsPlugin> {

    private final Map<String, LoreQuestCategory> categories;
    private final Map<String, LoreQuest> quests;
    private final String dirPath;
    private LoreCategoriesMenu categoriesMenu;
    private LoreMenu loreMenu;
    private LoreProgressionMenu progressionMenu;

    public LoreManager(@NotNull QuestsPlugin plugin) {
        super(plugin);
        this.categories = new LinkedHashMap<>();
        this.quests = new HashMap<>();
        this.dirPath = this.plugin.getDataFolder() + DIR_LORE;
    }

    @Override
    protected void onLoad() {
        this.loadLoreQuests();
        this.categoriesMenu = this.addMenu(new LoreCategoriesMenu(this.plugin, this), DIR_MENU_LORE, "lore_categories.yml");
        this.loreMenu = this.addMenu(new LoreMenu(this.plugin, this), DIR_MENU_LORE, "lore.yml");
        this.progressionMenu = this.addMenu(new LoreProgressionMenu(this.plugin, this), DIR_MENU_LORE, "lore_progression.yml");
        
        // Register events
        Bukkit.getPluginManager().registerEvents(new LoreGenericListener(this.plugin, this), this.plugin);
    }

    @Override
    protected void onShutdown() {
        this.categories.clear();
        this.quests.clear();
        this.categoriesMenu = null;
        this.loreMenu = null;
        this.progressionMenu = null;
    }

    public void loadLoreQuests() {
        this.categories.clear();
        this.quests.clear();

        File dir = new File(this.dirPath);
        if (!dir.exists()) {
            dir.mkdirs();
            LoreDefaults.createDefaultDemoCategory(this.dirPath);
        } else {
            File[] files = dir.listFiles();
            if (files == null || files.length == 0) {
                LoreDefaults.createDefaultDemoCategory(this.dirPath);
            }
        }

        FileUtil.getConfigFiles(this.dirPath).forEach(file -> {
            try {
                FileConfig config = new FileConfig(file);
                config.load();

                String catId = config.getString("id", file.getName().replace(".yml", ""));
                String catName = config.getString("category-name", config.getString("name", catId));
                List<String> catDesc = config.getStringList("description");
                String iconMat = config.getString("icon.material", "BOOK");
                int iconCustomModelData = config.getInt("icon.custom_model_data", 0);
                
                List<String> neededCats = new ArrayList<>();
                if (config.contains("prerequisite")) {
                    if (config.isList("prerequisite")) {
                        neededCats.addAll(config.getStringList("prerequisite"));
                    } else {
                        String prereq = config.getString("prerequisite", "");
                        if (!prereq.trim().isEmpty()) {
                            neededCats.add(prereq.trim());
                        }
                    }
                } else {
                    neededCats.addAll(config.getStringList("needed_completed_categories"));
                }

                String activeMat = config.getString("icon.active.material", iconMat);
                String activeName = config.getString("icon.active.name", null);
                List<String> activeLore = config.contains("icon.active.lore") ? config.getStringList("icon.active.lore") : null;
                int activeCmd = config.getInt("icon.active.custom_model_data", iconCustomModelData);

                String inactiveMat = config.getString("icon.inactive.material", iconMat);
                String inactiveName = config.getString("icon.inactive.name", null);
                List<String> inactiveLore = config.contains("icon.inactive.lore") ? config.getStringList("icon.inactive.lore") : null;
                int inactiveCmd = config.getInt("icon.inactive.custom_model_data", iconCustomModelData);

                String finishedMat = config.getString("icon.finished.material", iconMat);
                String finishedName = config.getString("icon.finished.name", null);
                List<String> finishedLore = config.contains("icon.finished.lore") ? config.getStringList("icon.finished.lore") : null;
                int finishedCmd = config.getInt("icon.finished.custom_model_data", iconCustomModelData);

                List<LoreQuest> catQuests = new ArrayList<>();

                if (config.contains("quests")) {
                    for (String qId : config.getConfigurationSection("quests").getKeys(false)) {
                        String qPath = "quests." + qId;
                        String qName = config.getString(qPath + ".name", qId);
                        List<String> qDesc = config.getStringList(qPath + ".description");
                        List<LoreObjective> qObjs = new ArrayList<>();

                        if (config.contains(qPath + ".objectives")) {
                            for (String oId : config.getConfigurationSection(qPath + ".objectives").getKeys(false)) {
                                String oPath = qPath + ".objectives." + oId;
                                String oType = config.getString(oPath + ".type", "talk_to_npc");
                                String oTarget = config.getString(oPath + ".target", "");
                                int oRequired = config.getInt(oPath + ".required", 1);
                                String oDesc = config.getString(oPath + ".description", "");

                                qObjs.add(new LoreObjective(oId, oType, oTarget, oRequired, oDesc));
                            }
                        }

                        List<String> qRewards = config.getStringList(qPath + ".rewards");
                        String qSound = config.getString(qPath + ".completion.sound");
                        String qTitle = config.getString(qPath + ".completion.title");
                        String qSubtitle = config.getString(qPath + ".completion.subtitle");

                        String qIconMat = config.getString(qPath + ".icon.material", "CHEST");
                        List<String> qIconLore = config.getStringList(qPath + ".icon.lore");
                        int qIconCmd = config.getInt(qPath + ".icon.custom_model_data", 0);

                        LoreQuest quest = new LoreQuest(qId, qName, qDesc, qObjs, qRewards, qSound, qTitle, qSubtitle, catId, qIconMat, qIconLore, qIconCmd);
                        catQuests.add(quest);
                        this.quests.put(qId, quest);
                    }
                }

                LoreQuestCategory category = new LoreQuestCategory(
                    catId, catName, catDesc, iconMat, iconCustomModelData, neededCats, catQuests,
                    activeMat, activeName, activeLore, activeCmd,
                    inactiveMat, inactiveName, inactiveLore, inactiveCmd,
                    finishedMat, finishedName, finishedLore, finishedCmd
                );
                this.categories.put(catId, category);
            } catch (Exception e) {
                this.plugin.error("Failed to load lore category from " + file.getName() + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        this.plugin.info("Loaded " + this.categories.size() + " lore categories and " + this.quests.size() + " lore quests.");
    }

    @NotNull
    public Map<String, LoreQuestCategory> getCategories() {
        return this.categories;
    }

    @Nullable
    public LoreQuestCategory getCategory(@NotNull String id) {
        return this.categories.get(id);
    }

    @Nullable
    public LoreQuest getQuest(@NotNull String id) {
        return this.quests.get(id);
    }

    public List<String> getActiveQuestTargets(@NotNull Player player) {
        List<String> targets = new ArrayList<>();
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        for (LoreQuestCategory category : this.categories.values()) {
            if (isCategoryUnlocked(user, category)) {
                LoreQuest activeQuest = getActiveQuest(user, category);
                if (activeQuest != null) {
                    for (LoreObjective obj : activeQuest.getObjectives()) {
                        targets.add(obj.getTarget());
                    }
                }
            }
        }
        return targets;
    }

    public boolean isCategoryUnlocked(@NotNull QuestUser user, @NotNull LoreQuestCategory category) {
        for (String needed : category.getNeededCompletedCategories()) {
            if (!user.getCompletedLoreQuests().contains("cat:" + needed)) {
                return false;
            }
        }
        return true;
    }

    public boolean isCategoryCompleted(@NotNull QuestUser user, @NotNull LoreQuestCategory category) {
        if (category.getQuests().isEmpty()) return false;
        for (LoreQuest quest : category.getQuests()) {
            if (!user.hasCompletedLore(quest.getId())) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public LoreQuest getActiveQuest(@NotNull QuestUser user, @NotNull LoreQuestCategory category) {
        for (LoreQuest quest : category.getQuests()) {
            if (!user.hasCompletedLore(quest.getId())) {
                return quest;
            }
        }
        return null;
    }

    public int getObjectiveProgress(@NotNull QuestUser user, @NotNull LoreQuest quest, @NotNull LoreObjective objective) {
        Map<String, LoreQuestData> allProgress = user.getLoreQuestsProgress();
        LoreQuestData questProgress = allProgress.get(quest.getId());
        if (questProgress == null) return 0;
        return questProgress.getProgress(objective.getId());
    }

    public void progressLoreQuests(@NotNull Player player, @NotNull String taskType, @NotNull String target, int amount) {
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        boolean changed = false;

        for (LoreQuestCategory category : this.categories.values()) {
            if (!isCategoryUnlocked(user, category) || isCategoryCompleted(user, category)) {
                continue;
            }

            LoreQuest activeQuest = getActiveQuest(user, category);
            if (activeQuest == null) continue;

            boolean questProgressed = false;
            Map<String, LoreQuestData> allProgress = user.getLoreQuestsProgress();
            LoreQuestData questProgress = allProgress.computeIfAbsent(activeQuest.getId(), k -> new LoreQuestData(activeQuest.getId()));

            for (LoreObjective objective : activeQuest.getObjectives()) {
                if (objective.getTaskType().equalsIgnoreCase(taskType) && objective.getTarget().equalsIgnoreCase(target)) {
                    int current = questProgress.getProgress(objective.getId());
                    if (current < objective.getRequired()) {
                        int newValue = Math.min(objective.getRequired(), current + amount);
                        questProgress.setProgress(objective.getId(), newValue);
                        questProgressed = true;
                        changed = true;
                    }
                }
            }

            if (questProgressed) {
                boolean allCompleted = true;
                for (LoreObjective objective : activeQuest.getObjectives()) {
                    int current = questProgress.getProgress(objective.getId());
                    if (current < objective.getRequired()) {
                        allCompleted = false;
                        break;
                    }
                }

                if (allCompleted) {
                    user.completeLoreQuest(activeQuest.getId());
                    allProgress.remove(activeQuest.getId());
                    QuestTrackerManager.forceCleanupCategory(player, category.getId());

                    for (String rewardCmd : activeQuest.getRewards()) {
                        String cmd = rewardCmd.replace("%player%", player.getName());
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }

                    if (activeQuest.getCompletionSound() != null && !activeQuest.getCompletionSound().isEmpty()) {
                        try {
                            player.playSound(player.getLocation(), Sound.valueOf(activeQuest.getCompletionSound().toUpperCase().replace(".", "_")), 1.0f, 1.0f);
                        } catch (Exception ignored) {
                            try {
                                player.playSound(player.getLocation(), activeQuest.getCompletionSound(), 1.0f, 1.0f);
                            } catch (Exception ignored2) {}
                        }
                    }

                    if (activeQuest.getCompletionTitle() != null || activeQuest.getCompletionSubtitle() != null) {
                        String title = activeQuest.getCompletionTitle() != null ? activeQuest.getCompletionTitle() : "";
                        String subtitle = activeQuest.getCompletionSubtitle() != null ? activeQuest.getCompletionSubtitle() : "";
                        player.sendTitle(title, subtitle, 10, 70, 20);
                    }

                    if (isCategoryCompleted(user, category)) {
                        user.completeLoreQuest("cat:" + category.getId());
                    }
                } else {
                    int totalProgress = 0;
                    int totalRequired = 0;
                    for (LoreObjective obj : activeQuest.getObjectives()) {
                        totalProgress += questProgress.getProgress(obj.getId());
                        totalRequired += obj.getRequired();
                    }
                    QuestTrackerManager.showLoreProgress(player, activeQuest, category, totalProgress, totalRequired);
                }
            }
        }

        if (changed) {
            this.plugin.getUserManager().save(user);
        }
    }

    public void openLoreMenu(@NotNull Player player) {
        if (this.loreMenu != null) {
            this.loreMenu.open(player);
        }
    }

    public void openCategories(@NotNull Player player) {
        if (this.categoriesMenu != null) {
            this.categoriesMenu.open(player);
        }
    }

    public void openProgression(@NotNull Player player, @NotNull LoreQuestCategory category) {
        if (this.progressionMenu != null) {
            this.progressionMenu.open(player, category);
        }
    }
}
