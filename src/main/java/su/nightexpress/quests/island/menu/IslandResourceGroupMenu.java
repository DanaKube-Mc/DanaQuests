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
import su.nightexpress.quests.island.definition.IslandResourceGroup;
import su.nightexpress.quests.util.MenuUtils;

import java.util.ArrayList;
import java.util.List;

public class IslandResourceGroupMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private final IslandManager manager;

    private String menuTitle = "Poids des Ressources";
    private int[] groupSlots = new int[]{10, 11, 12, 13, 14, 15, 16};
    private NightItem groupItemTemplate;
    private NightItem returnItemTemplate;
    private int returnSlot = 40;
    private String weightFormat;

    public IslandResourceGroupMenu(@NotNull QuestsPlugin plugin, @NotNull IslandManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Poids des Ressources");
        this.manager = manager;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();

        if (returnItemTemplate != null && returnSlot >= 0) {
            viewer.addItem(returnItemTemplate.copy()
                .toMenuItem()
                .setPriority(10)
                .setSlots(returnSlot)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> manager.openMenu(player));
                })
                .build()
            );
        }

        List<IslandResourceGroup> groups = new ArrayList<>(manager.getResourceGroups().values());
        for (int i = 0; i < groups.size() && i < groupSlots.length; i++) {
            IslandResourceGroup grp = groups.get(i);
            int slot = groupSlots[i];

            String weightLore = MenuUtils.formatWeightLore(grp, this.weightFormat);

            NightItem item;
            if (grp.getIcon() != null) {
                item = NightItem.fromType(grp.getIcon());
                if (groupItemTemplate != null) {
                    item.setDisplayName(groupItemTemplate.getDisplayName());
                    item.setLore(groupItemTemplate.getLore());
                }
            } else {
                item = groupItemTemplate != null ? groupItemTemplate.copy() : NightItem.fromType(Material.GOLD_NUGGET);
            }
            item.hideAllComponents();
            item.replacement(replacer -> replacer
                .replace("%group_id%", grp.getId())
                .replace("%group_name%", grp.getName())
                .replace("%weight_lore%", weightLore)
            );

            viewer.addItem(item.toMenuItem()
                .setPriority(10)
                .setSlots(slot)
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
        this.runNextTick(() -> this.manager.openMenu(viewer.getPlayer()));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.menuTitle = ConfigValue.create("Settings.Title", "Poids des Ressources").read(config);
        this.setTitle(menuTitle);

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, this::handleReturn));
        loader.addHandler("back-profile", (viewer, event) -> {
            this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
        });
        loader.addHandler("back_profile", (viewer, event) -> {
            this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
        });

        this.weightFormat = ConfigValue.create("Weight_Format", Config.ISLAND_WEIGHT_FORMAT.get()).read(config);

        String slotsStr = config.getString("Group_Slots", "10,11,12,13,14,15,16,19,20,21,22,23,24,25");
        this.groupSlots = parseSlots(slotsStr);

        this.groupItemTemplate = ConfigValue.create("Group_Item.Item", NightItem.fromType(Material.GOLD_NUGGET)).read(config);
        if (config.contains("Group_Item") && !config.contains("Group_Item.Item")) {
            this.groupItemTemplate = ConfigValue.create("Group_Item", NightItem.fromType(Material.GOLD_NUGGET)).read(config);
        }

        this.returnItemTemplate = ConfigValue.create("Return_Item.Item", NightItem.fromType(Material.ARROW)).read(config);
        if (config.contains("Return_Item") && !config.contains("Return_Item.Item")) {
            this.returnItemTemplate = ConfigValue.create("Return_Item", NightItem.fromType(Material.ARROW)).read(config);
        }
        this.returnSlot = config.getInt("Return_Item.slot", 40);
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
