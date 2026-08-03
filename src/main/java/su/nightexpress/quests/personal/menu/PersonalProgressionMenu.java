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
import su.nightexpress.nightcore.ui.menu.type.LinkedMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.personal.PersonalQuestManager;
import su.nightexpress.quests.personal.data.PersonalQuestData;
import su.nightexpress.quests.personal.definition.RpgCategory;
import su.nightexpress.quests.user.QuestUser;
import su.nightexpress.quests.util.MenuUtils;

import java.util.*;

public class PersonalProgressionMenu extends LinkedMenu<QuestsPlugin, RpgCategory> implements ConfigBased {

    private final PersonalQuestManager manager;
    private String menuTitle = "Progression: %category_name%";

    private String activeName = "&eQuête active: %objective%";
    private List<String> activeLore = Arrays.asList(
        "&7Objectif: &f%objective%",
        "&7Progression: &e%progress%/%required%",
        "&7Récompense: &a%money% $",
        "&7Barre de progression:",
        "%progress_bar%"
    );
    private String activeMaterial = "WRITABLE_BOOK";

    private String noQuestName = "&cAucune quête active";
    private List<String> noQuestLore = Arrays.asList(
        "&7Vous n'avez pas de quête en cours.",
        "&7Retournez au menu principal pour en accepter une."
    );
    private String noQuestMaterial = "BARRIER";

    private String levelProgressionName = "&eClasse: %category_name%";
    private List<String> levelProgressionLore = Arrays.asList(
        "&7Niveau actuel: &e%level%",
        "&7Quêtes terminées au niveau actuel: &a%completions%/%required_completions%",
        "%level_progress_bar%"
    );
    private String levelProgressionMaterial = "EXPERIENCE_BOTTLE";

    private String abandonName = "&cAbandonner la quête";
    private List<String> abandonLore = Arrays.asList(
        "&7Cliquez ici pour abandonner la quête actuelle.",
        "&cAttention: vos progrès seront perdus !"
    );
    private String abandonMaterial = "REDSTONE";

