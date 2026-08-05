package su.nightexpress.quests.personal;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;
import su.nightexpress.quests.personal.data.PersonalQuestData;
import su.nightexpress.quests.personal.definition.RpgCategory;
import su.nightexpress.quests.personal.menu.PersonalCategoriesMenu;
import su.nightexpress.quests.personal.menu.PersonalQuestMenu;
import su.nightexpress.quests.user.QuestUser;
import su.nightexpress.quests.util.MenuUtils;

import java.io.File;
import java.util.*;

public class PersonalQuestManager extends AbstractManager<QuestsPlugin> {

    private final Map<String, RpgCategory> categories;
    private final String configPath;

    private PersonalQuestMenu personalMenu;
    private PersonalCategoriesMenu categoriesMenu;

    public PersonalQuestManager(@NotNull QuestsPlugin plugin) {
        super(plugin);
        this.categories = new LinkedHashMap<>();
        this.configPath = this.plugin.getDataFolder() + Config.DIR_PERSONAL + "rpg_categories.yml";
    }

    @Override
    protected void onLoad() {
        PersonalQuestDefaults.createDefaults(this.plugin);
        this.loadCategories();

        this.personalMenu = this.addMenu(new PersonalQuestMenu(this.plugin, this), Config.DIR_MENU_PERSONAL, "personal.yml");
        this.categoriesMenu = this.addMenu(new PersonalCategoriesMenu(this.plugin, this), Config.DIR_MENU_PERSONAL, "personal_categories.yml");
    }

    @Override
    protected void onShutdown() {
        this.categories.clear();
        this.personalMenu = null;
        this.categoriesMenu = null;
    }

