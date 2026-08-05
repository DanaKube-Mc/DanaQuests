package su.nightexpress.quests.personal.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.personal.PersonalQuestManager;
import su.nightexpress.quests.personal.data.PersonalQuestData;
import su.nightexpress.quests.personal.definition.RpgCategory;
import su.nightexpress.quests.user.QuestUser;
import su.nightexpress.quests.util.MenuUtils;

import java.util.*;

public class PersonalQuestMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private final PersonalQuestManager manager;
    private int[] defaultSlots = new int[0];
    private Map<Integer, int[]> slotsByCategoryCount = new HashMap<>();

    private String menuTitle = "Quêtes Personnelles";
    private String questItemDisplayName = "&d%quest_name% %level% [%completions%/%required_completions%]";
    private List<String> questItemLore = Arrays.asList("%description%", "", "%status%", "&aClic Droit pour voir les objectifs.");
    private String categoryLoreSuffixActive = "\n&6Quête active: &e%objective%\n&7Progression: &e%progress%/%required%\n&7Récompense: &a%money% $\n\n&eClic Gauche pour quitter la quête.";
    private String categoryLoreSuffixInactive = "\n&aAucune quête active.\n&eClic Gauche pour accepter la quête.";

    public PersonalQuestMenu(@NotNull QuestsPlugin plugin, @NotNull PersonalQuestManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Quêtes Personnelles");
        this.manager = manager;
        this.setAutoRefreshInterval(1);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);

        List<RpgCategory> categories = new ArrayList<>(this.manager.getCategories().values());
        int count = categories.size();
        int[] slots = this.slotsByCategoryCount.get(count);
        if (slots == null || slots.length == 0) {
            slots = this.defaultSlots.length > 0 ? this.defaultSlots : new int[]{10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};
        }

        for (int i = 0; i < slots.length && i < categories.size(); i++) {
            int slot = slots[i];
            RpgCategory category = categories.get(i);

            int level = user.getRPGLevel(category.getId());
            int completions = (int) user.getRPGXP(category.getId());
            int completionsToLevelUp = category.getCompletionsToLevelUp();
            PersonalQuestData activeQuest = user.getPersonalQuestData().get(category.getId());

            Material mat = null;
            try {
                mat = Material.valueOf(category.getIconMaterial().toUpperCase());
            } catch (Exception ignored) {
                mat = Material.BOOK;
            }

            String finalTitle = questItemDisplayName
                .replace("%quest_name%", category.getDisplayName())
                .replace("%category_name%", category.getDisplayName())
                .replace("%name%", category.getDisplayName())
                .replace("%level%", String.valueOf(level))
                .replace("%completions%", String.valueOf(completions))
                .replace("%required_completions%", String.valueOf(completionsToLevelUp));

            boolean hasActive = activeQuest != null && activeQuest.hasActiveQuest();

            List<String> finalLore = new ArrayList<>();
            for (String line : this.questItemLore) {
                if (line.equalsIgnoreCase("%description%")) {
                    for (String descLine : category.getIconLore()) {
                        finalLore.add(descLine
                            .replace("%level%", String.valueOf(level))
                            .replace("%completions%", String.valueOf(completions))
                            .replace("%required_completions%", String.valueOf(completionsToLevelUp))
                        );
                    }
                } else if (line.equalsIgnoreCase("%status%")) {
                    if (hasActive) {
                        String objectiveName = activeQuest.getObjectiveId();
                        if (objectiveName == null) objectiveName = "";
                        String suffix = categoryLoreSuffixActive
                            .replace("%objective%", objectiveName)
                            .replace("%progress%", String.valueOf(activeQuest.getProgress()))
                            .replace("%required%", String.valueOf(activeQuest.getRequiredAmount()))
                            .replace("%money%", String.valueOf(activeQuest.getScaledMoney()));
                        finalLore.addAll(Arrays.asList(suffix.split("\n")));
                    } else {
                        finalLore.addAll(Arrays.asList(categoryLoreSuffixInactive.split("\n")));
                    }
                } else {
                    finalLore.add(line
                        .replace("%quest_name%", category.getDisplayName())
                        .replace("%category_name%", category.getDisplayName())
                        .replace("%name%", category.getDisplayName())
                        .replace("%level%", String.valueOf(level))
                        .replace("%completions%", String.valueOf(completions))
                        .replace("%required_completions%", String.valueOf(completionsToLevelUp))
                    );
                }
            }

            NightItem nightItem = NightItem.fromType(mat)
                .setDisplayName(finalTitle)
                .setLore(finalLore)
                .hideAllComponents();

            if (category.getIconCustomModelData() > 0) {
                nightItem.setCustomModelData((float) category.getIconCustomModelData());
            }

            MenuItem item = nightItem.toMenuItem()
                .setSlots(slot)
                .setPriority(Integer.MAX_VALUE)
                .setHandler((viewer1, event) -> {
                    if (event.isLeftClick()) {
                        if (!hasActive) {
                            if (this.manager.acceptQuest(player, category)) {
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                this.runNextTick(() -> this.flush(viewer1));
                            }
                        } else {
                            if (this.manager.cancelQuest(player, category)) {
                                this.runNextTick(() -> this.flush(viewer1));
                            }
                        }
                    } else if (event.isRightClick()) {
                        this.runNextTick(() -> this.manager.openCategoriesMenu(player, category));
                    }
                })
                .build();

            viewer.addItem(item);
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
    }

    private void handleReturn(@NotNull MenuViewer viewer, @NotNull org.bukkit.event.inventory.InventoryClickEvent event) {
        this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        Player player = viewer.getPlayer();
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        int acceptedToday = this.manager.getQuestsAcceptedToday(user);
        int dailyLimit = this.manager.getDailyLimit(player);

        item.replacement(replacer -> replacer
            .replace("%player%", player.getName())
            .replace("%daily_progress%", String.valueOf(acceptedToday))
            .replace("%daily_count%", String.valueOf(acceptedToday))
            .replace("%accepted_today%", String.valueOf(acceptedToday))
            .replace("%daily_limit%", String.valueOf(dailyLimit))
        );
        item.setSkullOwner(player);
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.menuTitle = ConfigValue.create("Settings.Title", "Quêtes Personnelles").read(config);
        this.setTitle(menuTitle);

        this.questItemDisplayName = ConfigValue.create("Quest.Item.Display_Name", "&d%quest_name% %level% [%completions%/%required_completions%]").read(config);
        this.questItemLore = ConfigValue.create("Quest.Item.Lore", Arrays.asList("%description%", "", "%status%", "&aClic Droit pour voir les objectifs.")).read(config);

        this.categoryLoreSuffixActive = ConfigValue.create("status.active.suffix", categoryLoreSuffixActive).read(config);
        this.categoryLoreSuffixInactive = ConfigValue.create("status.inactive.suffix", categoryLoreSuffixInactive).read(config);

        this.defaultSlots = MenuUtils.parseSlots(config.getString("Quest.Slots", ""));
        this.slotsByCategoryCount = MenuUtils.loadSlotsByCount(config, "Quest");

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, this::handleReturn));
    }
}
