package su.nightexpress.quests;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.nightcore.NightPlugin;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.config.PluginDetails;
import su.nightexpress.quests.battlepass.BattlePassManager;
import su.nightexpress.quests.command.BaseCommands;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;
import su.nightexpress.quests.data.DataHandler;
import su.nightexpress.quests.menu.MainMenu;
import su.nightexpress.quests.milestone.MilestoneManager;
import su.nightexpress.quests.reward.RewardManager;
import su.nightexpress.quests.task.TaskManager;
import su.nightexpress.quests.quest.QuestManager;
import su.nightexpress.quests.registry.Registries;
import su.nightexpress.quests.task.TaskTypeRegistry;
import su.nightexpress.quests.lore.LoreManager;
import su.nightexpress.quests.tracker.QuestTrackerManager;
import su.nightexpress.quests.user.UserManager;

import java.util.Optional;

public class QuestsPlugin extends NightPlugin {

    private DataHandler       dataHandler;
    private UserManager       userManager;
    private TaskTypeRegistry taskTypeRegistry;
    private TaskManager       taskManager;
    private RewardManager     rewardManager;
    private BattlePassManager battlePassManager;
    private MilestoneManager  milestoneManager;
    private QuestManager      questManager;
    private LoreManager       loreManager;

    @Override
    @NotNull
    protected PluginDetails getDefaultDetails() {
        String[] aliases = new String[]{"quests", "quete", "q"};
        try {
            java.io.File file = new java.io.File(this.getDataFolder(), "config.yml");
            if (file.exists()) {
                FileConfig tempConfig = new FileConfig(file);
                tempConfig.load();
                if (tempConfig.contains("General.Command_Aliases")) {
                    String raw = tempConfig.getString("General.Command_Aliases", "");
                    if (!raw.trim().isEmpty()) {
                        aliases = raw.split(",");
                        for (int i = 0; i < aliases.length; i++) {
                            aliases[i] = aliases[i].trim();
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        return PluginDetails.create("Quests", aliases)
            .setConfigClass(Config.class)
            .setPermissionsClass(Perms.class);
    }

    @Override
    protected void addRegistries() {
        this.registerLang(Lang.class);
    }

    @Override
    protected boolean disableCommandManager() {
        return true;
    }

    @Override
    protected void onStartup() {
        super.onStartup();
        QuestsAPI.load(this);

        this.taskTypeRegistry = new TaskTypeRegistry();
    }

    @Override
    public void enable() {
        Registries.setup(this);

        this.dataHandler = new DataHandler(this);
        this.dataHandler.setup();

        this.userManager = new UserManager(this, this.dataHandler);
        this.userManager.setup();

        this.taskManager = new TaskManager(this, this.taskTypeRegistry);
        this.taskManager.setup();

        this.rewardManager = new RewardManager(this);
        this.rewardManager.setup();

        if (Config.isBattlePassEnabled()) {
            this.battlePassManager = new BattlePassManager(this);
            this.battlePassManager.setup();
        }

        if (Config.FEATURES_MILESTONES_ENABLED.get()) {
            this.milestoneManager = new MilestoneManager(this);
            this.milestoneManager.setup();
        }

        if (Config.FEATURES_QUESTS_ENABLED.get()) {
            this.questManager = new QuestManager(this);
            this.questManager.setup();
        }

        if (Config.FEATURES_LORE_ENABLED.get()) {
            this.loreManager = new LoreManager(this);
            this.loreManager.setup();
        }

        QuestTrackerManager.setup(this);

        this.loadCommands();
    }

    @Override
    public void disable() {
        QuestTrackerManager.shutdown();

        if (this.taskManager != null) this.taskManager.shutdown();
        if (this.milestoneManager != null) this.milestoneManager.shutdown();
        if (this.questManager != null) this.questManager.shutdown();
        if (this.loreManager != null) this.loreManager.shutdown();
        if (this.battlePassManager != null) this.battlePassManager.shutdown();
        if (this.rewardManager != null) this.rewardManager.shutdown();
        if (this.userManager != null) this.userManager.shutdown();
        if (this.dataHandler != null) this.dataHandler.shutdown();

        Registries.shutdown();
        this.taskTypeRegistry.clear();
    }

    @Override
    protected void onShutdown() {
        super.onShutdown();

        QuestsAPI.shutdown();
    }

    private void loadCommands() {
        this.rootCommand = NightCommand.forPlugin(this, builder -> {
            BaseCommands.load(this, builder);
            builder.executes((context, arguments) -> {
                if (!context.isPlayer()) {
                    context.errorPlayerOnly();
                    return false;
                }
                Player player = context.getPlayerOrThrow();
                this.questManager().ifPresent(qm -> {
                    if (qm.getMainMenu() != null) {
                        qm.getMainMenu().open(player);
                    }
                });
                return true;
            });
        });
    }

    @NotNull
    public DataHandler getDataHandler() {
        return this.dataHandler;
    }

    @NotNull
    public UserManager getUserManager() {
        return this.userManager;
    }

    @NotNull
    public TaskTypeRegistry getTaskTypeRegistry() {
        return this.taskTypeRegistry;
    }

    @NotNull
    public TaskManager getTaskManager() {
        return this.taskManager;
    }

    @NotNull
    public RewardManager getRewardManager() {
        return this.rewardManager;
    }

    @Nullable
    public BattlePassManager getBattlePassManager() {
        return this.battlePassManager;
    }

    @NotNull
    public Optional<BattlePassManager> battlePassManager() {
        return Optional.ofNullable(this.battlePassManager);
    }

    @Nullable
    public MilestoneManager getMilestoneManager() {
        return this.milestoneManager;
    }

    @NotNull
    public Optional<MilestoneManager> milestoneManager() {
        return Optional.ofNullable(this.milestoneManager);
    }

    @Nullable
    public QuestManager getQuestManager() {
        return this.questManager;
    }

    @NotNull
    public Optional<QuestManager> questManager() {
        return Optional.ofNullable(this.questManager);
    }

    @Nullable
    public LoreManager getLoreManager() {
        return this.loreManager;
    }

    @NotNull
    public Optional<LoreManager> loreManager() {
        return Optional.ofNullable(this.loreManager);
    }

    @NotNull
    public Optional<MainMenu> mainMenu() {
        return this.questManager().map(QuestManager::getMainMenu);
    }
}
