package su.nightexpress.quests.tracker;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.island.data.IslandQuestProgress;
import su.nightexpress.quests.island.definition.IslandQuest;
import su.nightexpress.quests.island.definition.IslandQuestRequirement;
import su.nightexpress.quests.lore.data.LoreQuestData;
import su.nightexpress.quests.lore.definition.LoreObjective;
import su.nightexpress.quests.lore.definition.LoreQuest;
import su.nightexpress.quests.lore.definition.LoreQuestCategory;
import su.nightexpress.quests.personal.data.PersonalQuestData;
import su.nightexpress.quests.personal.definition.RpgCategory;
import su.nightexpress.quests.quest.data.QuestData;
import su.nightexpress.quests.quest.definition.Quest;
import su.nightexpress.quests.user.QuestUser;

import java.util.*;

public class QuestTrackerManager implements Listener {

    private static QuestTrackerManager instance;

    public static void setup(@NotNull QuestsPlugin plugin) {
        instance = new QuestTrackerManager(plugin);
        Bukkit.getPluginManager().registerEvents(instance, plugin);
    }

    public static void shutdown() {
        if (instance != null) {
            instance.clearAll();
            instance = null;
        }
    }

    public static QuestTrackerManager getInstance() {
        return instance;
    }

    private final QuestsPlugin plugin;
    private final Map<UUID, PlayerTracker> trackers = new HashMap<>();

    public QuestTrackerManager(@NotNull QuestsPlugin plugin) {
        this.plugin = plugin;
    }

    public static void showProgress(@NotNull Player player, @NotNull Quest quest, @NotNull QuestData questData) {
        if (instance != null) {
            instance.handleProgress(player, quest, questData);
        }
    }

    public static void showLoreProgress(@NotNull Player player, @NotNull LoreQuest quest, @NotNull LoreQuestCategory category, int progress, int required) {
        if (instance != null) {
            instance.handleLoreProgress(player, quest, category, progress, required);
        }
    }

    public static void forceCleanupCategory(@NotNull Player player, @NotNull String categoryId) {
        if (instance != null) {
            instance.handleCleanupCategory(player, categoryId);
        }
    }

    public synchronized void cleanup(@NotNull Player player) {
        removeTracker(player.getUniqueId());
    }

