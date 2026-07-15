package su.nightexpress.quests.tracker;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.config.Config;
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

    public synchronized void cleanup(@NotNull Player player) {
        removeTracker(player.getUniqueId());
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

            String progressStr = String.valueOf(questData.countTotalProgress());
            String requiredStr = String.valueOf(questData.countTotalRequirement());
            String titleText = formatPattern
                    .replace("%quest%", quest.getName())
                    .replace("%progress%", progressStr)
                    .replace("%required%", requiredStr);

            player.sendMessage(MiniMessage.miniMessage().deserialize(titleText));
            return;
        }

        PlayerTracker tracker = trackers.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerTracker(player));
        tracker.addProgress(quest, questData, modeStr);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeTracker(event.getPlayer().getUniqueId());
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
        private BossBar bossBar;
        private BukkitTask rotationTask;
        private BukkitTask cleanupTask;
        private BukkitTask actionBarTask;
        private String currentQuestId;
        private String mode;

        public PlayerTracker(Player player) {
            this.player = player;
        }

        public void addProgress(Quest quest, QuestData questData, String mode) {
            this.mode = mode;
            String type = "personal";
            if (quest.getId().toLowerCase().startsWith("lore_") || quest.getId().toLowerCase().startsWith("histoire_")) {
                type = "lore";
            } else if (quest.getId().toLowerCase().startsWith("island_") || quest.getId().toLowerCase().startsWith("ile_")) {
                type = "island";
            }

            activeQuests.put(quest.getId(), new TrackedQuest(quest, questData, type));

            updateDisplay();
            resetCleanupTimer();

            if (activeQuests.size() > 1 && rotationTask == null) {
                startRotation();
            }
        }

        private void updateDisplay() {
            if (activeQuests.isEmpty()) {
                hide();
                return;
            }

            if (currentQuestId == null || !activeQuests.containsKey(currentQuestId)) {
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

                String progressStr = String.valueOf(tracked.questData.countTotalProgress());
                String requiredStr = String.valueOf(tracked.questData.countTotalRequirement());
                String titleText = formatPattern
                        .replace("%quest%", tracked.quest.getName())
                        .replace("%progress%", progressStr)
                        .replace("%required%", requiredStr);

                Component titleComponent = MiniMessage.miniMessage().deserialize(titleText);
                float progress = (float) tracked.questData.getProgressValue();
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
            }

            int duration;
            if ("ACTION_BAR".equalsIgnoreCase(mode)) {
                duration = Config.TRACKER_ACTIONBAR_DISPLAY_DURATION.get();
            } else {
                duration = Config.TRACKER_BOSSBAR_DISPLAY_DURATION.get();
            }
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

                        String progressStr = String.valueOf(tracked.questData.countTotalProgress());
                        String requiredStr = String.valueOf(tracked.questData.countTotalRequirement());
                        String titleText = formatPattern
                                .replace("%quest%", tracked.quest.getName())
                                .replace("%progress%", progressStr)
                                .replace("%required%", requiredStr);

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
        }
    }

    private static class TrackedQuest {
        final Quest quest;
        final QuestData questData;
        final String type;

        TrackedQuest(Quest quest, QuestData questData, String type) {
            this.quest = quest;
            this.questData = questData;
            this.type = type;
        }
    }
}
