package su.nightexpress.quests.community;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.manager.AbstractManager;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.community.command.CommunityCommands;
import su.nightexpress.quests.community.data.CommunityActiveEvent;
import su.nightexpress.quests.community.definition.CommunityQuest;
import su.nightexpress.quests.community.definition.RankingRewards;
import su.nightexpress.quests.community.listener.CommunityQuestListener;
import su.nightexpress.quests.community.log.CommunityLogger;
import su.nightexpress.quests.community.menu.CommunityLeaderboardMenu;
import su.nightexpress.quests.community.webhook.DiscordWebhookSender;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.stream.Collectors;

public class CommunityQuestManager extends AbstractManager<QuestsPlugin> {

    private final Map<String, CommunityQuest> communityQuests;
    private CommunityActiveEvent activeEvent;
    private CommunityLeaderboardMenu leaderboardMenu;
    private BukkitTask timerTask;

    public CommunityQuestManager(@NotNull QuestsPlugin plugin) {
        super(plugin);
        this.communityQuests = new LinkedHashMap<>();
    }

    @Override
    protected void onLoad() {
        CommunityQuestDefaults.createDefaults(this.plugin);
        this.loadQuests();
        this.loadActiveEvent();

        this.leaderboardMenu = this.addMenu(new CommunityLeaderboardMenu(this.plugin, this), Config.DIR_MENU_COMMUNITY, "community_leaderboard.yml");

        Bukkit.getPluginManager().registerEvents(new CommunityQuestListener(this.plugin, this), this.plugin);

        CommunityCommands.load(this.plugin);

        this.startTimerTask();
    }

    @Override
    protected void onShutdown() {
        if (this.timerTask != null) {
            this.timerTask.cancel();
            this.timerTask = null;
        }

        this.saveActiveEvent();

        CommunityCommands.shutdown();
        this.communityQuests.clear();
        this.activeEvent = null;
        this.leaderboardMenu = null;
    }

    public void loadQuests() {
        this.communityQuests.clear();
        File file = new File(this.plugin.getDataFolder() + Config.DIR_COMMUNITY, "community_quests.yml");
        if (!file.exists()) return;

        try {
            FileConfig config = new FileConfig(file);
            config.load();

            if (config.contains("quests")) {
                for (String questId : config.getSection("quests")) {
                    String path = "quests." + questId;
                    String displayName = config.getString(path + ".display_name", questId);
                    List<String> description = config.getStringList(path + ".description");
                    String type = config.getString(path + ".type", "MONEY_DEPOSIT");
                    double defaultTarget = config.getDouble(path + ".default_target", 100000.0);
                    int defaultDurationHours = config.getInt(path + ".default_duration_hours", 24);
                    String iconStr = config.getString(path + ".icon", "GOLD_INGOT");
                    Material icon = Material.matchMaterial(iconStr);
                    if (icon == null) icon = Material.PAPER;
                    int customModelData = config.getInt(path + ".custom_model_data", 0);
                    List<String> globalRewards = config.getStringList(path + ".global_rewards");

                    List<String> top1 = config.getStringList(path + ".ranking_rewards.top_1");
                    List<String> top2 = config.getStringList(path + ".ranking_rewards.top_2");
                    List<String> top3 = config.getStringList(path + ".ranking_rewards.top_3");
                    double partMin = config.getDouble(path + ".ranking_rewards.participation_min_contribution", 0.0);
                    List<String> partRewards = config.getStringList(path + ".ranking_rewards.participation");

                    RankingRewards rewards = new RankingRewards(top1, top2, top3, partMin, partRewards);
                    CommunityQuest quest = new CommunityQuest(questId, displayName, description, type, defaultTarget, defaultDurationHours, icon, customModelData, globalRewards, rewards);

                    this.communityQuests.put(questId, quest);
                }
            }
            this.plugin.info("Loaded " + this.communityQuests.size() + " community quests.");
        } catch (Exception e) {
            this.plugin.error("Failed to load community_quests.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void loadActiveEvent() {
        File file = new File(this.plugin.getDataFolder(), "community_active_event.json");
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            StringBuilder sb = new StringBuilder();
            char[] buf = new char[1024];
            int numRead;
            while ((numRead = reader.read(buf)) != -1) {
                sb.append(buf, 0, numRead);
            }
            this.activeEvent = CommunityActiveEvent.fromJson(sb.toString());
        } catch (Exception e) {
            this.plugin.error("Failed to load community_active_event.json: " + e.getMessage());
        }
    }

    public void saveActiveEvent() {
        if (this.activeEvent == null) return;
        File file = new File(this.plugin.getDataFolder(), "community_active_event.json");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(this.activeEvent.toJson());
        } catch (Exception e) {
            this.plugin.error("Failed to save community_active_event.json: " + e.getMessage());
        }
    }

    private void startTimerTask() {
        this.timerTask = Bukkit.getScheduler().runTaskTimer(this.plugin, this::checkEventState, 20L, 20L * 5);
    }