    public void loadCategories() {
        this.categories.clear();
        File file = new File(this.configPath);
        if (!file.exists()) {
            return;
        }

        try {
            FileConfig config = new FileConfig(file);
            config.load();

            for (String catId : config.getKeys(false)) {
                String name = config.getString(catId + ".name", catId);
                String type = config.getString(catId + ".type", "BREAK_BLOCK");
                String material = config.getString(catId + ".icon.material", "BOOK");
                String iconName = config.getString(catId + ".icon.name", name);
                List<String> iconLore = config.getStringList(catId + ".icon.lore");
                int customModelData = config.getInt(catId + ".icon.custom_model_data", 0);
                int completionsToLevelUp = config.getInt(catId + ".completions_to_level_up", 5);
                double baseMoney = config.getDouble(catId + ".base_money", 100.0);
                List<String> commands = config.getStringList(catId + ".commands");

                Map<String, Integer> objectives = new LinkedHashMap<>();
                if (config.contains(catId + ".objectives")) {
                    for (String objKey : config.getSection(catId + ".objectives")) {
                        int baseAmount = config.getInt(catId + ".objectives." + objKey, 1);
                        objectives.put(objKey.toUpperCase(), baseAmount);
                    }
                }

                RpgCategory category = new RpgCategory(
                    catId, name, type, material, iconName, iconLore, customModelData,
                    completionsToLevelUp, baseMoney, commands, objectives
                );
                this.categories.put(catId, category);
            }
            this.plugin.info("Loaded " + this.categories.size() + " RPG categories.");
        } catch (Exception e) {
            this.plugin.error("Failed to load RPG categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @NotNull
    public Map<String, RpgCategory> getCategories() {
        return this.categories;
    }

    @Nullable
    public RpgCategory getCategory(@NotNull String id) {
        return this.categories.get(id);
    }

    public int getDailyLimit(@NotNull Player player) {
        return Config.PERSONAL_QUESTS_DAILY_LIMITS.get().getGreatest(player).intValue();
    }

    public int getQuestsAcceptedToday(@NotNull QuestUser user) {
        int total = 0;
        for (PersonalQuestData data : user.getPersonalQuestData().values()) {
            data.checkAndResetDailyLimit();
            total += data.getQuestsAcceptedToday();
        }
        return total;
    }

    public boolean acceptQuest(@NotNull Player player, @NotNull RpgCategory category) {
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        int acceptedToday = this.getQuestsAcceptedToday(user);
        int dailyLimit = this.getDailyLimit(player);

        if (acceptedToday >= dailyLimit) {
            Lang.PERSONAL_LIMIT_REACHED.message().send(player);
            return false;
        }

        PersonalQuestData currentData = user.getPersonalQuestData().get(category.getId());
        if (currentData != null && currentData.getObjectiveId() != null) {
            return false;
        }

        Map<String, Integer> objectives = category.getObjectives();
        if (objectives.isEmpty()) {
            return false;
        }

        List<String> keys = new ArrayList<>(objectives.keySet());
        String objectiveId = keys.get(new Random().nextInt(keys.size()));
        int baseAmount = objectives.get(objectiveId);

        int level = user.getRPGLevel(category.getId());
        int requiredAmount = baseAmount * level * level;
        double scaledMoney = category.getBaseMoney() * level * level;

        int questsAcceptedToday = 0;
        long lastAcceptedDayTimestamp = 0;
        if (currentData != null) {
            currentData.checkAndResetDailyLimit();
            questsAcceptedToday = currentData.getQuestsAcceptedToday();
            lastAcceptedDayTimestamp = currentData.getLastAcceptedDayTimestamp();
        }
        questsAcceptedToday++;
        lastAcceptedDayTimestamp = System.currentTimeMillis();

        PersonalQuestData newData = new PersonalQuestData(
            category.getId(),
            objectiveId,
            0,
            requiredAmount,
            scaledMoney,
            System.currentTimeMillis(),
            questsAcceptedToday,
            lastAcceptedDayTimestamp
        );

        user.getPersonalQuestData().put(category.getId(), newData);
        this.plugin.getUserManager().save(user);

        Lang.PERSONAL_QUEST_ACCEPTED.message().send(player, replacer -> replacer
            .replace("%amount%", String.valueOf(requiredAmount))
            .replace("%objective%", objectiveId)
        );
        return true;
    }

    public boolean cancelQuest(@NotNull Player player, @NotNull RpgCategory category) {
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        PersonalQuestData currentData = user.getPersonalQuestData().get(category.getId());
        if (currentData == null || currentData.getObjectiveId() == null) {
            return false;
        }

        currentData.setObjectiveId(null);
        currentData.setProgress(0);
        this.plugin.getUserManager().save(user);

        Lang.PERSONAL_QUEST_CANCELLED.message().send(player);
        try {
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
        } catch (Exception ignored) {}
        return true;
    }

    public synchronized void handleProgress(@NotNull Player player, @NotNull String categoryType, @NotNull String objectiveId, int amount) {
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        for (RpgCategory category : this.categories.values()) {
            if (!isTypeMatch(category.getType(), categoryType)) {
                continue;
            }

            PersonalQuestData activeQuest = user.getPersonalQuestData().get(category.getId());
            if (activeQuest == null || activeQuest.getObjectiveId() == null) {
                continue;
            }

            if (isObjectiveMatch(activeQuest.getObjectiveId(), objectiveId)) {
                int newProgress = activeQuest.getProgress() + amount;
                if (newProgress >= activeQuest.getRequiredAmount()) {
                    activeQuest.setProgress(activeQuest.getRequiredAmount());
                    completeQuest(player, user, category, activeQuest);
                } else {
                    activeQuest.setProgress(newProgress);
                    this.plugin.getUserManager().save(user);
                }
            }
        }
    }

    private boolean isTypeMatch(@NotNull String categoryType, @NotNull String taskTypeId) {
        if (categoryType.equalsIgnoreCase(taskTypeId)) return true;
        String normCat = categoryType.replace("_", "").toLowerCase();
        String normTask = taskTypeId.replace("_", "").toLowerCase();
        if (normCat.equals(normTask)) return true;
        
        if ((normCat.contains("enchant") && normTask.contains("enchant")) ||
            (normCat.contains("fertiliz") && normTask.contains("fertiliz"))) {
            return true;
        }
        return false;
    }

    private boolean isObjectiveMatch(@NotNull String questObjective, @NotNull String eventObjective) {
        if (questObjective.equalsIgnoreCase(eventObjective)) return true;
        String cleanQuest = questObjective.replace("minecraft:", "").toLowerCase();
        String cleanEvent = eventObjective.replace("minecraft:", "").toLowerCase();
        if (cleanQuest.equals(cleanEvent)) return true;

        // Aliases pour les cultures (Singulier vs Pluriel entre items et blocs Spigot)
        if ((cleanQuest.equals("carrot") && cleanEvent.equals("carrots")) || (cleanQuest.equals("carrots") && cleanEvent.equals("carrot"))) return true;
        if ((cleanQuest.equals("potato") && cleanEvent.equals("potatoes")) || (cleanQuest.equals("potatoes") && cleanEvent.equals("potato"))) return true;
        if ((cleanQuest.equals("beetroot") && cleanEvent.equals("beetroots")) || (cleanQuest.equals("beetroots") && cleanEvent.equals("beetroot"))) return true;

        return false;
    }

    private void completeQuest(@NotNull Player player, @NotNull QuestUser user, @NotNull RpgCategory category, @NotNull PersonalQuestData questData) {
        questData.setObjectiveId(null);
        questData.setProgress(0);

        double money = questData.getScaledMoney();
        if (money > 0) {
            String moneyCmd = Config.PERSONAL_QUESTS_MONEY_COMMAND.get()
                .replace("%player%", player.getName())
                .replace("%money%", String.valueOf(money));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), moneyCmd);
        }

        int level = user.getRPGLevel(category.getId());
        int completions = (int) user.getRPGXP(category.getId());
        completions++;
        int completionsToLevelUp = category.getCompletionsToLevelUp();

        Lang.PERSONAL_QUEST_COMPLETED.message().send(player, replacer -> replacer
            .replace("%money%", MenuUtils.formatNumber(money))
        );

        if (completions >= completionsToLevelUp) {
            int newLevel = level + 1;
            completions = 0;
            user.getRpgCategoryLevels().put(category.getId(), newLevel);

            for (String cmd : category.getCommands()) {
                String parsedCmd = cmd.replace("%player%", player.getName()).replace("%level%", String.valueOf(newLevel));
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCmd);
            }

            Lang.PERSONAL_LEVEL_UP.message().send(player, replacer -> replacer
                .replace("%category%", category.getDisplayName())
                .replace("%level%", String.valueOf(newLevel))
            );

            try {
                String soundStr = Config.SOUNDS_LEVEL_UP.get();
                player.playSound(player.getLocation(), Sound.valueOf(soundStr.toUpperCase()), 1.0f, 1.0f);
            } catch (Exception ignored) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }

        user.getRpgCategoryXP().put(category.getId(), (double) completions);
        this.plugin.getUserManager().save(user);
    }

    public void openPersonalMenu(@NotNull Player player) {
        if (this.personalMenu != null) {
            this.personalMenu.open(player);
        }
    }

    public void openCategoriesMenu(@NotNull Player player, @NotNull RpgCategory category) {
        if (this.categoriesMenu != null) {
            this.categoriesMenu.open(player, category);
        }
    }
}
