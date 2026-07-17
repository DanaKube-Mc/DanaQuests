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
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.personal.PersonalQuestManager;
import su.nightexpress.quests.personal.data.PersonalQuestData;
import su.nightexpress.quests.personal.definition.RpgCategory;
import su.nightexpress.quests.user.QuestUser;

import java.util.*;

public class PersonalQuestMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private final PersonalQuestManager manager;
    private final TreeMap<Integer, int[]> slotsByCategoryCount = new TreeMap<>();

    private String menuTitle = "Quêtes RPG";
    private String categoryLoreSuffixActive = "\n&6Quête active: &e%objective%\n&7Progression: &e%progress%/%required%\n&7Gains: &a%money% $\n&eClic Gauche pour voir la progression.";
    private String categoryLoreSuffixInactive = "\n&aAucune quête active.\n&eClic Gauche pour accepter une quête.";

    public PersonalQuestMenu(@NotNull QuestsPlugin plugin, @NotNull PersonalQuestManager manager) {
        super(plugin, MenuType.GENERIC_9X5, "Quêtes RPG");
        this.manager = manager;
        this.setAutoRefreshInterval(1);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        QuestUser user = this.plugin.getUserManager().getOrFetch(player);

        List<RpgCategory> categories = new ArrayList<>(this.manager.getCategories().values());
        int count = categories.size();
        int[] slots = Optional.ofNullable(this.slotsByCategoryCount.ceilingEntry(count)).map(Map.Entry::getValue).orElse(new int[0]);

        for (int i = 0; i < slots.length && i < categories.size(); i++) {
            int slot = slots[i];
            RpgCategory category = categories.get(i);

            int level = user.getRPGLevel(category.getId());
            int completions = (int) user.getRPGXP(category.getId());
            int completionsToLevelUp = category.getCompletionsToLevelUp();
            PersonalQuestData activeQuest = user.getPersonalQuestData().get(category.getId());

            Material mat = null;
            try {
                mat = Material.valueOf(category.getIconMaterial().toUpperCase());
            } catch (Exception ignored) {
                mat = Material.BOOK;
            }

            String finalTitle = category.getDisplayName()
                .replace("%level%", String.valueOf(level))
                .replace("%completions%", String.valueOf(completions))
                .replace("%required_completions%", String.valueOf(completionsToLevelUp));

            List<String> finalLore = new ArrayList<>();
            for (String line : category.getIconLore()) {
                finalLore.add(line
                    .replace("%level%", String.valueOf(level))
                    .replace("%completions%", String.valueOf(completions))
                    .replace("%required_completions%", String.valueOf(completionsToLevelUp))
                );
            }

            int acceptedToday = this.manager.getQuestsAcceptedToday(user);
            int dailyLimit = this.manager.getDailyLimit(player);

            finalLore.add("");
            finalLore.add("&7Limite journalière: &e" + acceptedToday + "/" + dailyLimit);

            if (activeQuest != null) {
                String objectiveName = activeQuest.getObjectiveId();
                String suffix = categoryLoreSuffixActive
                    .replace("%objective%", objectiveName)
                    .replace("%progress%", String.valueOf(activeQuest.getProgress()))
                    .replace("%required%", String.valueOf(activeQuest.getRequiredAmount()))
                    .replace("%money%", String.valueOf(activeQuest.getScaledMoney()));
                finalLore.addAll(Arrays.asList(suffix.split("\n")));
            } else {
                finalLore.addAll(Arrays.asList(categoryLoreSuffixInactive.split("\n")));
            }
            finalLore.add("&7Clic Droit pour voir les objectifs.");

            NightItem nightItem = NightItem.fromType(mat)
                .setDisplayName(finalTitle)
                .setLore(finalLore)
                .hideAllComponents();

            if (category.getIconCustomModelData() > 0) {
                nightItem.setCustomModelData((float) category.getIconCustomModelData());
            }

            MenuItem item = nightItem.toMenuItem()
                .setSlots(slot)
                .setPriority(Integer.MAX_VALUE)
                .setHandler((viewer1, event) -> {
                    if (event.isLeftClick()) {
                        if (activeQuest == null) {
                            if (this.manager.acceptQuest(player, category)) {
                                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                this.runNextTick(() -> this.flush(viewer1));
                            }
                        } else {
                            this.runNextTick(() -> this.manager.openProgressionMenu(player, category));
                        }
                    } else if (event.isRightClick()) {
                        this.runNextTick(() -> this.manager.openCategoriesMenu(player, category));
                    }
                })
                .build();

            viewer.addItem(item);
        }
    }

    @Override
    protected void onReady(@NotNull MenuViewer viewer, @NotNull Inventory inventory) {
    }

    private void handleReturn(@NotNull MenuViewer viewer, @NotNull org.bukkit.event.inventory.InventoryClickEvent event) {
        this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.menuTitle = ConfigValue.create("Settings.Title", "Quêtes RPG").read(config);
        this.setTitle(menuTitle);

        this.categoryLoreSuffixActive = ConfigValue.create("status.active.suffix", categoryLoreSuffixActive).read(config);
        this.categoryLoreSuffixInactive = ConfigValue.create("status.inactive.suffix", categoryLoreSuffixInactive).read(config);

        this.slotsByCategoryCount.clear();
        for (int count = 0; count < 10; count++) {
            int amount = count + 1;
            int[] defSlots = getDefaultSlots(amount);
            int[] skillSlots = ConfigValue.create("Quest.SlotsByCount." + amount, defSlots).read(config);
            this.slotsByCategoryCount.put(amount, skillSlots);
        }

        loader.addDefaultItem(MenuItem.buildReturn(this, 40, this::handleReturn));

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