    public synchronized void checkEventState() {
        if (this.activeEvent == null || !this.activeEvent.isActive()) return;

        if (this.activeEvent.getCurrentAmount() >= this.activeEvent.getTargetAmount()) {
            this.finishEvent(true);
        } else if (System.currentTimeMillis() >= this.activeEvent.getEndTimestamp()) {
            this.finishEvent(false);
        }
    }

    public boolean startEvent(@NotNull CommandSender sender, @NotNull String questId, @Nullable Integer overrideDurationHours, @Nullable Double overrideTargetAmount) {
        CommunityQuest quest = this.communityQuests.get(questId);
        if (quest == null) {
            Lang.COMMUNITY_INVALID_QUEST.message().send(sender);
            return false;
        }

        if (this.activeEvent != null && this.activeEvent.isActive()) {
            Lang.COMMUNITY_EVENT_ALREADY_ACTIVE.message().send(sender);
            return false;
        }

        double target = overrideTargetAmount != null && overrideTargetAmount > 0 ? overrideTargetAmount : quest.getDefaultTarget();
        int durationHours = overrideDurationHours != null && overrideDurationHours > 0 ? overrideDurationHours : quest.getDefaultDurationHours();

        long start = System.currentTimeMillis();
        long end = start + (durationHours * 3600000L);

        this.activeEvent = new CommunityActiveEvent(questId, target, 0.0, start, end, new HashMap<>(), false, true);
        this.saveActiveEvent();

        Lang.COMMUNITY_EVENT_STARTED.message().send(sender, replacer -> replacer
            .replace("%quest%", quest.getDisplayName())
            .replace("%target%", String.format("%.0f", target))
            .replace("%duration%", String.valueOf(durationHours))
        );

        DiscordWebhookSender.sendEventStart(quest, this.activeEvent);
        return true;
    }

    public boolean stopEvent(@NotNull CommandSender sender) {
        if (this.activeEvent == null || !this.activeEvent.isActive()) {
            Lang.COMMUNITY_EVENT_NO_ACTIVE.message().send(sender);
            return false;
        }

        this.activeEvent.setActive(false);
        this.saveActiveEvent();

        Lang.COMMUNITY_EVENT_STOPPED.message().send(sender);
        return true;
    }

    public synchronized boolean deposit(@NotNull Player player, double amount) {
        if (this.activeEvent == null || !this.activeEvent.isActive()) {
            Lang.COMMUNITY_EVENT_NO_ACTIVE.message().send(player);
            return false;
        }

        CommunityQuest quest = getQuest(this.activeEvent.getQuestId());
        if (quest != null && !"MONEY_DEPOSIT".equalsIgnoreCase(quest.getType())) {
            Lang.COMMUNITY_DEPOSIT_NOT_MONEY_TYPE.message().send(player);
            return false;
        }

        double remaining = this.activeEvent.getTargetAmount() - this.activeEvent.getCurrentAmount();
        if (remaining <= 0) {
            return false;
        }

        double toDeposit = Math.min(amount, remaining);
        if (toDeposit <= 0) return false;

        if (!VaultHook.withdraw(player, toDeposit)) {
            Lang.COMMUNITY_DEPOSIT_NOT_ENOUGH_MONEY.message().send(player);
            return false;
        }

        this.activeEvent.addContribution(player.getUniqueId(), toDeposit);
        this.saveActiveEvent();

        try {
            String soundStr = Config.SOUNDS_DEPOSIT.get();
            player.playSound(player.getLocation(), Sound.valueOf(soundStr.toUpperCase()), 1.0f, 1.0f);
        } catch (Exception ignored) {
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
        }

        double yourTotal = this.activeEvent.getContribution(player.getUniqueId());
        double currentGlobal = this.activeEvent.getCurrentAmount();
        double targetGlobal = this.activeEvent.getTargetAmount();

        Lang.COMMUNITY_CONTRIBUTED.message().send(player, replacer -> replacer
            .replace("%amount%", String.format("%.0f", toDeposit))
            .replace("%total_player%", String.format("%.0f", yourTotal))
            .replace("%current_global%", String.format("%.0f", currentGlobal))
            .replace("%target_global%", String.format("%.0f", targetGlobal))
        );

        if (this.activeEvent.getCurrentAmount() >= this.activeEvent.getTargetAmount()) {
            this.finishEvent(true);
        }
        return true;
    }

    public void depositMax(@NotNull Player player) {
        double balance = VaultHook.getBalance(player);
        if (balance > 0) {
            this.deposit(player, balance);
        }
    }

    public synchronized void addGlobalProgress(@NotNull Player player, double amount, @NotNull String questType) {
        if (this.activeEvent == null || !this.activeEvent.isActive()) return;

        CommunityQuest quest = getQuest(this.activeEvent.getQuestId());
        if (quest == null || !questType.equalsIgnoreCase(quest.getType())) return;

        double remaining = this.activeEvent.getTargetAmount() - this.activeEvent.getCurrentAmount();
        if (remaining <= 0) return;

        double toAdd = Math.min(amount, remaining);
        if (toAdd <= 0) return;

        this.activeEvent.addContribution(player.getUniqueId(), toAdd);
        this.saveActiveEvent();

        if (this.activeEvent.getCurrentAmount() >= this.activeEvent.getTargetAmount()) {
            this.finishEvent(true);
        }
    }

