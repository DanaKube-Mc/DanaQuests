package su.nightexpress.quests.lore.menu;

import org.bukkit.Material;
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
import su.nightexpress.nightcore.ui.menu.data.Filled;
import su.nightexpress.nightcore.ui.menu.data.MenuFiller;
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.lore.LoreManager;
import su.nightexpress.quests.lore.definition.LoreObjective;
import su.nightexpress.quests.lore.definition.LoreQuest;
import su.nightexpress.quests.lore.definition.LoreQuestCategory;
import su.nightexpress.quests.user.QuestUser;

import java.util.*;
import java.util.stream.IntStream;

public class LoreProgressionMenu extends LinkedMenu<QuestsPlugin, LoreQuestCategory> implements ConfigBased, Filled<LoreQuest> {

    private final LoreManager manager;
    private final TreeMap<Integer, int[]> slotsByQuestCount = new TreeMap<>();

    public LoreProgressionMenu(@NotNull QuestsPlugin plugin, @NotNull LoreManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Progression");
        this.manager = manager;
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        LoreQuestCategory category = this.getLink(viewer);
        return super.getTitle(viewer).replace("%lore_category_name%", category.getName());
    }

    @Override
    @NotNull
    public MenuFiller<LoreQuest> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        LoreQuestCategory category = this.getLink(player);
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);

        List<LoreQuest> quests = category.getQuests();
        int count = quests.size();
        int[] slots = Optional.ofNullable(this.slotsByQuestCount.ceilingEntry(count)).map(Map.Entry::getValue).orElse(new int[0]);

        return MenuFiller.builder(this)
            .setSlots(slots)
            .setItems(quests)
            .setItemCreator(quest -> {
                String statusStr;
                if (user.hasCompletedLore(quest.getId())) {
                    statusStr = "&aComplétée";
                } else {
                    LoreQuest active = this.manager.getActiveQuest(user, category);
                    if (active != null && active.getId().equals(quest.getId())) {
                        statusStr = "&eEn cours";
                    } else {
                        statusStr = "&cVerrouillée";
                    }
                }

                List<String> objectivesFormatted = new ArrayList<>();
                for (LoreObjective obj : quest.getObjectives()) {
                    int current = this.manager.getObjectiveProgress(user, quest, obj);
                    String color = current >= obj.getRequired() ? "&a" : "&7";
                    objectivesFormatted.add("  " + color + "- " + obj.getDescription() + " &8(" + current + "/" + obj.getRequired() + ")");
                }

                Material material = Material.CHEST;
                try {
                    material = Material.valueOf(quest.getIconMaterial().toUpperCase());
                } catch (Exception ignored) {}

                NightItem icon = NightItem.fromType(material)
                    .setDisplayName(quest.getName())
                    .hideAllComponents();

                if (quest.getIconCustomModelData() > 0) {
                    icon.setCustomModelData((float) quest.getIconCustomModelData());
                }

                List<String> finalLore = new ArrayList<>();
                for (String line : quest.getIconLore()) {
                    if (line.contains("%quest_objectives%")) {
                        finalLore.addAll(objectivesFormatted);
                    } else {
                        finalLore.add(line
                            .replace("%status%", statusStr)
                            .replace("%quest_name%", quest.getName())
                        );
                    }
                }
                icon.setLore(finalLore);

                return icon;
            })
            .build();
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        this.autoFill(viewer);
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
        this.runNextTick(() -> this.manager.openCategories(viewer.getPlayer()));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        String title = ConfigValue.create("Settings.Title", "<yellow><b>%lore_category_name%</b></yellow>").read(config);
        this.setTitle(title);

        this.slotsByQuestCount.clear();
        for (int count = 0; count < 10; count++) {
            int amount = count + 1;
            int[] defSlots = getDefaultSlots(amount);
            int[] skillSlots = ConfigValue.create("Quest.SlotsByCount." + amount, defSlots).read(config);
            this.slotsByQuestCount.put(amount, skillSlots);
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
