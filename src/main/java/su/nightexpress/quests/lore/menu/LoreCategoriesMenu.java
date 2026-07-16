package su.nightexpress.quests.lore.menu;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
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
import su.nightexpress.quests.lore.LoreManager;
import su.nightexpress.quests.lore.data.LoreQuestData;
import su.nightexpress.quests.lore.definition.LoreObjective;
import su.nightexpress.quests.lore.definition.LoreQuest;
import su.nightexpress.quests.lore.definition.LoreQuestCategory;
import su.nightexpress.quests.tracker.QuestTrackerManager;
import su.nightexpress.quests.user.QuestUser;

import java.util.*;

public class LoreCategoriesMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private final LoreManager manager;
    private final TreeMap<Integer, int[]> slotsByCategoryCount = new TreeMap<>();

    private String menuTitle = "Chapitres";
    private String lockedName = "&c[Verrouillé] &7%category_name%";
    private List<String> lockedLore = Collections.singletonList("&7Complétez les chapitres précédents.");
    private String completedName = "&a[Complété] &f%category_name%";
    private List<String> completedLore = Collections.singletonList("&7Chapitre terminé !");
    private String activeName = "&e[En Cours] &f%category_name%";
    private List<String> activeLore = Arrays.asList(
            "%category_description%",
            "",
            "&6Quête active: &e%quest_name%",
            "%quest_description%",
            "",
            "&6Objectifs:",
            "%quest_objectives%",
            "",
            "&7Clic Gauche pour voir la progression.",
            "&7Clic Droit pour &e%tracker_action% &7le tracker.",
            "&7Statut du tracker: %tracker_status%"
    );

    private String lockedMaterialStr = "BARRIER";
    private int lockedCustomModelData = 0;

    private String completedMaterialStr = "";
    private int completedCustomModelData = -1;

    private String activeMaterialStr = "";
    private int activeCustomModelData = -1;

    public LoreCategoriesMenu(@NotNull QuestsPlugin plugin, @NotNull LoreManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Chapitres");
        this.manager = manager;
        this.setAutoRefreshInterval(1);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);

        List<LoreQuestCategory> categories = new ArrayList<>(this.manager.getCategories().values());
        int count = categories.size();
        int[] slots = Optional.ofNullable(this.slotsByCategoryCount.ceilingEntry(count)).map(Map.Entry::getValue).orElse(new int[0]);

        for (int i = 0; i < slots.length && i < categories.size(); i++) {
            int slot = slots[i];
            LoreQuestCategory category = categories.get(i);

            MenuItem.Builder menuItem;
            if (!this.manager.isCategoryUnlocked(user, category)) {
                Material mat = null;
                if (category.getInactiveIconMaterial() != null && !category.getInactiveIconMaterial().isEmpty()) {
                    try {
                        mat = Material.valueOf(category.getInactiveIconMaterial().toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (mat == null) {
                    try {
                        mat = Material.valueOf(lockedMaterialStr.toUpperCase());
                    } catch (Exception ignored) {
                        mat = Material.BARRIER;
                    }
                }
                int cmd = (category.getInactiveIconCustomModelData() > 0) ? category.getInactiveIconCustomModelData() : lockedCustomModelData;
                String namePat = (category.getInactiveIconName() != null) ? category.getInactiveIconName() : lockedName;
                List<String> lorePat = (category.getInactiveIconLore() != null) ? category.getInactiveIconLore() : lockedLore;

                menuItem = buildItem(category, mat, cmd, namePat, lorePat, user, player);
            } else if (this.manager.isCategoryCompleted(user, category)) {
                Material mat = null;
                if (category.getFinishedIconMaterial() != null && !category.getFinishedIconMaterial().isEmpty()) {
                    try {
                        mat = Material.valueOf(category.getFinishedIconMaterial().toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (mat == null && completedMaterialStr != null && !completedMaterialStr.isEmpty()) {
                    try {
                        mat = Material.valueOf(completedMaterialStr.toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (mat == null) {
                    try {
                        mat = Material.valueOf(category.getIconMaterial().toUpperCase());
                    } catch (Exception ignored) {
                        mat = Material.BOOK;
                    }
                }
                int cmd = (category.getFinishedIconCustomModelData() > 0) ? category.getFinishedIconCustomModelData() : 
                          ((completedCustomModelData != -1) ? completedCustomModelData : category.getIconCustomModelData());
                String namePat = (category.getFinishedIconName() != null) ? category.getFinishedIconName() : completedName;
                List<String> lorePat = (category.getFinishedIconLore() != null) ? category.getFinishedIconLore() : completedLore;

                menuItem = buildItem(category, mat, cmd, namePat, lorePat, user, player);
            } else {
                Material mat = null;
                if (category.getActiveIconMaterial() != null && !category.getActiveIconMaterial().isEmpty()) {
                    try {
                        mat = Material.valueOf(category.getActiveIconMaterial().toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (mat == null && activeMaterialStr != null && !activeMaterialStr.isEmpty()) {
                    try {
                        mat = Material.valueOf(activeMaterialStr.toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (mat == null) {
                    try {
                        mat = Material.valueOf(category.getIconMaterial().toUpperCase());
                    } catch (Exception ignored) {
                        mat = Material.WRITABLE_BOOK;
                    }
                }
                int cmd = (category.getActiveIconCustomModelData() > 0) ? category.getActiveIconCustomModelData() : 
                          ((activeCustomModelData != -1) ? activeCustomModelData : category.getIconCustomModelData());
                String namePat = (category.getActiveIconName() != null) ? category.getActiveIconName() : activeName;
                List<String> lorePat = (category.getActiveIconLore() != null) ? category.getActiveIconLore() : activeLore;

                menuItem = buildItem(category, mat, cmd, namePat, lorePat, user, player);
            }

            viewer.addItem(menuItem.setSlots(slot).build());
        }
    }

    private MenuItem.Builder buildItem(@NotNull LoreQuestCategory category, @NotNull Material fallbackMaterial, int customModelData, @NotNull String titlePattern, @NotNull List<String> lorePattern, @NotNull QuestUser user, @NotNull Player player) {
        LoreQuest activeQuest = this.manager.getActiveQuest(user, category);
        
        String questName = activeQuest != null ? activeQuest.getName() : "";
        List<String> questDesc = activeQuest != null ? activeQuest.getDescription() : Collections.emptyList();
        
        List<String> objectivesFormatted = new ArrayList<>();
        if (activeQuest != null) {
            for (LoreObjective obj : activeQuest.getObjectives()) {
                int current = this.manager.getObjectiveProgress(user, activeQuest, obj);
                String color = current >= obj.getRequired() ? "&a" : "&7";
                objectivesFormatted.add("  " + color + "- " + obj.getDescription() + " &8(" + current + "/" + obj.getRequired() + ")");
            }
        }

        boolean trackerDisabled = user.isCategoryTrackerDisabled(category.getId());
        String trackerStatus = trackerDisabled ? "&cDésactivé" : "&aActivé";
        String trackerAction = trackerDisabled ? "Activer" : "Désactiver";

        List<String> finalLore = new ArrayList<>();
        for (String line : lorePattern) {
            if (line.contains("%category_description%")) {
                for (String descLine : category.getDescription()) {
                    finalLore.add(descLine);
                }
            } else if (line.contains("%quest_description%")) {
                for (String qLine : questDesc) {
                    finalLore.add(qLine);
                }
            } else if (line.contains("%quest_objectives%")) {
                for (String objLine : objectivesFormatted) {
                    finalLore.add(objLine);
                }
            } else {
                finalLore.add(line
                        .replace("%category_name%", category.getName())
                        .replace("%quest_name%", questName)
                        .replace("%tracker_status%", trackerStatus)
                        .replace("%tracker_action%", trackerAction)
                );
            }
        }

        String finalTitle = titlePattern.replace("%category_name%", category.getName());

        NightItem nightItem = NightItem.fromType(fallbackMaterial)
                .setDisplayName(finalTitle)
                .setLore(finalLore)
                .hideAllComponents();

        if (customModelData > 0) {
            nightItem.setCustomModelData((float) customModelData);
        }

        return nightItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setHandler((viewer1, event) -> {
                    if (event.isLeftClick()) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                        if (this.manager.isCategoryUnlocked(user, category)) {
                            this.runNextTick(() -> this.manager.openProgression(player, category));
                        }
                    } else if (event.isRightClick()) {
                        boolean newDisabled = !user.isCategoryTrackerDisabled(category.getId());
                        user.toggleCategoryTracker(category.getId(), newDisabled);
                        this.plugin.getUserManager().save(user);

                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);

                        if (newDisabled) {
                            QuestTrackerManager.forceCleanupCategory(player, category.getId());
                        } else {
                            if (activeQuest != null) {
                                int totalProgress = 0;
                                int totalRequired = 0;
                                LoreQuestData questProgress = user.getLoreQuestsProgress().get(activeQuest.getId());
                                if (questProgress != null) {
                                    for (LoreObjective obj : activeQuest.getObjectives()) {
                                        totalProgress += questProgress.getProgress(obj.getId());
                                        totalRequired += obj.getRequired();
                                    }
                                } else {
                                    totalRequired = activeQuest.getObjectives().stream().mapToInt(LoreObjective::getRequired).sum();
                                }
                                QuestTrackerManager.showLoreProgress(player, activeQuest, category, totalProgress, totalRequired);
                            }
                        }

                        this.runNextTick(() -> this.flush(viewer1));
                    }
                });
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
    }

    private void handleReturn(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event) {
        this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.menuTitle = ConfigValue.create("Settings.Title", "Chapitres").read(config);
        this.setTitle(menuTitle);

        lockedName = ConfigValue.create("status.locked.name", "&c[Verrouillé] &7%category_name%").read(config);
        lockedLore = ConfigValue.create("status.locked.lore", Collections.singletonList("&7Complétez les chapitres précédents.")).read(config);

        completedName = ConfigValue.create("status.completed.name", "&a[Complété] &f%category_name%").read(config);
        completedLore = ConfigValue.create("status.completed.lore", Collections.singletonList("&7Chapitre terminé !")).read(config);

        activeName = ConfigValue.create("status.active.name", "&e[En Cours] &f%category_name%").read(config);
        activeLore = ConfigValue.create("status.active.lore", Arrays.asList(
                "%category_description%",
                "",
                "&6Quête active: &e%quest_name%",
                "%quest_description%",
                "",
                "&6Objectifs:",
                "%quest_objectives%",
                "",
                "&7Clic Gauche pour voir la progression.",
                "&7Clic Droit pour &e%tracker_action% &7le tracker.",
                "&7Statut du tracker: %tracker_status%"
        )).read(config);

        this.lockedMaterialStr = ConfigValue.create("status.locked.material", "BARRIER").read(config);
        this.lockedCustomModelData = ConfigValue.create("status.locked.custom_model_data", 0).read(config);

        this.completedMaterialStr = ConfigValue.create("status.completed.material", "").read(config);
        this.completedCustomModelData = ConfigValue.create("status.completed.custom_model_data", -1).read(config);

        this.activeMaterialStr = ConfigValue.create("status.active.material", "").read(config);
        this.activeCustomModelData = ConfigValue.create("status.active.custom_model_data", -1).read(config);

        this.slotsByCategoryCount.clear();
        for (int count = 0; count < 10; count++) {
            int amount = count + 1;
            int[] defSlots = getDefaultSlots(amount);
            int[] skillSlots = ConfigValue.create("Quest.SlotsByCount." + amount, defSlots).read(config);
            this.slotsByCategoryCount.put(amount, skillSlots);
        }

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, this::handleReturn));

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE)
            .setHideTooltip(true)
            .toMenuItem()
            .setPriority(-1)
            .setSlots(0,1,2,3,4,5,6,7,8,36,37,38,39,40,41,42,43,44)
        );

        loader.addDefaultItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE)
            .setHideTooltip(true)
            .toMenuItem()
            .setPriority(-1)
            .setSlots(java.util.stream.IntStream.range(9, 36).toArray())
        );
    }

    private static int[] getDefaultSlots(int count) {
        return switch (count) {
            case 1 -> new int[]{22};
            case 2 -> new int[]{21, 23};
            case 3 -> new int[]{21, 22, 23};
            case 4 -> new int[]{21, 22, 24, 25};
            case 5 -> new int[]{20, 21, 22, 23, 24};
            case 6 -> new int[]{20, 21, 22, 23, 24, 31};
            case 7 -> new int[]{20, 21, 22, 23, 24, 30, 32};
            case 8 -> new int[]{20, 21, 22, 23, 24, 30, 31, 32};
            case 9 -> new int[]{20, 21, 22, 23, 24, 29, 30, 32, 33};
            case 10 -> new int[]{20, 21, 22, 23, 24, 29, 30, 31, 32, 33};
            default -> new int[]{};
        };
    }
}