    public synchronized void finishEvent(boolean success) {
        if (this.activeEvent == null) return;

        this.activeEvent.setActive(false);
        this.activeEvent.setCompleted(success);
        this.saveActiveEvent();

        CommunityQuest quest = getQuest(this.activeEvent.getQuestId());
        List<Map.Entry<UUID, Double>> sortedRankings = getTopContributors();
        List<Map.Entry<String, Double>> namedRankings = getTopContributorsNamed();

        if (success) {
            Lang.COMMUNITY_EVENT_SUCCESS.message().send(Bukkit.getConsoleSender(), replacer -> replacer
                .replace("%quest%", quest != null ? quest.getDisplayName() : this.activeEvent.getQuestId())
            );
            Bukkit.getOnlinePlayers().forEach(p -> Lang.COMMUNITY_EVENT_SUCCESS.message().send(p, replacer -> replacer
                .replace("%quest%", quest != null ? quest.getDisplayName() : this.activeEvent.getQuestId())
            ));

            if (quest != null) {
                distributeRewards(quest, sortedRankings);
            }

            DiscordWebhookSender.sendEventVictory(quest, this.activeEvent, namedRankings);
        } else {
            Lang.COMMUNITY_EVENT_FAILED.message().send(Bukkit.getConsoleSender(), replacer -> replacer
                .replace("%quest%", quest != null ? quest.getDisplayName() : this.activeEvent.getQuestId())
            );
            Bukkit.getOnlinePlayers().forEach(p -> Lang.COMMUNITY_EVENT_FAILED.message().send(p, replacer -> replacer
                .replace("%quest%", quest != null ? quest.getDisplayName() : this.activeEvent.getQuestId())
            ));

            DiscordWebhookSender.sendEventFailure(quest, this.activeEvent, namedRankings);
        }

        CommunityLogger.logEvent(this.plugin, quest, this.activeEvent, namedRankings);
    }

    private void distributeRewards(@NotNull CommunityQuest quest, @NotNull List<Map.Entry<UUID, Double>> rankings) {
        // Global rewards
        for (String cmd : quest.getGlobalRewardCommands()) {
            if (cmd.contains("%all%")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%all%", p.getName()));
                }
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
            }
        }

        RankingRewards rr = quest.getRankingRewards();
        if (rr == null) return;

        // Top 1
        if (!rankings.isEmpty()) {
            executePlayerCommands(rankings.get(0).getKey(), rr.getTop1Commands());
        }
        // Top 2
        if (rankings.size() > 1) {
            executePlayerCommands(rankings.get(1).getKey(), rr.getTop2Commands());
        }
        // Top 3
        if (rankings.size() > 2) {
            executePlayerCommands(rankings.get(2).getKey(), rr.getTop3Commands());
        }

        // Participation rewards
        double minContr = rr.getParticipationMinContribution();
        List<String> partCmds = rr.getParticipationCommands();
        if (!partCmds.isEmpty()) {
            for (Map.Entry<UUID, Double> entry : rankings) {
                if (entry.getValue() >= minContr) {
                    executePlayerCommands(entry.getKey(), partCmds);
                }
            }
        }
    }

    private void executePlayerCommands(UUID playerUuid, List<String> commands) {
        if (playerUuid == null || commands == null || commands.isEmpty()) return;
        String pName = Bukkit.getOfflinePlayer(playerUuid).getName();
        if (pName == null) pName = playerUuid.toString();

        for (String cmd : commands) {
            String parsed = cmd.replace("%player%", pName);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsed);
        }
    }

    @NotNull
    public List<Map.Entry<UUID, Double>> getTopContributors() {
        if (this.activeEvent == null || this.activeEvent.getContributions() == null) {
            return Collections.emptyList();
        }
        return this.activeEvent.getContributions().entrySet().stream()
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .collect(Collectors.toList());
    }

    @NotNull
    public List<Map.Entry<String, Double>> getTopContributorsNamed() {
        List<Map.Entry<UUID, Double>> top = getTopContributors();
        List<Map.Entry<String, Double>> result = new ArrayList<>();
        for (Map.Entry<UUID, Double> entry : top) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            if (name == null) name = entry.getKey().toString();
            result.add(new AbstractMap.SimpleEntry<>(name, entry.getValue()));
        }
        return result;
    }

    @Nullable
    public CommunityActiveEvent getActiveEvent() {
        return activeEvent;
    }

    public void setActiveEvent(CommunityActiveEvent activeEvent) {
        this.activeEvent = activeEvent;
    }

    @NotNull
    public Map<String, CommunityQuest> getCommunityQuests() {
        return communityQuests;
    }

    @Nullable
    public CommunityQuest getQuest(String questId) {
        return this.communityQuests.get(questId);
    }

    public void openLeaderboardMenu(@NotNull Player player) {
        if (this.leaderboardMenu != null) {
            this.leaderboardMenu.open(player);
        }
    }
}