    public PersonalProgressionMenu(@NotNull QuestsPlugin plugin, @NotNull PersonalQuestManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Progression: %category_name%");
        this.manager = manager;
        this.setAutoRefreshInterval(1);
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
        PersonalQuestData activeQuest = user.getPersonalQuestData().get(category.getId());
        int level = user.getRPGLevel(category.getId());
        int completions = (int) user.getRPGXP(category.getId());
        int completionsToLevelUp = category.getCompletionsToLevelUp();

        // 1. Level progression item
        Material lpMat = Material.EXPERIENCE_BOTTLE;
        try {
            lpMat = Material.valueOf(levelProgressionMaterial.toUpperCase());
        } catch (Exception ignored) {}

        String lpTitle = levelProgressionName.replace("%category_name%", category.getDisplayName());
        List<String> lpLoreList = new ArrayList<>();
        double levelPercent = (double) completions / completionsToLevelUp;
        String levelBar = MenuUtils.buildProgressBar(levelPercent);

        for (String line : levelProgressionLore) {
            lpLoreList.add(line
                .replace("%level%", String.valueOf(level))
                .replace("%completions%", String.valueOf(completions))
                .replace("%required_completions%", String.valueOf(completionsToLevelUp))
                .replace("%level_progress_bar%", levelBar)
            );
        }

        NightItem lpItem = NightItem.fromType(lpMat)
            .setDisplayName(lpTitle)
            .setLore(lpLoreList)
            .hideAllComponents();

        viewer.addItem(lpItem.toMenuItem().setSlots(13).setPriority(Integer.MAX_VALUE).build());

        // 2. Active Quest or No Quest Item
        if (activeQuest != null && activeQuest.hasActiveQuest()) {
            Material actMat = Material.WRITABLE_BOOK;
            try {
                actMat = Material.valueOf(activeMaterial.toUpperCase());
            } catch (Exception ignored) {}

            String objectiveIdStr = activeQuest.getObjectiveId() != null ? activeQuest.getObjectiveId() : "";
            String actTitle = activeName.replace("%objective%", objectiveIdStr);
            List<String> actLoreList = new ArrayList<>();
            double progressPercent = (double) activeQuest.getProgress() / activeQuest.getRequiredAmount();
            String progressBar = MenuUtils.buildProgressBar(progressPercent);

            for (String line : activeLore) {
                actLoreList.add(line
                    .replace("%objective%", objectiveIdStr)
                    .replace("%progress%", MenuUtils.formatNumber(activeQuest.getProgress()))
                    .replace("%required%", MenuUtils.formatNumber(activeQuest.getRequiredAmount()))
                    .replace("%money%", MenuUtils.formatNumber(activeQuest.getScaledMoney()))
                    .replace("%progress_bar%", progressBar)
                );
            }

            NightItem actItem = NightItem.fromType(actMat)
                .setDisplayName(actTitle)
                .setLore(actLoreList)
                .hideAllComponents();

            viewer.addItem(actItem.toMenuItem().setSlots(22).setPriority(Integer.MAX_VALUE).build());

            // 3. Abandon Quest Item
            Material abMat = Material.REDSTONE;
            try {
                abMat = Material.valueOf(abandonMaterial.toUpperCase());
            } catch (Exception ignored) {}

            NightItem abItem = NightItem.fromType(abMat)
                .setDisplayName(abandonName)
                .setLore(abandonLore)
                .hideAllComponents();

            viewer.addItem(abItem.toMenuItem()
                .setSlots(31)
                .setPriority(Integer.MAX_VALUE)
                .setHandler((viewer1, event) -> {
                    user.getPersonalQuestData().remove(category.getId());
                    this.plugin.getUserManager().save(user);
                    player.sendMessage("§cQuête abandonnée !");
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.5f, 1.0f);
                    this.runNextTick(() -> this.manager.openPersonalMenu(player));
                })
                .build()
            );

        } else {
            Material noMat = Material.BARRIER;
            try {
                noMat = Material.valueOf(noQuestMaterial.toUpperCase());
            } catch (Exception ignored) {}
            NightItem noItem = NightItem.fromType(noMat)
                .setDisplayName(noQuestName)
                .setLore(noQuestLore)
                .hideAllComponents();

            viewer.addItem(noItem.toMenuItem().setSlots(22).setPriority(Integer.MAX_VALUE).build());
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
        this.menuTitle = ConfigValue.create("Settings.Title", "Progression").read(config);
        this.setTitle(menuTitle);

        this.activeName = ConfigValue.create("active.name", activeName).read(config);
        this.activeLore = ConfigValue.create("active.lore", activeLore).read(config);
        this.activeMaterial = ConfigValue.create("active.material", activeMaterial).read(config);

        this.noQuestName = ConfigValue.create("no_quest.name", noQuestName).read(config);
        this.noQuestLore = ConfigValue.create("no_quest.lore", noQuestLore).read(config);
        this.noQuestMaterial = ConfigValue.create("no_quest.material", noQuestMaterial).read(config);

        this.levelProgressionName = ConfigValue.create("level_progression.name", levelProgressionName).read(config);
        this.levelProgressionLore = ConfigValue.create("level_progression.lore", levelProgressionLore).read(config);
        this.levelProgressionMaterial = ConfigValue.create("level_progression.material", levelProgressionMaterial).read(config);

        this.abandonName = ConfigValue.create("abandon.name", abandonName).read(config);
        this.abandonLore = ConfigValue.create("abandon.lore", abandonLore).read(config);
        this.abandonMaterial = ConfigValue.create("abandon.material", abandonMaterial).read(config);

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, (viewer1, event) -> {
            this.runNextTick(() -> this.manager.openPersonalMenu(viewer1.getPlayer()));
        }));

        loader.addDefaultItem(NightItem.fromType(Material.BLACK_STAINED_GLASS_PANE)
            .setHideTooltip(true)
            .toMenuItem()
            .setPriority(-1)
            .setSlots(0, 1, 2, 3, 4, 5, 6, 7, 8, 36, 37, 38, 39, 41, 42, 43, 44)
        );

        loader.addDefaultItem(NightItem.fromType(Material.GRAY_STAINED_GLASS_PANE)
            .setHideTooltip(true)
            .toMenuItem()
            .setPriority(-1)
            .setSlots(java.util.stream.IntStream.range(9, 36).toArray())
        );
    }
}
