package su.nightexpress.quests.community.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import su.nightexpress.nightcore.ui.menu.data.MenuLoader;
import su.nightexpress.nightcore.ui.menu.type.NormalMenu;
import su.nightexpress.nightcore.ui.menu.item.MenuItem;
import su.nightexpress.nightcore.util.bukkit.NightItem;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.community.CommunityQuestManager;
import su.nightexpress.quests.community.data.CommunityActiveEvent;
import su.nightexpress.quests.community.definition.CommunityQuest;
import su.nightexpress.quests.util.MenuUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class CommunityLeaderboardMenu extends NormalMenu<QuestsPlugin> implements ConfigBased {

    private final CommunityQuestManager manager;

    private String menuTitle = "Quêtes Communautaires - Leaderboard";

    private int progressSlot = 4;
    private NightItem progressItemTemplate;

    private int contributeSlot = 49;
    private NightItem contributeItemTemplate;

    private int[] leaderboardSlots = new int[]{12, 13, 14, 21, 22, 23, 30, 31, 32, 40};
    private NightItem leaderboardItemTemplate;
    private NightItem emptyLeaderboardItemTemplate;

    public CommunityLeaderboardMenu(@NotNull QuestsPlugin plugin, @NotNull CommunityQuestManager manager) {
        super(plugin, MenuType.GENERIC_9X6, "Quêtes Communautaires - Leaderboard");
        this.manager = manager;
        this.setAutoRefreshInterval(1);
    }

    @Override
    protected void onPrepare(@NotNull MenuViewer viewer, @NotNull InventoryView view) {
        Player player = viewer.getPlayer();
        CommunityActiveEvent activeEvent = manager.getActiveEvent();

        if (activeEvent == null || !activeEvent.isActive()) {
            viewer.addItem(NightItem.fromType(Material.BARRIER)
                .setDisplayName("&c&lAucun Événement Communautaire Actif")
                .setLore(Arrays.asList("&7Revenez plus tard lorsqu'un événement aura été lancé."))
                .toMenuItem()
                .setPriority(10)
                .setSlots(22)
                .build()
            );
            return;
        }

        CommunityQuest quest = manager.getQuest(activeEvent.getQuestId());
        String questName = quest != null ? quest.getDisplayName() : activeEvent.getQuestId();
        String questDesc = quest != null && quest.getDescription() != null ? String.join(" ", quest.getDescription()) : "";

        double current = activeEvent.getCurrentAmount();
        double target = activeEvent.getTargetAmount();
        double percent = target > 0 ? (current * 100.0 / target) : 100.0;
        String progressBar = MenuUtils.buildProgressBar(target > 0 ? (current / target) : 1.0);

        long millisLeft = activeEvent.getEndTimestamp() - System.currentTimeMillis();
        String timeLeft = formatTimeLeft(millisLeft);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String startDate = sdf.format(new Date(activeEvent.getStartTimestamp()));
        String endDate = sdf.format(new Date(activeEvent.getEndTimestamp()));

        // Progress Item
        if (progressItemTemplate != null && progressSlot >= 0) {
            NightItem progItem = progressItemTemplate.copy()
                .hideAllComponents()
                .replacement(replacer -> replacer
                    .replace("%quest_name%", questName)
                    .replace("%quest_description%", questDesc)
                    .replace("%current%", String.format("%.0f", current))
                    .replace("%target%", String.format("%.0f", target))
                    .replace("%percent%", String.format("%.1f", percent))
                    .replace("%progress_bar%", progressBar)
                    .replace("%time_left%", timeLeft)
                    .replace("%start_date%", startDate)
                    .replace("%end_date%", endDate)
                );

            viewer.addItem(progItem.toMenuItem()
                .setPriority(10)
                .setSlots(progressSlot)
                .build()
            );
        }

        // Contribute Button
        if (contributeItemTemplate != null && contributeSlot >= 0) {
            double yourContribution = activeEvent.getContribution(player.getUniqueId());
            NightItem contribItem = contributeItemTemplate.copy()
                .hideAllComponents()
                .replacement(replacer -> replacer
                    .replace("%your_contribution%", String.format("%.0f", yourContribution))
                );

            viewer.addItem(contribItem.toMenuItem()
                .setPriority(10)
                .setSlots(contributeSlot)
                .setHandler((viewer1, event) -> {
                    if ("MONEY_DEPOSIT".equalsIgnoreCase(quest != null ? quest.getType() : "MONEY_DEPOSIT")) {
                        if (event.isShiftClick() && event.isRightClick()) {
                            manager.depositMax(player);
                        } else if (event.isRightClick()) {
                            manager.deposit(player, 100000.0);
                        } else {
                            manager.deposit(player, 10000.0);
                        }
                    }
                })
                .build()
            );
        }

        // Leaderboard Heads
        List<Map.Entry<UUID, Double>> rankings = manager.getTopContributors();
        for (int i = 0; i < leaderboardSlots.length; i++) {
            int slot = leaderboardSlots[i];
            int rank = i + 1;

            if (i < rankings.size()) {
                Map.Entry<UUID, Double> entry = rankings.get(i);
                UUID uuid = entry.getKey();
                double amount = entry.getValue();
                double share = current > 0 ? (amount * 100.0 / current) : 0.0;

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
                String pName = offlinePlayer.getName() != null ? offlinePlayer.getName() : uuid.toString().substring(0, 8);

                NightItem headItem = leaderboardItemTemplate.copy()
                    .hideAllComponents()
                    .replacement(replacer -> replacer
                        .replace("%rank%", String.valueOf(rank))
                        .replace("%player%", pName)
                        .replace("%amount%", String.format("%.0f", amount))
                        .replace("%share%", String.format("%.1f", share))
                    )
                    .setSkullOwner(offlinePlayer);

                MenuItem menuItem = headItem.toMenuItem()
                    .setPriority(10)
                    .setSlots(slot)
                    .build();

                viewer.addItem(menuItem);
            } else if (emptyLeaderboardItemTemplate != null) {
                NightItem emptyItem = emptyLeaderboardItemTemplate.copy()
                    .hideAllComponents()
                    .replacement(replacer -> replacer
                        .replace("%rank%", String.valueOf(rank))
                    );

                viewer.addItem(emptyItem.toMenuItem()
                    .setPriority(10)
                    .setSlots(slot)
                    .build()
                );
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

    private void handleReturn(@NotNull MenuViewer viewer, @NotNull InventoryClickEvent event) {
        this.runNextTick(() -> this.plugin.mainMenu().ifPresent(menu -> menu.open(viewer.getPlayer())));
    }

    @Override
    public void loadConfiguration(@NotNull FileConfig config, @NotNull MenuLoader loader) {
        this.menuTitle = ConfigValue.create("Settings.Title", "Événement Communautaire").read(config);
        this.setTitle(menuTitle);

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

        this.progressSlot = config.getInt("Progress_Item.slot", 4);
        this.progressItemTemplate = ConfigValue.create("Progress_Item.Item", NightItem.fromType(Material.NETHER_STAR)).read(config);

        this.contributeSlot = config.getInt("Contribute_Button.slot", 49);
        this.contributeItemTemplate = ConfigValue.create("Contribute_Button.Item", NightItem.fromType(Material.GOLD_NUGGET)).read(config);

        String slotsStr = config.getString("Leaderboard_Slots", "12,13,14,21,22,23,30,31,32,40");
        this.leaderboardSlots = parseSlots(slotsStr);

        this.leaderboardItemTemplate = ConfigValue.create("Leaderboard_Item.Item", NightItem.fromType(Material.PLAYER_HEAD)).read(config);
        this.emptyLeaderboardItemTemplate = ConfigValue.create("Empty_Leaderboard_Item.Item", NightItem.fromType(Material.BARRIER)).read(config);
    }

    public static String formatTimeLeft(long millis) {
        if (millis <= 0) return "00:00:00";
        long seconds = millis / 1000;
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
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
