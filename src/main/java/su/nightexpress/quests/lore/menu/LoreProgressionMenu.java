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
import su.nightexpress.quests.util.MenuUtils;

import java.util.*;

public class LoreProgressionMenu extends LinkedMenu<QuestsPlugin, LoreQuestCategory> implements ConfigBased, Filled<LoreQuest> {

    private final LoreManager manager;
    private Map<Integer, int[]> slotsByQuestCount = new HashMap<>();

    private String questItemDisplayName = "%status_name%";
    private List<String> questItemLore = Collections.singletonList("%status_lore%");

    private String lockedMaterialStr = "RED_STAINED_GLASS_PANE";
    private int lockedCustomModelData = 0;
    private String lockedName = "<lyellow>%quest_name% %status%";
    private List<String> lockedLore = Collections.singletonList("Vous devez finir les quete precendete");

    private String finishedMaterialStr = "GREEN_STAINED_GLASS_PANE";
    private int finishedCustomModelData = 0;
    private String finishedName = "<lyellow>%quest_name% %status%";
    private List<String> finishedLore = Collections.singletonList("Quete fini");

    private String activeMaterialStr = "ORANGE_STAINED_GLASS_PANE";
    private int activeCustomModelData = 0;
    private String activeName = "<lyellow>%quest_name% %status%";
    private List<String> activeLore = Arrays.asList("%quest_lore%", "", "%quest_reward%");

    public LoreProgressionMenu(@NotNull QuestsPlugin plugin, @NotNull LoreManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Progression");
        this.manager = manager;
        this.setAutoRefreshInterval(1);
    }

    @Override
    @NotNull
    protected String getTitle(@NotNull MenuViewer viewer) {
        LoreQuestCategory category = this.getLink(viewer);
        return super.getTitle(viewer).replace("%lore_category_name%", category != null ? category.getName() : "");
    }

    @Override
    @NotNull
    public MenuFiller<LoreQuest> createFiller(@NotNull MenuViewer viewer) {
        Player player = viewer.getPlayer();
        LoreQuestCategory category = this.getLink(player);
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);

        List<LoreQuest> quests = category != null ? category.getQuests() : Collections.emptyList();
        int count = quests.size();
        int[] slots = this.slotsByQuestCount.getOrDefault(count, new int[0]);

