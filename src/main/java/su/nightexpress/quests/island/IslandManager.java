package su.nightexpress.quests.island;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.hook.SkyblockHookManager;
import su.nightexpress.quests.island.data.IslandQuestProgress;
import su.nightexpress.quests.island.definition.IslandQuest;
import su.nightexpress.quests.island.definition.IslandQuestRequirement;
import su.nightexpress.quests.island.definition.IslandResourceGroup;
import su.nightexpress.quests.island.listener.IslandGenericListener;
import su.nightexpress.quests.island.menu.IslandQuestMenu;
import su.nightexpress.quests.island.menu.IslandResourceGroupMenu;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class IslandManager extends AbstractManager<QuestsPlugin> {

    private final Map<String, IslandResourceGroup> resourceGroups = new HashMap<>();
    private final Map<String, IslandQuest> quests = new HashMap<>();
    private final Map<String, IslandQuestProgress> progressCache = new ConcurrentHashMap<>();
    private final IslandLockManager lockManager = new IslandLockManager();
    private IslandQuestMenu menu;
    private IslandResourceGroupMenu resourceGroupMenu;

    public IslandManager(@NotNull QuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onLoad() {
        if (!Config.FEATURES_ISLAND_QUESTS_ENABLED.get()) {
            return;
        }

        IslandDefaults.setupDefaults(plugin);
        loadResourceGroups();
        loadQuests();

        this.menu = this.addMenu(new IslandQuestMenu(this.plugin, this), Config.DIR_MENU_ISLAND, "island_quests.yml");
        this.resourceGroupMenu = this.addMenu(new IslandResourceGroupMenu(this.plugin, this), Config.DIR_MENU_ISLAND, "resource_groups.yml");

        this.plugin.getServer().getPluginManager().registerEvents(new IslandGenericListener(this.plugin, this), this.plugin);

        var hook = SkyblockHookManager.getHook();
        if (hook != null) {
            hook.registerListeners(this.plugin);
        }
    }

    @Override
    protected void onShutdown() {
        resourceGroups.clear();
        quests.clear();
        progressCache.clear();
        lockManager.clear();
        menu = null;
        resourceGroupMenu = null;
    }

    private void loadResourceGroups() {
        resourceGroups.clear();
        File file = new File(plugin.getDataFolder() + Config.DIR_ISLAND, "resource_groups.yml");
        if (!file.exists()) return;
        FileConfig config = new FileConfig(file);
        config.load();
        if (config.contains("resource_groups")) {
            for (String groupId : config.getSection("resource_groups")) {
                String path = "resource_groups." + groupId;
                String name = config.getString(path + ".name", groupId);
                Map<Material, Double> materials = new HashMap<>();
                if (config.contains(path + ".materials")) {
                    for (String matStr : config.getSection(path + ".materials")) {
                        try {
                            Material mat = Material.valueOf(matStr.toUpperCase());
                            double weight = config.getDouble(path + ".materials." + matStr, 1.0);
                            materials.put(mat, weight);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Invalid material: " + matStr + " in resource group " + groupId);
                        }
                    }
                }
                resourceGroups.put(groupId, new IslandResourceGroup(groupId, name, materials));
            }
        }
    }

    private void loadQuests() {
        quests.clear();
        File file = new File(plugin.getDataFolder() + Config.DIR_ISLAND, "island_quests.yml");
        if (!file.exists()) return;
        FileConfig config = new FileConfig(file);
        config.load();
        if (config.contains("quests")) {
            for (String questId : config.getSection("quests")) {
                String path = "quests." + questId;
                String name = config.getString(path + ".name", questId);
                int order = config.getInt(path + ".order", 1);
                
                List<String> description = config.getStringList(path + ".description");
                if (description.isEmpty()) {
                    description = config.getStringList(path + ".lore");
                }

                List<IslandQuestRequirement> requirements = new ArrayList<>();
                if (config.contains(path + ".requirements")) {
                    for (String reqId : config.getSection(path + ".requirements")) {
                        String reqPath = path + ".requirements." + reqId;
                        String reqName = config.getString(reqPath + ".name", reqId);
                        String resourceGroup = config.getString(reqPath + ".resource_group", "");
                        int target = config.getInt(reqPath + ".target", 100);
                        requirements.add(new IslandQuestRequirement(reqId, reqName, resourceGroup, target));
                    }
                }
                
                List<String> rewards = config.getStringList(path + ".rewards");
                quests.put(questId, new IslandQuest(questId, name, order, description, requirements, rewards));
            }
        }
    }

    public Map<String, IslandResourceGroup> getResourceGroups() {
        return resourceGroups;
    }

    public IslandResourceGroup getResourceGroup(String id) {
        return resourceGroups.get(id);
    }

    public Map<String, IslandQuest> getQuests() {
        return quests;
    }

    public IslandQuest getQuest(String id) {
        return quests.get(id);
    }

    public IslandLockManager getLockManager() {
        return lockManager;
    }

    public IslandQuestMenu getMenu() {
        return menu;
    }

    public IslandResourceGroupMenu getResourceGroupMenu() {
        return resourceGroupMenu;
    }

    public void openResourceGroupMenu(@NotNull Player player) {
        if (resourceGroupMenu != null) {
            resourceGroupMenu.open(player);
        }
    }

    @Nullable
    public UUID getPlayerIsland(@NotNull Player player) {
        var hook = SkyblockHookManager.getHook();
        return hook != null ? hook.getIslandUUID(player) : null;
    }

    @Nullable
    public IslandQuest getActiveQuest(@NotNull UUID islandUuid) {
        List<IslandQuest> sortedQuests = new ArrayList<>(quests.values());
        sortedQuests.sort(Comparator.comparingInt(IslandQuest::getOrder));
        for (IslandQuest q : sortedQuests) {
            IslandQuestProgress progress = getProgress(islandUuid, q.getId());
            if (!progress.isCompleted()) {
                return q;
            }
        }
        return null;
    }

    @NotNull
    public IslandQuestProgress getProgress(@NotNull UUID islandUuid, @NotNull String questId) {
        String key = islandUuid.toString() + ":" + questId;
        return progressCache.computeIfAbsent(key, k -> plugin != null && plugin.getDataHandler() != null ? plugin.getDataHandler().loadIslandProgress(islandUuid, questId) : new IslandQuestProgress(islandUuid, questId));
    }

    public void purgeIslandData(@NotNull UUID islandUuid) {
        lockManager.releaseLock(islandUuid);
        progressCache.keySet().removeIf(k -> k.startsWith(islandUuid.toString()));
        if (plugin != null && plugin.getDataHandler() != null) {
            plugin.getDataHandler().deleteIslandProgress(islandUuid);
        }
    }

    public void invalidatePlayer(@NotNull UUID playerUuid) {
        UUID lockedIsland = lockManager.getPlayerLockedIsland(playerUuid);
        if (lockedIsland != null) {
            lockManager.releasePlayerLock(playerUuid);
            progressCache.keySet().removeIf(k -> k.startsWith(lockedIsland.toString()));
        }
    }

    public void saveProgress(@NotNull IslandQuestProgress progress) {
        if (plugin != null && plugin.getDataHandler() != null) {
            plugin.getDataHandler().saveIslandProgress(progress.getIslandUuid(), progress.getQuestId(), progress.getProgress(), progress.isCompleted());
        }
    }

    public void handleInventoryClose(@NotNull Player player) {
        lockManager.releasePlayerLock(player.getUniqueId());
    }

    public void handlePlayerQuit(@NotNull Player player) {
        handleInventoryClose(player);
    }

    public void openMenu(@NotNull Player player) {
        if (!Config.FEATURES_ISLAND_QUESTS_ENABLED.get()) {
            return;
        }

        UUID islandUuid = getPlayerIsland(player);
        if (islandUuid == null) {
            Lang.ISLAND_QUESTS_NO_ISLAND.message().send(player);
            return;
        }

        if (!lockManager.acquireLock(islandUuid, player.getUniqueId())) {
            Lang.ISLAND_QUEST_MENU_LOCKED.message().send(player);
            return;
        }

        if (menu != null) {
            menu.open(player);
        }
    }

    public void deposit(@NotNull Player player, @NotNull IslandQuestRequirement requirement, boolean all) {
        UUID islandUuid = getPlayerIsland(player);
        if (islandUuid == null) {
            Lang.ISLAND_QUESTS_NO_ISLAND.message().send(player);
            return;
        }

        if (!lockManager.acquireLock(islandUuid, player.getUniqueId())) {
            Lang.ISLAND_QUEST_MENU_LOCKED.message().send(player);
            return;
        }

        IslandQuest activeQuest = getActiveQuest(islandUuid);
        if (activeQuest == null) {
            return;
        }

        IslandQuestProgress progress = getProgress(islandUuid, activeQuest.getId());
        if (progress.isCompleted()) {
            return;
        }

        int currentProgress = progress.getRequirementProgress(requirement.getId());
        int target = requirement.getTargetAmount();
        if (currentProgress >= target) {
            return;
        }

        IslandResourceGroup group = resourceGroups.get(requirement.getResourceGroupId());
        if (group == null) return;

        int neededPoints = target - currentProgress;
        int totalPointsDeposited = 0;

        var inventory = player.getInventory();

        if (!all) {
            var handItem = inventory.getItemInMainHand();
            if (handItem == null || handItem.getType().isAir() || !group.contains(handItem.getType())) {
                Lang.ISLAND_QUESTS_INVALID_HAND_ITEM.message().send(player);
                return;
            }

            Material mat = handItem.getType();
            double weight = group.getWeight(mat);
            if (weight > 0) {
                int amountInStack = handItem.getAmount();
                int maxItemsNeeded = (int) Math.ceil((neededPoints - totalPointsDeposited) / weight);
                if (maxItemsNeeded > 0) {
                    int toTake = Math.min(amountInStack, maxItemsNeeded);
                    int pointsAdded = (int) Math.round(toTake * weight);
                    totalPointsDeposited += pointsAdded;

                    handItem.setAmount(amountInStack - toTake);
                    inventory.setItemInMainHand(handItem);
                }
            }
        } else {
            var contents = inventory.getContents();
            for (int i = 0; i < contents.length; i++) {
                var item = contents[i];
                if (item == null || item.getType().isAir()) continue;

                Material mat = item.getType();
                if (group.contains(mat)) {
                    double weight = group.getWeight(mat);
                    if (weight <= 0) continue;

                    int amountInStack = item.getAmount();
                    int maxItemsNeeded = (int) Math.ceil((neededPoints - totalPointsDeposited) / weight);
                    if (maxItemsNeeded <= 0) break;

                    int toTake = Math.min(amountInStack, maxItemsNeeded);
                    int pointsAdded = (int) Math.round(toTake * weight);
                    totalPointsDeposited += pointsAdded;

                    item.setAmount(amountInStack - toTake);
                    inventory.setItem(i, item);

                    if (totalPointsDeposited >= neededPoints) {
                        break;
                    }
                }
            }
        }

        if (totalPointsDeposited > 0) {
            int newProgress = currentProgress + totalPointsDeposited;
            progress.setRequirementProgress(requirement.getId(), Math.min(newProgress, target));
            saveProgress(progress);

            playDepositSound(player);

            if (menu != null) {
                var hook = SkyblockHookManager.getHook();
                List<Player> members = hook != null ? hook.getOnlineMembers(islandUuid) : Collections.singletonList(player);
                for (Player member : members) {
                    var viewer = menu.getViewer(member);
                    if (viewer != null) {
                        menu.open(member);
                    }
                }
            }

            boolean allCompleted = true;
            for (IslandQuestRequirement req : activeQuest.getRequirements()) {
                if (progress.getRequirementProgress(req.getId()) < req.getTargetAmount()) {
                    allCompleted = false;
                    break;
                }
            }

            if (allCompleted) {
                progress.setCompleted(true);
                saveProgress(progress);
                handleQuestCompletion(player, islandUuid, activeQuest);
            }
        }
    }

    protected void playDepositSound(Player player) {
        String soundStr = Config.SOUNDS_DEPOSIT.get();
        if (soundStr != null && !soundStr.isEmpty()) {
            try {
                player.playSound(player.getLocation(), Sound.valueOf(soundStr.toUpperCase()), 1.0f, 1.0f);
            } catch (Exception e) {
                try {
                    player.playSound(player.getLocation(), soundStr, 1.0f, 1.0f);
                } catch (Exception ignored) {}
            }
        }
    }

    protected void handleQuestCompletion(Player player, UUID islandUuid, IslandQuest quest) {
        var hook = SkyblockHookManager.getHook();
        List<Player> members = hook != null ? hook.getOnlineMembers(islandUuid) : Collections.singletonList(player);

        String soundStr = Config.SOUNDS_ISLAND_QUEST_COMPLETED.get();
        String title = ChatColor.translateAlternateColorCodes('&', "&aQuête d'Île Complétée !");
        String subtitle = ChatColor.translateAlternateColorCodes('&', "&f" + quest.getName());

        for (Player p : members) {
            if (soundStr != null && !soundStr.isEmpty()) {
                try {
                    p.playSound(p.getLocation(), Sound.valueOf(soundStr.toUpperCase()), 1.0f, 1.0f);
                } catch (Exception e) {
                    try {
                        p.playSound(p.getLocation(), soundStr, 1.0f, 1.0f);
                    } catch (Exception ignored) {}
                }
            }

            p.sendTitle(title, subtitle, 10, 70, 20);
            Lang.ISLAND_QUEST_COMPLETED.message().send(p);

            for (String rewardCmd : quest.getRewards()) {
                String cmd = rewardCmd.replace("%player%", p.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }
    }
}
