package su.nightexpress.quests.menu;

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
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.tracker.QuestTrackerManager;
import su.nightexpress.quests.user.QuestUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.lang.reflect.Field;

public class MainMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private NightItem dailyItem;
    private int[] dailySlots;

    private NightItem milestonesItem;
    private int[] milestonesSlots;

    private NightItem loreItem;
    private int[] loreSlots;

    private NightItem islandItem;
    private int[] islandSlots;

    private NightItem personalItem;
    private int[] personalSlots;

    private NightItem battlePassItem;
    private int[] battlePassSlots;

    private NightItem communityItem;
    private int[] communitySlots;

    public MainMenu(@NotNull QuestsPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X5, "Quests");
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);

        if (dailyItem != null && dailySlots != null && dailySlots.length > 0 && Config.FEATURES_QUESTS_ENABLED.get()) {
            viewer.addItem(dailyItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setSlots(dailySlots)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> this.plugin.questManager().ifPresent(manager -> manager.openQuests(player)));
                })
                .build());
        }

        if (milestonesItem != null && milestonesSlots != null && milestonesSlots.length > 0 && Config.FEATURES_MILESTONES_ENABLED.get()) {
            viewer.addItem(milestonesItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setSlots(milestonesSlots)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> this.plugin.milestoneManager().ifPresent(manager -> manager.openCategories(player)));
                })
                .build());
        }

        if (loreItem != null && loreSlots != null && loreSlots.length > 0 && Config.FEATURES_LORE_ENABLED.get()) {
            viewer.addItem(loreItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setSlots(loreSlots)
                .setHandler((viewer1, event) -> {
                    if (event.isLeftClick()) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                        this.runNextTick(() -> this.plugin.loreManager().ifPresent(manager -> manager.openLoreMenu(player)));
                    } else if (event.isRightClick()) {
                        boolean newDisabled = !user.isCategoryTrackerDisabled("lore");
                        user.toggleCategoryTracker("lore", newDisabled);
                        this.plugin.getUserManager().save(user);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                        if (QuestTrackerManager.getInstance() != null) {
                            QuestTrackerManager.getInstance().refreshPlayerTrackers(player);
                        }
                        this.runNextTick(() -> this.flush(viewer1));
                    }
                })
                .build());
        }

        if (islandItem != null && islandSlots != null && islandSlots.length > 0 && Config.FEATURES_ISLAND_QUESTS_ENABLED.get()) {
            viewer.addItem(islandItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setSlots(islandSlots)
                .setHandler((viewer1, event) -> {
                    if (event.isLeftClick()) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                        this.runNextTick(() -> this.plugin.islandManager().ifPresent(manager -> manager.openMenu(player)));
                    } else if (event.isRightClick()) {
                        boolean newDisabled = !user.isCategoryTrackerDisabled("island");
                        user.toggleCategoryTracker("island", newDisabled);
                        this.plugin.getUserManager().save(user);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                        if (QuestTrackerManager.getInstance() != null) {
                            QuestTrackerManager.getInstance().refreshPlayerTrackers(player);
                        }
                        this.runNextTick(() -> this.flush(viewer1));
                    }
                })
                .build());
        }

        if (personalItem != null && personalSlots != null && personalSlots.length > 0 && Config.FEATURES_PERSONAL_QUESTS_ENABLED.get()) {
            viewer.addItem(personalItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setSlots(personalSlots)
                .setHandler((viewer1, event) -> {
                    if (event.isLeftClick()) {
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                        this.runNextTick(() -> this.plugin.personalQuestManager().ifPresent(manager -> manager.openPersonalMenu(player)));
                    } else if (event.isRightClick()) {
                        boolean newDisabled = !user.isCategoryTrackerDisabled("personal");
                        user.toggleCategoryTracker("personal", newDisabled);
                        this.plugin.getUserManager().save(user);
                        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.8f, 1.0f);
                        if (QuestTrackerManager.getInstance() != null) {
                            QuestTrackerManager.getInstance().refreshPlayerTrackers(player);
                        }
                        this.runNextTick(() -> this.flush(viewer1));
                    }
                })
                .build());
        }

        if (battlePassItem != null && battlePassSlots != null && battlePassSlots.length > 0 && Config.FEATURES_BATTLE_PASS_ENABLED.get()) {
            viewer.addItem(battlePassItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setSlots(battlePassSlots)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> this.plugin.battlePassManager().ifPresent(manager -> manager.openBattlePass(player)));
                })
                .build());
        }

        if (communityItem != null && communitySlots != null && communitySlots.length > 0 && Config.FEATURES_COMMUNITY_QUESTS_ENABLED.get()) {
            viewer.addItem(communityItem.toMenuItem()
                .setPriority(Integer.MAX_VALUE)
                .setSlots(communitySlots)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> this.plugin.communityQuestManager().ifPresent(manager -> manager.openLeaderboardMenu(player)));
                })
                .build());
        }
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        Player player = viewer.getPlayer();
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);

        int[] slots = menuItem.getSlots();
        boolean isLore = loreSlots != null && slots != null && Arrays.stream(slots).anyMatch(s -> Arrays.stream(loreSlots).anyMatch(ls -> ls == s));
        boolean isIsland = islandSlots != null && slots != null && Arrays.stream(slots).anyMatch(s -> Arrays.stream(islandSlots).anyMatch(is -> is == s));
        boolean isPersonal = personalSlots != null && slots != null && Arrays.stream(slots).anyMatch(s -> Arrays.stream(personalSlots).anyMatch(ps -> ps == s));

        boolean disabled = isLore ? user.isCategoryTrackerDisabled("lore")
                : isIsland ? user.isCategoryTrackerDisabled("island")
                : isPersonal ? user.isCategoryTrackerDisabled("personal")
                : false;

        String trackerAction = disabled ? "afficher" : "masquer";
        String trackerStatus = disabled ? "<red>Désactivé</red>" : "<green>Activé</green>";

        item.replacement(replacer -> replacer
            .replace("%player%", player.getName())
            .replace("%tracker_action%", trackerAction)
            .replace("%tracker_status%", trackerStatus)
        );
        item.setSkullOwner(player);
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        String title = config.getString("Settings.Title", "Quests");
        this.setTitle(title);

        String menuTypeStr = config.getString("Settings.MenuType", "");
        if (!menuTypeStr.isEmpty()) {
            try {
                String name = menuTypeStr.replace("minecraft:", "").toUpperCase();
                Field field = MenuType.class.getField(name);
                if (MenuType.class.isAssignableFrom(field.getType())) {
                    this.setMenuType((MenuType) field.get(null));
                }
            } catch (Exception ignored) {}
        }

        if (config.contains("Types.SlotsByCount.DailyQuests")) {
            this.dailyItem = ConfigValue.create("Types.SlotsByCount.DailyQuests.Item", NightItem.fromType(Material.KNOWLEDGE_BOOK)).read(config);
            this.dailySlots = parseSlots(config.get("Types.SlotsByCount.DailyQuests.Slots"));
        } else {
            this.dailyItem = null;
            this.dailySlots = null;
        }

        if (config.contains("Types.SlotsByCount.Milestones")) {
            this.milestonesItem = ConfigValue.create("Types.SlotsByCount.Milestones.Item", NightItem.fromType(Material.KNOWLEDGE_BOOK)).read(config);
            this.milestonesSlots = parseSlots(config.get("Types.SlotsByCount.Milestones.Slots"));
        } else {
            this.milestonesItem = null;
            this.milestonesSlots = null;
        }

        if (config.contains("Types.SlotsByCount.LoreQuests")) {
            this.loreItem = ConfigValue.create("Types.SlotsByCount.LoreQuests.Item", NightItem.fromType(Material.KNOWLEDGE_BOOK)).read(config);
            this.loreSlots = parseSlots(config.get("Types.SlotsByCount.LoreQuests.Slots"));
        } else {
            this.loreItem = null;
            this.loreSlots = null;
        }

        if (config.contains("Types.SlotsByCount.IslandQuests")) {
            this.islandItem = ConfigValue.create("Types.SlotsByCount.IslandQuests.Item", NightItem.fromType(Material.LECTERN)).read(config);
            this.islandSlots = parseSlots(config.get("Types.SlotsByCount.IslandQuests.Slots"));
        } else {
            this.islandItem = null;
            this.islandSlots = null;
        }

        String personalPath = null;
        if (config.contains("Types.SlotsByCount.PersonalQuests")) {
            personalPath = "Types.SlotsByCount.PersonalQuests";
        } else if (config.contains("Types.SlotsByCount.PersonnalQuests")) {
            personalPath = "Types.SlotsByCount.PersonnalQuests";
        }

        if (personalPath != null) {
            this.personalItem = ConfigValue.create(personalPath + ".Item", NightItem.fromType(Material.FLOW_BANNER_PATTERN)).read(config);
            this.personalSlots = parseSlots(config.get(personalPath + ".Slots"));
        } else {
            this.personalItem = null;
            this.personalSlots = null;
        }

        if (config.contains("Types.SlotsByCount.BattlePass")) {
            this.battlePassItem = ConfigValue.create("Types.SlotsByCount.BattlePass.Item", NightItem.fromType(Material.PAPER)).read(config);
            this.battlePassSlots = parseSlots(config.get("Types.SlotsByCount.BattlePass.Slots"));
        } else {
            this.battlePassItem = null;
            this.battlePassSlots = null;
        }

        if (config.contains("Types.SlotsByCount.CommunityQuests")) {
            this.communityItem = ConfigValue.create("Types.SlotsByCount.CommunityQuests.Item", NightItem.fromType(Material.NETHER_STAR)).read(config);
            this.communitySlots = parseSlots(config.get("Types.SlotsByCount.CommunityQuests.Slots"));
        } else {
            this.communityItem = null;
            this.communitySlots = null;
        }
    }

    private int[] parseSlots(Object obj) {
        if (obj == null) return new int[0];
        String slotsStr = String.valueOf(obj);
        if (slotsStr.trim().isEmpty()) {
            return new int[0];
        }
        try {
            List<Integer> slotsList = new ArrayList<>();
            String[] split = slotsStr.split(",");
            for (String s : split) {
                s = s.trim();
                if (s.contains("-")) {
                    String[] range = s.split("-");
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    for (int i = start; i <= end; i++) {
                        slotsList.add(i);
                    }
                } else {
                    slotsList.add(Integer.parseInt(s));
                }
            }
            return slotsList.stream().mapToInt(Integer::intValue).toArray();
        } catch (Exception e) {
            return new int[0];
        }
    }
}
