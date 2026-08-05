package su.nightexpress.quests.personal.menu;

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
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.personal.PersonalQuestManager;
import su.nightexpress.quests.personal.definition.RpgCategory;
import su.nightexpress.quests.user.QuestUser;
import su.nightexpress.quests.util.MenuUtils;

import java.util.*;

public class PersonalCategoriesMenu extends LinkedMenu<QuestsPlugin, RpgCategory> implements ConfigBased {

    private final PersonalQuestManager manager;
    private int[] defaultSlots = new int[0];
    private Map<Integer, int[]> slotsByObjectiveCount = new HashMap<>();

    private String menuTitle = "Objectifs: %category_name%";
    private String itemDisplayName = "&d%material_name%";
    private List<String> itemLore = Collections.singletonList("Quantité requise: %material_quantite%");

    public PersonalCategoriesMenu(@NotNull QuestsPlugin plugin, @NotNull PersonalQuestManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Objectifs: %category_name%");
        this.manager = manager;
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        RpgCategory category = this.getLink(viewer);
        return super.getTitle(viewer).replace("%category_name%", category != null ? category.getDisplayName() : "");
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        RpgCategory category = this.getLink(viewer);
        if (category == null) return;

        QuestUser user = this.plugin.getUserManager().getOrFetch(player);
        int level = user.getRPGLevel(category.getId());

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(category.getObjectives().entrySet());
        int count = entries.size();
        int[] slots = this.slotsByObjectiveCount.get(count);
        if (slots == null || slots.length == 0) {
            slots = this.defaultSlots.length > 0 ? this.defaultSlots : new int[]{20, 21, 22, 23, 24};
        }

        for (int i = 0; i < slots.length && i < entries.size(); i++) {
            int slot = slots[i];
            Map.Entry<String, Integer> entry = entries.get(i);

            String objectiveId = entry.getKey();
            int baseAmount = entry.getValue();
            int scaledAmount = baseAmount * level * level;
            double scaledMoney = category.getBaseMoney() * level * level;

            String formattedAmount = MenuUtils.formatNumber(scaledAmount);
            String formattedMoney = MenuUtils.formatNumber(scaledMoney);

            Material mat = getObjectiveMaterial(objectiveId);

            String finalTitle = itemDisplayName
                .replace("%material_name%", objectiveId)
                .replace("%objective%", objectiveId)
                .replace("%material%", objectiveId)
                .replace("%material_quantite%", formattedAmount)
                .replace("%amount%", formattedAmount)
                .replace("%required%", formattedAmount)
                .replace("%money%", formattedMoney)
                .replace("%level%", String.valueOf(level))
                .replace("%category_name%", category.getDisplayName());

            List<String> finalLore = new ArrayList<>();
            for (String line : this.itemLore) {
                finalLore.add(line
                    .replace("%material_name%", objectiveId)
                    .replace("%objective%", objectiveId)
                    .replace("%material%", objectiveId)
                    .replace("%material_quantite%", formattedAmount)
                    .replace("%amount%", formattedAmount)
                    .replace("%required%", formattedAmount)
                    .replace("%money%", formattedMoney)
                    .replace("%level%", String.valueOf(level))
                    .replace("%category_name%", category.getDisplayName())
                );
            }

            NightItem nightItem = NightItem.fromType(mat)
                .setDisplayName(finalTitle)
                .setLore(finalLore)
                .hideAllComponents();

            viewer.addItem(nightItem.toMenuItem().setSlots(slot).setPriority(Integer.MAX_VALUE).build());
        }
    }

    private Material getObjectiveMaterial(String objectiveId) {
        String upper = objectiveId.toUpperCase();
        try {
            return Material.valueOf(upper);
        } catch (Exception ignored) {
            if (upper.equals("CARROTS")) return Material.CARROT;
            if (upper.equals("POTATOES")) return Material.POTATO;
            if (upper.equals("BEETROOTS")) return Material.BEETROOT;

            if (upper.equals("BLOCKS") || upper.equals("BLOCK") || upper.equals("ANY") || upper.equals("ANY_BLOCK") || upper.equals("ALL") || upper.equals("BUILDING_BLOCKS")) return Material.BRICK;

            if (upper.equals("PLANKS") || upper.equals("PLANK")) return Material.OAK_PLANKS;
            if (upper.equals("STAIRS") || upper.equals("STAIR")) return Material.OAK_STAIRS;
            if (upper.equals("SLAB") || upper.equals("SLABS")) return Material.OAK_SLAB;
            if (upper.equals("LOG") || upper.equals("LOGS") || upper.equals("WOOD")) return Material.OAK_LOG;
            if (upper.equals("TOOL") || upper.equals("TOOLS")) return Material.IRON_PICKAXE;
            if (upper.equals("ARMOR") || upper.equals("ARMOUR")) return Material.IRON_CHESTPLATE;
            if (upper.equals("WOOL")) return Material.WHITE_WOOL;
            if (upper.equals("BED") || upper.equals("BEDS")) return Material.RED_BED;
            if (upper.equals("GLASS")) return Material.GLASS;
            if (upper.equals("GLASS_PANE") || upper.equals("GLASS_PANES")) return Material.GLASS_PANE;
            if (upper.equals("TERRACOTTA")) return Material.TERRACOTTA;
            if (upper.equals("CONCRETE")) return Material.WHITE_CONCRETE;
            if (upper.equals("CANDLE") || upper.equals("CANDLES")) return Material.CANDLE;
            if (upper.equals("SHULKER_BOX") || upper.equals("SHULKER_BOXES")) return Material.SHULKER_BOX;

            try {
                return Material.valueOf(upper + "_SPAWN_EGG");
            } catch (Exception ignored2) {
                return Material.ENCHANTED_BOOK;
            }
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
        this.menuTitle = ConfigValue.create("Settings.Title", "Objectifs: %category_name%").read(config);
        this.setTitle(menuTitle);

        this.itemDisplayName = ConfigValue.create("Quest.Item.Display_Name", "&d%material_name%").read(config);
        this.itemLore = ConfigValue.create("Quest.Item.Lore", Collections.singletonList("Quantité requise: %material_quantite%")).read(config);

        this.defaultSlots = MenuUtils.parseSlots(config.getString("Quest.Slots", ""));
        this.slotsByObjectiveCount = MenuUtils.loadSlotsByCount(config, "Quest");

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, (viewer1, event) -> {
            this.runNextTick(() -> this.manager.openPersonalMenu(viewer1.getPlayer()));
        }));
    }
}
