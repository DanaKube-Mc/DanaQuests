package su.nightexpress.quests.island.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.MenuType;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.config.Config;
import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.ui.menu.MenuViewer;
import su.nightexpress.nightcore.ui.menu.data.ConfigBased;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.island.IslandManager;
import su.nightexpress.quests.island.data.IslandQuestProgress;
import su.nightexpress.quests.island.definition.IslandQuest;
import su.nightexpress.quests.island.definition.IslandQuestRequirement;
import su.nightexpress.quests.island.definition.IslandResourceGroup;
import su.nightexpress.quests.util.MenuUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IslandQuestMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private final IslandManager manager;

    private String menuTitle = "Quêtes d'Île";
    private int[] requirementSlots = new int[]{22};
    private NightItem requirementItemTemplate;
    private NightItem levelInfoItemTemplate;
    private int levelInfoSlot = 4;
    private NightItem resourceGroupInfoItemTemplate;
    private int resourceGroupInfoSlot = 40;
    private String weightFormat;

    public IslandQuestMenu(@NotNull QuestsPlugin plugin, @NotNull IslandManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Quêtes d'Île");
        this.manager = manager;
        this.setAutoRefreshInterval(1);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        UUID islandUuid = manager.getPlayerIsland(player);
        if (islandUuid == null) {
            this.runNextTick(player::closeInventory);
            return;
        }

        IslandQuest activeQuest = manager.getActiveQuest(islandUuid);
        if (activeQuest == null) {
            viewer.addItem(NightItem.fromType(Material.BOOK)
                .setDisplayName("&aQuêtes d'Île Complétées !")
                .setLore(Arrays.asList("&7Vous avez complété toutes les quêtes d'île coopératives."))
                .toMenuItem()
                .setPriority(10)
                .setSlots(22)
                .build()
            );
            return;
        }

        IslandQuestProgress progress = manager.getProgress(islandUuid, activeQuest.getId());

        if (levelInfoItemTemplate != null && levelInfoSlot >= 0) {
            String descStr = activeQuest.getDescription() != null ? String.join("\n", activeQuest.getDescription()) : "";
            viewer.addItem(levelInfoItemTemplate.copy()
                .hideAllComponents()
                .replacement(replacer -> replacer
                    .replace("%level_name%", activeQuest.getName())
                    .replace("%level_id%", activeQuest.getId())
                    .replace("%level_order%", String.valueOf(activeQuest.getOrder()))
                    .replace("%level_description%", descStr)
                    .replace("%level_lore%", descStr)
                )
                .toMenuItem()
                .setPriority(10)
                .setSlots(levelInfoSlot)
                .build()
            );
        }

        if (resourceGroupInfoItemTemplate != null && resourceGroupInfoSlot >= 0) {
            viewer.addItem(resourceGroupInfoItemTemplate.copy()
                .hideAllComponents()
                .toMenuItem()
                .setPriority(10)
                .setSlots(resourceGroupInfoSlot)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> manager.openResourceGroupMenu(player));
                })
                .build()
            );
        }

        List<IslandQuestRequirement> requirements = activeQuest.getRequirements();
        for (int i = 0; i < requirements.size() && i < requirementSlots.length; i++) {
            IslandQuestRequirement req = requirements.get(i);
            int slot = requirementSlots[i];

            int reqProgress = progress.getRequirementProgress(req.getId());
            int target = req.getTargetAmount();
            double percent = target > 0 ? (reqProgress * 100.0 / target) : 100.0;
            String progressBar = MenuUtils.buildProgressBar(target > 0 ? ((double) reqProgress / target) : 1.0);

            IslandResourceGroup grp = manager.getResourceGroup(req.getResourceGroupId());
            String groupName = grp != null ? grp.getName() : req.getResourceGroupId();

            String weightLore = grp != null ? MenuUtils.formatWeightLore(grp, this.weightFormat) : "";

            NightItem reqItem;
            if (grp != null && grp.getIcon() != null) {
                reqItem = NightItem.fromType(grp.getIcon());
                reqItem.setDisplayName(requirementItemTemplate.getDisplayName());
                reqItem.setLore(requirementItemTemplate.getLore());
            } else {
                reqItem = requirementItemTemplate.copy();
            }
            reqItem.hideAllComponents()
                .replacement(replacer -> replacer
                    .replace("%requirement_name%", req.getName())
                    .replace("%resource_group_name%", groupName)
                    .replace("%progress%", MenuUtils.formatNumber(reqProgress))
                    .replace("%target%", MenuUtils.formatNumber(target))
                    .replace("%percent%", String.format("%.1f", percent))
                    .replace("%progress_bar%", progressBar)
                    .replace("%weight_lore%", weightLore)
                );

            viewer.addItem(reqItem.toMenuItem()
                .setPriority(10)
                .setSlots(slot)
                .setHandler((viewer1, event) -> {
                    boolean all = event.isRightClick();
                    manager.deposit(player, req, all);
                })
                .build()
            );
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
    }

    @Override
    protected void onItemPrepare(@NotNull MenuViewer viewer, @NotNull MenuItem menuItem, @NotNull NightItem item) {
        super.onItemPrepare(viewer, menuItem, item);

        Player player = viewer.getPlayer();
        item.replacement(replacer -> replacer.replace("%player%", player.getName()));
        item.setSkullOwner(player);
    }

    private void handleReturn(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event) {
        this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.menuTitle = ConfigValue.create("Settings.Title", "Quêtes d'Île").read(config);
        this.setTitle(menuTitle);

        this.weightFormat = ConfigValue.create("Weight_Format", Config.ISLAND_WEIGHT_FORMAT.get()).read(config);

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, this::handleReturn));
        loader.addHandler("back-profile", (viewer, event) -> {
            this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
        });
        loader.addHandler("back_profile", (viewer, event) -> {
            this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
        });

        if (config.contains("Decorations")) {
            for (String decId : config.getSection("Decorations")) {
                String path = "Decorations." + decId;
                String materialStr = config.getString(path + ".material", "AIR");
                String slotsStr = config.getString(path + ".slots", "");
                int[] slots = parseSlots(slotsStr);
                if (slots.length > 0) {
                    try {
                        Material mat = Material.valueOf(materialStr.toUpperCase());
                        loader.addDefaultItem(NightItem.fromType(mat)
                            .setHideTooltip(true)
                            .toMenuItem()
                            .setPriority(-99)
                            .setSlots(slots)
                        );
                    } catch (Exception ignored) {}
                }
            }
        }

        String reqSlotsStr = config.getString("Requirement_Slots", "20,21,22,23,24");
        this.requirementSlots = parseSlots(reqSlotsStr);

        this.requirementItemTemplate = ConfigValue.create("Requirement_Item.Item", NightItem.fromType(Material.CHEST)).read(config);
        if (config.contains("Requirement_Item") && !config.contains("Requirement_Item.Item")) {
            this.requirementItemTemplate = ConfigValue.create("Requirement_Item", NightItem.fromType(Material.CHEST)).read(config);
        }

        this.levelInfoItemTemplate = ConfigValue.create("Level_Info_Item.Item", NightItem.fromType(Material.BOOK)).read(config);
        if (config.contains("Level_Info_Item") && !config.contains("Level_Info_Item.Item")) {
            this.levelInfoItemTemplate = ConfigValue.create("Level_Info_Item", NightItem.fromType(Material.BOOK)).read(config);
        }
        this.levelInfoSlot = config.getInt("Level_Info_Item.slot", 4);

        if (config.contains("Resource_Group_Info_Item")) {
            this.resourceGroupInfoItemTemplate = ConfigValue.create("Resource_Group_Info_Item.Item", NightItem.fromType(Material.PAPER)).read(config);
            if (!config.contains("Resource_Group_Info_Item.Item")) {
                this.resourceGroupInfoItemTemplate = ConfigValue.create("Resource_Group_Info_Item", NightItem.fromType(Material.PAPER)).read(config);
            }
            this.resourceGroupInfoSlot = config.getInt("Resource_Group_Info_Item.slot", 40);
        } else {
            this.resourceGroupInfoItemTemplate = null;
            this.resourceGroupInfoSlot = -1;
        }
    }

    private int[] parseSlots(String slotsStr) {
        if (slotsStr == null || slotsStr.trim().isEmpty()) {
            return new int[0];
        }
        try {
            List<Integer> slots = new ArrayList<>();
            String[] split = slotsStr.split(",");
            for (String s : split) {
                s = s.trim();
                if (s.contains("-")) {
                    String[] range = s.split("-");
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    for (int i = start; i <= end; i++) {
                        slots.add(i);
                    }
                } else {
                    slots.add(Integer.parseInt(s));
                }
            }
            return slots.stream().mapToInt(Integer::intValue).toArray();
        } catch (Exception e) {
            return new int[0];
        }
    }
}
