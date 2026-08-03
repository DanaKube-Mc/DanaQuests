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

import java.util.*;

public class PersonalCategoriesMenu extends LinkedMenu<QuestsPlugin, RpgCategory> implements ConfigBased {

    private final PersonalQuestManager manager;
    private String menuTitle = "Objectifs: %category_name%";

    public PersonalCategoriesMenu(@NotNull QuestsPlugin plugin, @NotNull PersonalQuestManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Objectifs: %category_name%");
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
        int level = user.getRPGLevel(category.getId());

        int[] slots = {20, 21, 22, 23, 24};
        int slotIndex = 0;

        for (Map.Entry<String, Integer> entry : category.getObjectives().entrySet()) {
            if (slotIndex >= slots.length) break;
            int slot = slots[slotIndex++];

            String objectiveId = entry.getKey();
            int baseAmount = entry.getValue();
            int scaledAmount = baseAmount * level * level;
            double scaledMoney = category.getBaseMoney() * level * level;

            Material mat = getObjectiveMaterial(objectiveId);
            List<String> lore = Arrays.asList(
                "&7Objectif: &f" + objectiveId,
                "&7Quantité requise: &e" + scaledAmount,
                "&7Récompense d'argent: &a" + scaledMoney + " $",
                "&8(Mis à l'échelle pour votre niveau " + level + ")"
            );

            NightItem nightItem = NightItem.fromType(mat)
                .setDisplayName("&6" + objectiveId)
                .setLore(lore)
                .hideAllComponents();

            viewer.addItem(nightItem.toMenuItem().setSlots(slot).setPriority(Integer.MAX_VALUE).build());
        }
    }

    private Material getObjectiveMaterial(String objectiveId) {
        try {
            return Material.valueOf(objectiveId.toUpperCase());
        } catch (Exception ignored) {
            try {
                return Material.valueOf(objectiveId.toUpperCase() + "_SPAWN_EGG");
            } catch (Exception ignored2) {
                return Material.PAPER;
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

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, (viewer1, event) -> {
            this.runNextTick(() -> this.manager.openPersonalMenu(viewer1.getPlayer()));
        }));
        loader.addHandler("back-profile", (viewer, event) -> {
            this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
        });
        loader.addHandler("back_profile", (viewer, event) -> {
            this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
        });

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
