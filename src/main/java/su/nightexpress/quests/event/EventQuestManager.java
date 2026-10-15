package su.nightexpress.quests.event;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.nightcore.util.Players;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.event.data.EventDataDao;
import su.nightexpress.quests.event.data.EventUserData;
import su.nightexpress.quests.event.definition.EventQuest;
import su.nightexpress.quests.event.definition.EventTier;
import su.nightexpress.quests.event.listener.EventPickupListener;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EventQuestManager extends AbstractManager<QuestsPlugin> {

    public static final String EVENTS_DIR = "/events/";

    private final Map<String, EventQuest> eventQuests;
    private final Map<String, EventUserData> userDataCache;
    private final Map<String, su.nightexpress.quests.event.data.EventLeaderboard> leaderboards;
    private EventDataDao eventDataDao;
    private EventPickupListener pickupListener;

    public EventQuestManager(@Nullable QuestsPlugin plugin) {
        super(plugin);
        this.eventQuests = new LinkedHashMap<>();
        this.userDataCache = new ConcurrentHashMap<>();
        this.leaderboards = new ConcurrentHashMap<>();
    }

    @Override
    protected void onLoad() {
        if (this.plugin != null && this.plugin.getDataHandler() != null) {
            this.eventDataDao = this.plugin.getDataHandler().getEventDataDao();
        }

        this.loadQuests();

        this.pickupListener = new EventPickupListener(this.plugin, this);
        this.addListener(this.pickupListener);

        if (this.plugin != null) {
            this.plugin.runTaskTimerAsync(() -> {
                for (su.nightexpress.quests.event.data.EventLeaderboard lb : this.leaderboards.values()) {
                    lb.refreshSync();
                }
            }, 20L, 20L * 60L); // Every 60s
        }
    }

    @Override
    protected void onShutdown() {
        this.saveAllUserData();
        this.userDataCache.clear();
        this.eventQuests.clear();
        this.eventDataDao = null;
        this.pickupListener = null;
    }

    public void loadQuests() {
        this.eventQuests.clear();
        File dir = new File(this.plugin.getDataFolder() + EVENTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
        if (files == null || files.length == 0) {
            try {
                if (this.plugin != null) {
                    this.plugin.saveResource("events/halloween_pumpkin.yml", false);
                    files = dir.listFiles((d, name) -> name.endsWith(".yml"));
                }
            } catch (Exception ignored) {}
            if (files == null || files.length == 0) return;
        }

        for (File file : files) {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                EventQuest quest = EventQuest.fromYaml(config);
                this.registerQuest(quest);
            } catch (Exception e) {
                this.plugin.error("Failed to load event quest from " + file.getName() + ": " + e.getMessage());
            }
        }
        this.plugin.info("Loaded " + this.eventQuests.size() + " event quests.");
    }

    public void registerQuest(@NotNull EventQuest quest) {
        this.eventQuests.put(quest.getId().toLowerCase(), quest);
    }

    @Nullable
    public EventQuest getQuest(@NotNull String id) {
        return this.eventQuests.get(id.toLowerCase());
    }

    @NotNull
    public Map<String, EventQuest> getQuests() {
        return Collections.unmodifiableMap(this.eventQuests);
    }

    @NotNull
    public List<EventQuest> getActiveQuests() {
        List<EventQuest> active = new ArrayList<>();
        for (EventQuest quest : this.eventQuests.values()) {
            if (quest.isActive()) {
                active.add(quest);
            }
        }
        return active;
    }

    @NotNull
    public su.nightexpress.quests.event.data.EventLeaderboard getLeaderboard(@NotNull String eventId) {
        return this.leaderboards.computeIfAbsent(eventId.toLowerCase(), id -> new su.nightexpress.quests.event.data.EventLeaderboard(id, this.eventDataDao));
    }

    public void setLeaderboard(@NotNull String eventId, @NotNull su.nightexpress.quests.event.data.EventLeaderboard leaderboard) {
        this.leaderboards.put(eventId.toLowerCase(), leaderboard);
    }

    @NotNull
    public EventUserData getUserData(@NotNull UUID playerUuid, @NotNull String eventId) {
        String cacheKey = playerUuid + ":" + eventId.toLowerCase();
        return this.userDataCache.computeIfAbsent(cacheKey, k -> {
            if (this.eventDataDao != null) {
                return this.eventDataDao.loadUserData(playerUuid, eventId.toLowerCase());
            }
            return new EventUserData(playerUuid, eventId.toLowerCase());
        });
    }

    public void progressEvent(@NotNull Player player, @NotNull EventQuest quest, long amount) {
        if (!quest.isActive()) return;
        if (amount <= 0) return;

        EventUserData userData = getUserData(player.getUniqueId(), quest.getId());
        long oldScore = userData.getTotalScore();
        userData.addScore(amount);
        long newScore = userData.getTotalScore();

        // Check if a new tier was just unlocked and notify player
        for (EventTier tier : quest.getTiers().values()) {
            if (oldScore < tier.getRequirement() && newScore >= tier.getRequirement()) {
                player.sendMessage("★ Félicitations ! Vous avez débloqué le " + tier.getDisplayName() + " dans l'événement " + quest.getName() + " ! Ouvrez le menu pour réclamer votre récompense.");
                try {
                    player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                } catch (Exception ignored) {}
            }
        }

        saveUserData(userData);
    }

    public boolean claimTier(@NotNull Player player, @NotNull EventQuest quest, int tierNumber) {
        EventTier tier = quest.getTier(tierNumber);
        if (tier == null) return false;

        EventUserData userData = getUserData(player.getUniqueId(), quest.getId());
        if (!userData.canClaim(tier)) {
            return false;
        }

        boolean claimed = userData.claimTier(tierNumber);
        if (claimed) {
            if (this.plugin != null) {
                tier.giveRewards(player, this.plugin);
            }
            saveUserData(userData);
            player.sendMessage("✔ Vous avez réclamé la récompense du " + tier.getDisplayName() + " !");
            try {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);
            } catch (Exception ignored) {}
        }
        return claimed;
    }

    public void saveUserData(@NotNull EventUserData data) {
        if (this.eventDataDao != null) {
            if (this.plugin != null) {
                this.plugin.runTaskAsync(() -> this.eventDataDao.saveUserData(data));
            } else {
                this.eventDataDao.saveUserData(data);
            }
        }
    }

    public void saveAllUserData() {
        if (this.eventDataDao != null) {
            for (EventUserData data : this.userDataCache.values()) {
                this.eventDataDao.saveUserData(data);
            }
        }
    }

    public void resetUserData(@NotNull UUID playerUuid, @NotNull String eventId) {
        String cacheKey = playerUuid + ":" + eventId.toLowerCase();
        EventUserData data = this.userDataCache.get(cacheKey);
        if (data != null) {
            data.reset();
        }
        if (this.eventDataDao != null) {
            this.eventDataDao.resetUserData(playerUuid, eventId.toLowerCase());
        }
    }

    public void openEventMenu(@NotNull Player player, @NotNull String eventId) {
        EventQuest quest = getQuest(eventId);
        if (quest == null) {
            player.sendMessage("§cÉvénement introuvable : " + eventId);
            return;
        }
        if (this.plugin != null) {
            new su.nightexpress.quests.event.menu.EventQuestMenu(this.plugin, this, quest).open(player);
        }
    }

    public void openMainMenu(@NotNull Player player) {
        List<EventQuest> active = getActiveQuests();
        if (active.size() == 1) {
            openEventMenu(player, active.get(0).getId());
        } else if (this.plugin != null) {
            new su.nightexpress.quests.event.menu.EventListMenu(this.plugin, this).open(player);
        }
    }

    @Nullable
    public EventDataDao getEventDataDao() {
        return eventDataDao;
    }
}