    private synchronized void handleLoreProgress(@NotNull Player player, @NotNull LoreQuest quest, @NotNull LoreQuestCategory category, int progress, int required) {
        if (!Config.TRACKER_ENABLED.get()) {
            return;
        }

        QuestUser user = plugin.getUserManager().getOrFetch(player);
        if (user.isCategoryTrackerDisabled(category.getId()) || user.isCategoryTrackerDisabled("lore")) {
            return;
        }

        String modeStr = user.getTrackerMode();
        if ("NONE".equalsIgnoreCase(modeStr)) {
            return;
        }

        if ("CHAT".equalsIgnoreCase(modeStr)) {
            String formatPattern = Config.TRACKER_FORMATS_CHAT_LORE.get();
            String firstDesc = quest.getDescription().isEmpty() ? "" : quest.getDescription().get(0);
            TrackedQuest temp = new TrackedQuest(quest.getId(), quest.getName(), progress, required, 0.0, "lore", category.getId(), firstDesc, "", 0, "");
            String titleText = formatText(formatPattern, temp);

            player.sendMessage(MiniMessage.miniMessage().deserialize(titleText));
            return;
        }

        PlayerTracker tracker = trackers.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerTracker(player));
        String firstDesc = quest.getDescription().isEmpty() ? "" : quest.getDescription().get(0);
        tracker.addLoreProgress(quest.getId(), quest.getName(), category.getId(), progress, required, modeStr, firstDesc, "", 0, "");
    }

    private synchronized void handleCleanupCategory(@NotNull Player player, @NotNull String categoryId) {
        PlayerTracker tracker = trackers.get(player.getUniqueId());
        if (tracker != null) {
            tracker.removeCategory(categoryId);
        }
    }

    private synchronized void handleProgress(@NotNull Player player, @NotNull Quest quest, @NotNull QuestData questData) {
        if (!Config.TRACKER_ENABLED.get()) {
            return;
        }

        QuestUser user = plugin.getUserManager().getOrFetch(player);
        String modeStr = user.getTrackerMode();
        if ("NONE".equalsIgnoreCase(modeStr)) {
            return;
        }

        if ("CHAT".equalsIgnoreCase(modeStr)) {
            String type = "personal";
            if (quest.getId().toLowerCase().startsWith("lore_") || quest.getId().toLowerCase().startsWith("histoire_")) {
                type = "lore";
            } else if (quest.getId().toLowerCase().startsWith("island_") || quest.getId().toLowerCase().startsWith("ile_")) {
                type = "island";
            }

            String formatPattern;
            if ("lore".equals(type)) {
                formatPattern = Config.TRACKER_FORMATS_CHAT_LORE.get();
            } else if ("island".equals(type)) {
                formatPattern = Config.TRACKER_FORMATS_CHAT_ISLAND.get();
            } else {
                formatPattern = Config.TRACKER_FORMATS_CHAT_PERSONAL.get();
            }

            TrackedQuest temp = new TrackedQuest(quest.getId(), quest.getName(), questData.countTotalProgress(), questData.countTotalRequirement(), questData.getProgressValue(), type, null, "", "", 0, "");
            String titleText = formatText(formatPattern, temp);

            player.sendMessage(MiniMessage.miniMessage().deserialize(titleText));
            return;
        }

        PlayerTracker tracker = trackers.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerTracker(player));
        tracker.addProgress(quest, questData, modeStr);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> refreshPlayerTrackers(event.getPlayer()), 20L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        refreshPlayerTrackers(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeTracker(event.getPlayer().getUniqueId());
    }

    public synchronized void refreshPlayerTrackers(@NotNull Player player) {
        if (!Config.TRACKER_ENABLED.get() || !player.isOnline()) {
            return;
        }

        QuestUser user = plugin.getUserManager().getOrFetch(player);
        String modeStr = user.getTrackerMode();
        if ("NONE".equalsIgnoreCase(modeStr) || "CHAT".equalsIgnoreCase(modeStr)) {
            removeTracker(player.getUniqueId());
            return;
        }

        PlayerTracker tracker = trackers.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerTracker(player));
        tracker.resetActiveQuests();

        String worldName = player.getWorld().getName();
        boolean allowLore = !Config.TRACKER_DISABLED_WORLDS_LORE.get().contains(worldName) && !user.isCategoryTrackerDisabled("lore");
        boolean allowPersonal = !Config.TRACKER_DISABLED_WORLDS_PERSONAL.get().contains(worldName) && !user.isCategoryTrackerDisabled("personal");
        boolean allowIsland = Config.TRACKER_INCLUDE_ISLAND_QUESTS.get() && !Config.TRACKER_DISABLED_WORLDS_ISLAND.get().contains(worldName) && !user.isCategoryTrackerDisabled("island");

        if (allowLore && plugin.getLoreManager() != null) {
            for (LoreQuestCategory category : plugin.getLoreManager().getCategories().values()) {
                if (user.isCategoryTrackerDisabled(category.getId())) continue;
                if (plugin.getLoreManager().isCategoryUnlocked(user, category) && !plugin.getLoreManager().isCategoryCompleted(user, category)) {
                    LoreQuest activeQuest = plugin.getLoreManager().getActiveQuest(user, category);
                    if (activeQuest != null) {
                        int totalProgress = 0;
                        int totalRequired = 0;
                        LoreQuestData questProgress = user.getLoreQuestsProgress().get(activeQuest.getId());
                        if (questProgress != null) {
                            for (LoreObjective obj : activeQuest.getObjectives()) {
                                totalProgress += questProgress.getProgress(obj.getId());
                                totalRequired += obj.getRequired();
                            }
                        } else {
                            totalRequired = activeQuest.getObjectives().stream().mapToInt(LoreObjective::getRequired).sum();
                        }
                        String firstDesc = "";
                        if (!activeQuest.getDescription().isEmpty()) {
                            firstDesc = activeQuest.getDescription().get(0);
                        } else if (!activeQuest.getObjectives().isEmpty()) {
                            firstDesc = activeQuest.getObjectives().get(0).getDescription();
                        }
                        tracker.addLoreProgress(activeQuest.getId(), activeQuest.getName(), category.getId(), totalProgress, totalRequired, modeStr, firstDesc, "", 0, "");
                    }
                }
            }
        }

        for (QuestData qData : user.getQuestDatas()) {
            if (qData.isActive() && !qData.isCompleted() && !qData.isExpired()) {
                Quest q = plugin.questManager().map(qm -> qm.getQuestById(qData.getQuestId())).orElse(null);
                if (q != null) {
                    tracker.addProgress(q, qData, modeStr);
                }
            }
        }

        if (allowPersonal) {
            plugin.personalQuestManager().ifPresent(pqm -> {
                for (RpgCategory cat : pqm.getCategories().values()) {
                    PersonalQuestData pData = user.getPersonalQuestData().get(cat.getId());
                    if (pData != null && pData.hasActiveQuest()) {
                        String titleName = cat.getDisplayName();
                        int lvl = user.getRPGLevel(cat.getId());
                        String obj = pData.getObjectiveId() != null ? pData.getObjectiveId() : "";
                        tracker.addLoreProgress("personal_" + cat.getId(), titleName, cat.getId(), pData.getProgress(), pData.getRequiredAmount(), modeStr, "", "", lvl, obj);
                    }
                }
            });
        }

        if (allowIsland) {
            plugin.islandManager().ifPresent(im -> {
                UUID islandUuid = im.getPlayerIsland(player);
                if (islandUuid != null) {
                    IslandQuest activeQuest = im.getActiveQuest(islandUuid);
                    if (activeQuest != null) {
                        IslandQuestProgress iProgress = im.getProgress(islandUuid, activeQuest.getId());
                        int req = activeQuest.getRequirements().stream().mapToInt(IslandQuestRequirement::getTargetAmount).sum();
                        int cur = iProgress.getProgress().values().stream().mapToInt(Integer::intValue).sum();

                        String reqName = activeQuest.getName();
                        for (IslandQuestRequirement reqObj : activeQuest.getRequirements()) {
                            int reqCur = iProgress.getRequirementProgress(reqObj.getId());
                            if (reqCur < reqObj.getTargetAmount()) {
                                reqName = reqObj.getName();
                                break;
                            }
                        }

                        tracker.addLoreProgress("island_" + activeQuest.getId(), activeQuest.getName(), "island", cur, req, modeStr, "", reqName, activeQuest.getOrder(), "");
                    }
                }
            });
        }

        tracker.updateDisplay();
    }

    private synchronized void removeTracker(UUID uuid) {
        PlayerTracker tracker = trackers.remove(uuid);
        if (tracker != null) {
            tracker.cleanup();
        }
    }

    private synchronized void clearAll() {
        for (PlayerTracker tracker : trackers.values()) {
            tracker.cleanup();
        }
        trackers.clear();
    }

    private class PlayerTracker {
        private final Player player;
        private final LinkedHashMap<String, TrackedQuest> activeQuests = new LinkedHashMap<>();
        private final Map<String, Integer> lastProgressValues = new HashMap<>();
        private BossBar bossBar;
        private BukkitTask rotationTask;
        private BukkitTask cleanupTask;
        private BukkitTask actionBarTask;
        private String currentQuestId;
        private String focusedQuestId;
        private long lastFocusTime = 0L;
        private String mode;

        public PlayerTracker(Player player) {
            this.player = player;
        }

        public void resetActiveQuests() {
            this.activeQuests.clear();
        }

        private void registerProgress(String questId, int progress, int required) {
            int prev = lastProgressValues.getOrDefault(questId, -1);
            boolean progressIncreased = (prev != -1 && progress > prev);
            lastProgressValues.put(questId, progress);

            if (progressIncreased) {
                double newRatio = required > 0 ? (double) progress / required : 0.0;
                long focusTimeout = Config.TRACKER_BOSSBAR_DISPLAY_DURATION.get() * 1000L;
                if (focusTimeout <= 0) focusTimeout = 5000L;

                boolean shouldFocus = false;
                if (focusedQuestId == null || !activeQuests.containsKey(focusedQuestId) || (System.currentTimeMillis() - lastFocusTime >= focusTimeout)) {
                    shouldFocus = true;
                } else {
                    TrackedQuest focusedQuest = activeQuests.get(focusedQuestId);
                    double focusedRatio = (focusedQuest != null) ? focusedQuest.progressValue : 0.0;
                    if (newRatio >= focusedRatio || questId.equals(focusedQuestId)) {
                        shouldFocus = true;
                    }
                }

                if (shouldFocus) {
                    focusedQuestId = questId;
                    lastFocusTime = System.currentTimeMillis();
                    currentQuestId = questId;
                } else {
                    lastFocusTime = System.currentTimeMillis();
                }
            }
        }

        public void addProgress(Quest quest, QuestData questData, String mode) {
            this.mode = mode;
            String type = "personal";
            if (quest.getId().toLowerCase().startsWith("lore_") || quest.getId().toLowerCase().startsWith("histoire_")) {
                type = "lore";
            } else if (quest.getId().toLowerCase().startsWith("island_") || quest.getId().toLowerCase().startsWith("ile_")) {
                type = "island";
            }

            int curProgress = questData.countTotalProgress();
            int reqProgress = questData.countTotalRequirement();
            double progressVal = questData.getProgressValue();

            activeQuests.put(quest.getId(), new TrackedQuest(
                quest.getId(),
                quest.getName(),
                curProgress,
                reqProgress,
                progressVal,
                type,
                null,
                "",
                "",
                0,
                ""
            ));

            registerProgress(quest.getId(), curProgress, reqProgress);

            updateDisplay();
            resetCleanupTimer();

            if (activeQuests.size() > 1 && rotationTask == null) {
                startRotation();
            }
        }

        public void addLoreProgress(String questId, String questName, String categoryId, int progress, int required, String mode) {
            addLoreProgress(questId, questName, categoryId, progress, required, mode, "", "", 0, "");
        }

        public void addLoreProgress(String questId, String questName, String categoryId, int progress, int required, String mode,
                                    String description, String requirementName, int level, String objective) {
            this.mode = mode;
            String type = "lore";
            if (questId.startsWith("personal_")) {
                type = "personal";
            } else if (questId.startsWith("island_")) {
                type = "island";
            }

            double progressVal = required > 0 ? (double) progress / required : 0.0;
            activeQuests.put(questId, new TrackedQuest(
                questId,
                questName,
                progress,
                required,
                progressVal,
                type,
                categoryId,
                description,
                requirementName,
                level,
                objective
            ));

            registerProgress(questId, progress, required);

            updateDisplay();
            resetCleanupTimer();

            if (activeQuests.size() > 1 && rotationTask == null) {
                startRotation();
            }
        }

        public void removeCategory(String categoryId) {
            activeQuests.entrySet().removeIf(entry -> categoryId.equalsIgnoreCase(entry.getValue().categoryId));
            if (focusedQuestId != null && !activeQuests.containsKey(focusedQuestId)) {
                focusedQuestId = null;
            }
            if (currentQuestId != null && !activeQuests.containsKey(currentQuestId)) {
                currentQuestId = null;
            }
            updateDisplay();
        }

        private void updateDisplay() {
            if (activeQuests.isEmpty()) {
                hide();
                if (rotationTask != null) {
                    rotationTask.cancel();
                    rotationTask = null;
                }
                focusedQuestId = null;
                return;
            }

            if (activeQuests.size() <= 1 && rotationTask != null) {
                rotationTask.cancel();
                rotationTask = null;
            }

            long focusTimeout = Config.TRACKER_BOSSBAR_DISPLAY_DURATION.get() * 1000L;
            if (focusTimeout <= 0) focusTimeout = 5000L;

            if (focusedQuestId != null && activeQuests.containsKey(focusedQuestId) && (System.currentTimeMillis() - lastFocusTime < focusTimeout)) {
                currentQuestId = focusedQuestId;
            } else if (currentQuestId == null || !activeQuests.containsKey(currentQuestId)) {
                currentQuestId = activeQuests.keySet().iterator().next();
            }

            TrackedQuest tracked = activeQuests.get(currentQuestId);
            if (tracked == null) return;

            if ("BOSS_BAR".equalsIgnoreCase(mode)) {
                stopActionBarTask();

                String formatPattern;
                if ("lore".equals(tracked.type)) {
                    formatPattern = Config.TRACKER_FORMATS_BOSSBAR_LORE.get();
                } else if ("island".equals(tracked.type)) {
                    formatPattern = Config.TRACKER_FORMATS_BOSSBAR_ISLAND.get();
                } else {
                    formatPattern = Config.TRACKER_FORMATS_BOSSBAR_PERSONAL.get();
                }

                String titleText = formatText(formatPattern, tracked);

                Component titleComponent = MiniMessage.miniMessage().deserialize(titleText);
                float progress = (float) tracked.progressValue;
                if (progress < 0f) progress = 0f;
                if (progress > 1f) progress = 1f;

                BossBar.Color barColor;
                try {
                    barColor = BossBar.Color.valueOf(Config.TRACKER_BOSSBAR_COLOR.get().toUpperCase());
                } catch (Exception e) {
                    barColor = BossBar.Color.GREEN;
                }

                BossBar.Overlay barOverlay;
                try {
                    barOverlay = BossBar.Overlay.valueOf(Config.TRACKER_BOSSBAR_STYLE.get().toUpperCase());
                } catch (Exception e) {
                    barOverlay = BossBar.Overlay.PROGRESS;
                }

                if (bossBar == null) {
                    bossBar = BossBar.bossBar(titleComponent, progress, barColor, barOverlay);
                    player.showBossBar(bossBar);
                } else {
                    bossBar.name(titleComponent);
                    bossBar.progress(progress);
                    bossBar.color(barColor);
                    bossBar.overlay(barOverlay);
                }
            } else if ("ACTION_BAR".equalsIgnoreCase(mode)) {
                hideBossBarOnly();
                startActionBarTask();
            }
        }

        private void startRotation() {
            int interval = Config.TRACKER_BOSSBAR_SWITCHING_INTERVAL.get();
            if (interval <= 0) interval = 3;

            rotationTask = new BukkitRunnable() {
                @Override
                public void run() {
                    synchronized (QuestTrackerManager.this) {
                        if (activeQuests.size() <= 1) {
                            if (rotationTask != null) {
                                rotationTask.cancel();
                                rotationTask = null;
                            }
                            return;
                        }

                        long focusTimeout = Config.TRACKER_BOSSBAR_DISPLAY_DURATION.get() * 1000L;
                        if (focusTimeout <= 0) focusTimeout = 5000L;

                        if (focusedQuestId != null && (System.currentTimeMillis() - lastFocusTime < focusTimeout)) {
                            if (activeQuests.containsKey(focusedQuestId)) {
                                currentQuestId = focusedQuestId;
                                updateDisplay();
                                return;
                            } else {
                                focusedQuestId = null;
                            }
                        } else {
                            focusedQuestId = null;
                        }

                        List<String> keys = new ArrayList<>(activeQuests.keySet());
                        int index = keys.indexOf(currentQuestId);
                        int nextIndex = (index + 1) % keys.size();
                        currentQuestId = keys.get(nextIndex);

                        updateDisplay();
                    }
                }
            }.runTaskTimer(plugin, interval * 20L, interval * 20L);
        }

        private void resetCleanupTimer() {
            if (cleanupTask != null) {
                cleanupTask.cancel();
                cleanupTask = null;
            }

            if ("BOSS_BAR".equalsIgnoreCase(mode) || "ACTION_BAR".equalsIgnoreCase(mode)) {
                return;
            }

            int duration = Config.TRACKER_BOSSBAR_DISPLAY_DURATION.get();
            if (duration <= 0) duration = 5;

            cleanupTask = new BukkitRunnable() {
                @Override
                public void run() {
                    synchronized (QuestTrackerManager.this) {
                        hide();
                        cleanup();
                        trackers.remove(player.getUniqueId());
                    }
                }
            }.runTaskLater(plugin, duration * 20L);
        }

        private void hideBossBarOnly() {
            if (bossBar != null) {
                player.hideBossBar(bossBar);
                bossBar = null;
            }
        }

        private void stopActionBarTask() {
            if (actionBarTask != null) {
                actionBarTask.cancel();
                actionBarTask = null;
            }
        }

        private void startActionBarTask() {
            if (actionBarTask != null) {
                return;
            }

            actionBarTask = new BukkitRunnable() {
                @Override
                public void run() {
                    synchronized (QuestTrackerManager.this) {
                        if (activeQuests.isEmpty() || !player.isOnline()) {
                            stopActionBarTask();
                            return;
                        }

                        TrackedQuest tracked = activeQuests.get(currentQuestId);
                        if (tracked == null) {
                            stopActionBarTask();
                            return;
                        }

                        String formatPattern;
                        if ("lore".equals(tracked.type)) {
                            formatPattern = Config.TRACKER_FORMATS_ACTIONBAR_LORE.get();
                        } else if ("island".equals(tracked.type)) {
                            formatPattern = Config.TRACKER_FORMATS_ACTIONBAR_ISLAND.get();
                        } else {
                            formatPattern = Config.TRACKER_FORMATS_ACTIONBAR_PERSONAL.get();
                        }

                        String titleText = formatText(formatPattern, tracked);

                        Component actionBarComponent = MiniMessage.miniMessage().deserialize(titleText);
                        player.sendActionBar(actionBarComponent);
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }

        private void hide() {
            hideBossBarOnly();
            stopActionBarTask();
        }

        public void cleanup() {
            hide();
            if (rotationTask != null) {
                rotationTask.cancel();
                rotationTask = null;
            }
            if (cleanupTask != null) {
                cleanupTask.cancel();
                cleanupTask = null;
            }
            activeQuests.clear();
            lastProgressValues.clear();
            focusedQuestId = null;
        }
    }

    private static String formatText(String formatPattern, TrackedQuest tracked) {
        if (formatPattern == null) return "";
        String desc = tracked.description != null ? tracked.description : "";
        String reqName = tracked.requirementName != null ? tracked.requirementName : "";
        String obj = tracked.objective != null ? tracked.objective : "";

        return formatPattern
                .replace("%quest_name%", tracked.name)
                .replace("%quest%", tracked.name)
                .replace("%description%", desc)
                .replace("%requirement_name%", reqName)
                .replace("%level%", String.valueOf(tracked.level))
                .replace("%objective%", obj)
                .replace("%progress%", String.valueOf(tracked.progress))
                .replace("%required%", String.valueOf(tracked.required))
                .replace("%target%", String.valueOf(tracked.required));
    }

    private static class TrackedQuest {
        final String id;
        final String name;
        final int progress;
        final int required;
        final double progressValue;
        final String type;
        final String categoryId;
        final String description;
        final String requirementName;
        final int level;
        final String objective;

        TrackedQuest(String id, String name, int progress, int required, double progressValue, String type, String categoryId,
                     String description, String requirementName, int level, String objective) {
            this.id = id;
            this.name = name;
            this.progress = progress;
            this.required = required;
            this.progressValue = progressValue;
            this.type = type;
            this.categoryId = categoryId;
            this.description = description;
            this.requirementName = requirementName;
            this.level = level;
            this.objective = objective;
        }
    }
}