        return MenuFiller.builder(this)
            .setSlots(slots)
            .setItems(quests)
            .setItemCreator(quest -> {
                boolean isFinished = user.hasCompletedLore(quest.getId());
                LoreQuest active = category != null ? this.manager.getActiveQuest(user, category) : null;
                boolean isActive = active != null && active.getId().equals(quest.getId());

                String materialStr;
                int cmd;
                String namePattern;
                List<String> lorePattern;
                String statusTag;

                if (isFinished) {
                    materialStr = finishedMaterialStr;
                    cmd = finishedCustomModelData;
                    namePattern = finishedName;
                    lorePattern = finishedLore;
                    statusTag = "&a[Complétée]";
                } else if (isActive) {
                    materialStr = activeMaterialStr;
                    cmd = activeCustomModelData;
                    namePattern = activeName;
                    lorePattern = activeLore;
                    statusTag = "&e[En cours]";
                } else {
                    materialStr = lockedMaterialStr;
                    cmd = lockedCustomModelData;
                    namePattern = lockedName;
                    lorePattern = lockedLore;
                    statusTag = "&c[Verrouillée]";
                }

                Material material = null;
                if (materialStr != null && !materialStr.isEmpty()) {
                    try {
                        material = Material.valueOf(materialStr.toUpperCase());
                    } catch (Exception ignored) {}
                }
                if (material == null) {
                    try {
                        material = Material.valueOf(quest.getIconMaterial().toUpperCase());
                    } catch (Exception ignored) {
                        material = Material.PAPER;
                    }
                }

                List<String> objectivesFormatted = new ArrayList<>();
                for (LoreObjective obj : quest.getObjectives()) {
                    int current = this.manager.getObjectiveProgress(user, quest, obj);
                    String color = current >= obj.getRequired() ? "&a" : "&7";
                    objectivesFormatted.add("  " + color + "- " + obj.getDescription() + " &8(" + current + "/" + obj.getRequired() + ")");
                }

                List<String> rewardsFormatted = new ArrayList<>();
                for (String rw : quest.getRewards()) {
                    rewardsFormatted.add("&7Récompense: &a" + rw);
                }

                String statusName = namePattern
                    .replace("%quest_name%", quest.getName())
                    .replace("%status%", statusTag);

                List<String> statusLoreLines = new ArrayList<>();
                for (String line : lorePattern) {
                    if (line.contains("%quest_lore%")) {
                        statusLoreLines.addAll(quest.getDescription());
                    } else if (line.contains("%quest_objectives%")) {
                        statusLoreLines.addAll(objectivesFormatted);
                    } else if (line.contains("%quest_reward%") || line.contains("%rewards%")) {
                        statusLoreLines.addAll(rewardsFormatted);
                    } else {
                        statusLoreLines.add(line
                            .replace("%quest_name%", quest.getName())
                            .replace("%status%", statusTag)
                        );
                    }
                }

                String finalTitle = questItemDisplayName
                    .replace("%status_name%", statusName)
                    .replace("%quest_name%", quest.getName())
                    .replace("%status%", statusTag);

                List<String> finalLore = new ArrayList<>();
                for (String line : questItemLore) {
                    if (line.contains("%status_lore%")) {
                        finalLore.addAll(statusLoreLines);
                    } else {
                        finalLore.add(line
                            .replace("%status_name%", statusName)
                            .replace("%quest_name%", quest.getName())
                            .replace("%status%", statusTag)
                        );
                    }
                }

                NightItem icon = NightItem.fromType(material)
                    .setDisplayName(finalTitle)
                    .setLore(finalLore)
                    .hideAllComponents();

                if (cmd > 0) {
                    icon.setCustomModelData((float) cmd);
                } else if (quest.getIconCustomModelData() > 0) {
                    icon.setCustomModelData((float) quest.getIconCustomModelData());
                }

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
        this.runNextTick(() -> this.manager.openLoreMenu(viewer.getPlayer()));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        String title = ConfigValue.create("Settings.Title", "<yellow><b>%lore_category_name%</b></yellow>").read(config);
        this.setTitle(title);

        this.questItemDisplayName = ConfigValue.create("Quest.Item.Display_Name", "%status_name%").read(config);
        this.questItemLore = ConfigValue.create("Quest.Item.Lore", Collections.singletonList("%status_lore%")).read(config);

        this.slotsByQuestCount = MenuUtils.loadSlotsByCount(config, "Quest");

        this.lockedMaterialStr = ConfigValue.create("status.locked.material", "RED_STAINED_GLASS_PANE").read(config);
        this.lockedCustomModelData = ConfigValue.create("status.locked.custom_model_data", 0).read(config);
        this.lockedName = ConfigValue.create("status.locked.name", "<lyellow>%quest_name% %status%").read(config);
        this.lockedLore = ConfigValue.create("status.locked.lore", Collections.singletonList("Vous devez finir les quete precendete")).read(config);

        this.finishedMaterialStr = ConfigValue.create("status.finished.material", "GREEN_STAINED_GLASS_PANE").read(config);
        this.finishedCustomModelData = ConfigValue.create("status.finished.custom_model_data", 0).read(config);
        this.finishedName = ConfigValue.create("status.finished.name", "<lyellow>%quest_name% %status%").read(config);
        this.finishedLore = ConfigValue.create("status.finished.lore", Collections.singletonList("Quete fini")).read(config);

        this.activeMaterialStr = ConfigValue.create("status.actived.material", ConfigValue.create("status.active.material", "ORANGE_STAINED_GLASS_PANE").read(config)).read(config);
        this.activeCustomModelData = ConfigValue.create("status.actived.custom_model_data", ConfigValue.create("status.active.custom_model_data", 0).read(config)).read(config);
        this.activeName = ConfigValue.create("status.actived.name", ConfigValue.create("status.active.name", "<lyellow>%quest_name% %status%").read(config)).read(config);
        this.activeLore = ConfigValue.create("status.actived.lore", ConfigValue.create("status.active.lore", Arrays.asList("%quest_lore%", "", "%quest_reward%")).read(config)).read(config);

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, this::handleReturn));
    }
}
