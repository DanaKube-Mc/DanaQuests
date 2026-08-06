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
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.island.IslandManager;
import su.nightexpress.quests.island.definition.IslandResourceGroup;
import su.nightexpress.quests.util.MenuUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IslandResourceGroupMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private final IslandManager manager;

    private String menuTitle = "Poids des Ressources";
    private Map<Integer, int[]> groupSlotsByCount = new HashMap<>();
    private NightItem groupItemTemplate;
    private String weightFormat;

    public IslandResourceGroupMenu(@NotNull QuestsPlugin plugin, @NotNull IslandManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Poids des Ressources");
        this.manager = manager;
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {

        List<IslandResourceGroup> groups = new ArrayList<>(manager.getResourceGroups().values());
        int[] groupSlots = this.groupSlotsByCount.getOrDefault(groups.size(), new int[0]);
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

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.menuTitle = ConfigValue.create("Settings.Title", "Poids des Ressources").read(config);
        this.setTitle(menuTitle);

        this.weightFormat = ConfigValue.create("Weight_Format", Config.ISLAND_WEIGHT_FORMAT.get()).read(config);

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, (viewer1, event) -> {
            this.runNextTick(() -> manager.openMenu(viewer1.getPlayer()));
        }));

        this.groupSlotsByCount = MenuUtils.loadSlotsByCount(config, "Group_Slots");

        this.groupItemTemplate = ConfigValue.create("Group_Item.Item", NightItem.fromType(Material.GOLD_NUGGET)).read(config);
        if (config.contains("Group_Item") && !config.contains("Group_Item.Item")) {
            this.groupItemTemplate = ConfigValue.create("Group_Item", NightItem.fromType(Material.GOLD_NUGGET)).read(config);
        }
    }
}
