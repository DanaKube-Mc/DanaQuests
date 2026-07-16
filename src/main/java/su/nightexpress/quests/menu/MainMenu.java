package su.nightexpress.quests.menu;

import org.bukkit.Material;
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
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;

public class MainMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private NightItem dailyItem;
    private int[] dailySlots;

    private NightItem milestonesItem;
    private int[] milestonesSlots;

    private NightItem loreItem;
    private int[] loreSlots;

    public MainMenu(@NotNull QuestsPlugin plugin) {
        super(plugin, MenuType.GENERIC_9X5, "Quests");
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();

        if (dailyItem != null && dailySlots != null && Config.FEATURES_QUESTS_ENABLED.get()) {
            viewer.addItem(dailyItem.toMenuItem()
                .setPriority(10)
                .setSlots(dailySlots)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> this.plugin.questManager().ifPresent(manager -> manager.openQuests(player)));
                })
                .build());
        }

        if (milestonesItem != null && milestonesSlots != null && Config.FEATURES_MILESTONES_ENABLED.get()) {
            viewer.addItem(milestonesItem.toMenuItem()
                .setPriority(10)
                .setSlots(milestonesSlots)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> this.plugin.milestoneManager().ifPresent(manager -> manager.openCategories(player)));
                })
                .build());
        }

        if (loreItem != null && loreSlots != null && Config.FEATURES_LORE_ENABLED.get()) {
            viewer.addItem(loreItem.toMenuItem()
                .setPriority(10)
                .setSlots(loreSlots)
                .setHandler((viewer1, event) -> {
                    this.runNextTick(() -> this.plugin.loreManager().ifPresent(manager -> manager.openLoreMenu(player)));
                })
                .build());
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        String title = config.getString("Settings.Title", "Quests");
        this.setTitle(title);

        this.dailyItem = ConfigValue.create("Types.SlotsByCount.DailyQuests.Item", NightItem.fromType(Material.KNOWLEDGE_BOOK)).read(config);
        String dailySlotsStr = config.getString("Types.SlotsByCount.DailyQuests.Slots", "22");
        this.dailySlots = parseSlots(dailySlotsStr);

        this.milestonesItem = ConfigValue.create("Types.SlotsByCount.Milestones.Item", NightItem.fromType(Material.KNOWLEDGE_BOOK)).read(config);
        String milestonesSlotsStr = config.getString("Types.SlotsByCount.Milestones.Slots", "21");
        this.milestonesSlots = parseSlots(milestonesSlotsStr);

        this.loreItem = ConfigValue.create("Types.SlotsByCount.LoreQuests.Item", NightItem.fromType(Material.KNOWLEDGE_BOOK)).read(config);
        String loreSlotsStr = config.getString("Types.SlotsByCount.LoreQuests.Slots", "23");
        this.loreSlots = parseSlots(loreSlotsStr);
    }

    private int[] parseSlots(String slotsStr) {
        if (slotsStr == null || slotsStr.trim().isEmpty()) {
            return new int[0];
        }
        try {
            String[] split = slotsStr.split(",");
            int[] slots = new int[split.length];
            for (int i = 0; i < split.length; i++) {
                slots[i] = Integer.parseInt(split[i].trim());
            }
            return slots;
        } catch (Exception e) {
            return new int[0];
        }
    }
}
