package su.nightexpress.quests.config;

import su.nightexpress.nightcore.config.ConfigValue;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.nightcore.util.RankTable;
import su.nightexpress.quests.battlepass.command.BattlePassCommands;
import su.nightexpress.quests.lore.command.LoreCommands;
import su.nightexpress.quests.milestone.command.MilestoneCommands;
import su.nightexpress.quests.quest.command.QuestsCommands;
import su.nightexpress.quests.util.QuestUtils;

import java.util.Set;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class Config {

    public static final String DIR_DAILY      = "/daily/";
    public static final String DIR_BATTLEPASS = "/battlepass/";
    public static final String DIR_COMMUNITY  = "/community/";
    public static final String DIR_LORE       = "/lore/";
    public static final String DIR_MILESTONES = "/milestones/";
    public static final String DIR_ISLAND     = "/island/";
    public static final String DIR_PERSONAL   = "/personal/";

    public static final String DIR_MENU            = "/menu/";
    public static final String DIR_MENU_DAILY      = "/menu/daily/";
    public static final String DIR_MENU_BATTLEPASS = "/menu/battlepass/";
    public static final String DIR_MENU_COMMUNITY  = "/menu/community/";
    public static final String DIR_MENU_LORE       = "/menu/lore/";
    public static final String DIR_MENU_MILESTONES = "/menu/milestones/";
    public static final String DIR_MENU_ISLAND     = "/menu/island/";
    public static final String DIR_MENU_PERSONAL   = "/menu/personal/";

    public static final ConfigValue<String> GENERAL_DATE_TIME_FORMAT = ConfigValue.create("General.DateTimeFormat",
        "dd/MM/yyyy HH:mm",
        "Sets date time format."
    ).whenRead(QuestUtils::setDateTimeFormatter);

    public static final ConfigValue<String> GENERAL_COMMAND_ALIASES = ConfigValue.create("General.Command_Aliases",
        "quests,quete,q",
        "Main command aliases for players"
    );

    public static final ConfigValue<String> GENERAL_ADMIN_COMMAND_ALIASES = ConfigValue.create("General.Admin_Command_Aliases",
        "danaquests,dquests,dq",
        "Main command aliases for administrators"
    );

    public static final ConfigValue<Boolean> FEATURES_BATTLE_PASS_ENABLED = ConfigValue.create("Features.BattlePass.Enabled",
        true,
        "Enables the Battle Pass feature."
    );

    public static final ConfigValue<Boolean> FEATURES_BATTLE_PASS_STANDALONE_COMMAND = ConfigValue.create("Features.BattlePass.Standalone_Command",
        true,
        "Whether the Battle Pass command can be run standalone directly without the main command prefix."
    );

    public static final ConfigValue<String[]> FEATURES_BATTLE_PASS_ALIASES = ConfigValue.create("Features.BattlePass.Aliases",
        new String[]{BattlePassCommands.DEFAULT_ALIAS, "bp"},
        "Command aliases for the Battle Pass feature.",
        "[*] Server reboot is highly recommended when changed."
    );

    public static final ConfigValue<Boolean> FEATURES_QUESTS_ENABLED = ConfigValue.create("Features.DailyQuests.Enabled",
        true,
        "Enables the Daily Quests feature."
    );

    public static final ConfigValue<Boolean> FEATURES_QUESTS_STANDALONE_COMMAND = ConfigValue.create("Features.DailyQuests.Standalone_Command",
        true,
        "Whether the Daily Quests command can be run standalone directly without the main command prefix."
    );

    public static final ConfigValue<String[]> FEATURES_QUESTS_ALIASES = ConfigValue.create("Features.DailyQuests.Aliases",
        new String[]{"dailyquests", "dquests", "daily"},
        "Command aliases for the Daily Quests feature.",
        "[*] Server reboot is highly recommended when changed."
    );

    public static final ConfigValue<Boolean> FEATURES_MILESTONES_ENABLED = ConfigValue.create("Features.Milestones.Enabled",
        true,
        "Enables the milestones feature."
    );

    public static final ConfigValue<Boolean> FEATURES_MILESTONES_STANDALONE_COMMAND = ConfigValue.create("Features.Milestones.Standalone_Command",
        true,
        "Whether the Milestones command can be run standalone directly without the main command prefix."
    );

    public static final ConfigValue<String[]> FEATURES_MILESTONES_ALIASES = ConfigValue.create("Features.Milestones.Aliases",
        new String[]{MilestoneCommands.DEFAULT_ALIAS, "ms"},
        "Command aliases for the Milestones feature.",
        "[*] Server reboot is highly recommended when changed."
    );

    public static final ConfigValue<Boolean> FEATURES_LORE_ENABLED = ConfigValue.create("Features.LoreQuests.Enabled",
        true,
        "Enables the Lore Quests feature."
    );

    public static final ConfigValue<Boolean> FEATURES_LORE_STANDALONE_COMMAND = ConfigValue.create("Features.LoreQuests.Standalone_Command",
        true,
        "Whether the Lore Quests command can be run standalone directly without the main command prefix."
    );

    public static final ConfigValue<String[]> FEATURES_LORE_ALIASES = ConfigValue.create("Features.LoreQuests.Aliases",
        new String[]{LoreCommands.DEFAULT_ALIAS, "lq", "lore"},
        "Command aliases for the Lore Quests feature.",
        "[*] Server reboot is highly recommended when changed."
    );

    public static final ConfigValue<Boolean> FEATURES_ISLAND_QUESTS_ENABLED = ConfigValue.create("Features.IslandQuests.Enabled",
        true,
        "Enables the Island Quests feature."
    );

    public static final ConfigValue<Boolean> FEATURES_ISLAND_QUESTS_STANDALONE_COMMAND = ConfigValue.create("Features.IslandQuests.Standalone_Command",
        true,
        "Whether the Island Quests command can be run standalone directly without the main command prefix."
    );

    public static final ConfigValue<String[]> FEATURES_ISLAND_QUESTS_ALIASES = ConfigValue.create("Features.IslandQuests.Aliases",
        new String[]{"island", "is"},
        "Command aliases for the Island Quests feature."
    );

    public static final ConfigValue<Boolean> FEATURES_PERSONAL_QUESTS_ENABLED = ConfigValue.create("Features.PersonalQuests.Enabled",
        true,
        "Enables the Personal Quests feature."
    );

    public static final ConfigValue<Boolean> FEATURES_PERSONAL_QUESTS_STANDALONE_COMMAND = ConfigValue.create("Features.PersonalQuests.Standalone_Command",
        true,
        "Whether the Personal Quests command can be run standalone directly without the main command prefix."
    );

    public static final ConfigValue<String[]> FEATURES_PERSONAL_QUESTS_ALIASES = ConfigValue.create("Features.PersonalQuests.Aliases",
        new String[]{"personal", "rpg", "pq"},
        "Command aliases for the Personal Quests feature."
    );

    public static final ConfigValue<Boolean> FEATURES_COMMUNITY_QUESTS_ENABLED = ConfigValue.create("Features.CommunityQuests.Enabled",
        true,
        "Enables the Community Quests feature."
    );

    public static final ConfigValue<Boolean> FEATURES_COMMUNITY_QUESTS_STANDALONE_COMMAND = ConfigValue.create("Features.CommunityQuests.Standalone_Command",
        true,
        "Whether the Community Quests command can be run standalone directly without the main command prefix."
    );

    public static final ConfigValue<String[]> FEATURES_COMMUNITY_QUESTS_ALIASES = ConfigValue.create("Features.CommunityQuests.Aliases",
        new String[]{"community", "cq"},
        "Command aliases for the Community Quests feature."
    );

    public static final ConfigValue<Boolean> DISCORD_WEBHOOK_ENABLED = ConfigValue.create("discord-webhook.enabled",
        true,
        "Enables Discord Webhook notifications for Community Events."
    );

    public static final ConfigValue<String> DISCORD_WEBHOOK_URL = ConfigValue.create("discord-webhook.url",
        "https://discord.com/api/webhooks/YOUR_WEBHOOK_URL",
        "Discord Webhook URL."
    );

    public static final ConfigValue<String> DISCORD_WEBHOOK_AVATAR_URL = ConfigValue.create("discord-webhook.avatar-url",
        "https://i.imgur.com/4M34hi2.png",
        "Avatar URL for Discord Webhook messages."
    );

    public static final ConfigValue<String> DISCORD_WEBHOOK_USERNAME = ConfigValue.create("discord-webhook.username",
        "DanaQuests - Événements",
        "Username for Discord Webhook messages."
    );

    public static final ConfigValue<Integer> DISCORD_WEBHOOK_COLOR_START = ConfigValue.create("discord-webhook.colors.start",
        3447003,
        "Decimal color code for event start embed."
    );

    public static final ConfigValue<Integer> DISCORD_WEBHOOK_COLOR_SUCCESS = ConfigValue.create("discord-webhook.colors.success",
        3066993,
        "Decimal color code for event victory embed."
    );

    public static final ConfigValue<Integer> DISCORD_WEBHOOK_COLOR_FAILED = ConfigValue.create("discord-webhook.colors.failed",
        15158332,
        "Decimal color code for event failure embed."
    );

    public static final ConfigValue<String> COMMUNITY_LOGS_PATH = ConfigValue.create("community-quests.logs-path",
        "logs/community_events",
        "Relative path for community event log files."
    );

    public static final ConfigValue<String> SOUNDS_LEVEL_UP = ConfigValue.create("sounds.level-up",
        "ENTITY_PLAYER_LEVELUP",
        "Sound played when RPG category levels up."
    );

    public static final ConfigValue<RankTable> PERSONAL_QUESTS_DAILY_LIMITS = ConfigValue.create("personal-quests.daily-limits",
        RankTable::read,
        RankTable.builder(RankTable.Mode.RANK, 3)
            .addRankValue("vip", 5)
            .addRankValue("admin", 999)
            .build(),
        "Daily limits of personal quests for players based on their rank or permissions."
    );

    public static final ConfigValue<String> PERSONAL_QUESTS_MONEY_COMMAND = ConfigValue.create("personal-quests.money-command",
        "eco give %player% %money%",
        "Command executed to give money to the player upon quest completion."
    );

    public static final ConfigValue<String> SOUNDS_DEPOSIT = ConfigValue.create("sounds.deposit",
        "ENTITY_EXPERIENCE_ORB_PICKUP",
        "Sound played when resource is deposited."
    );

    public static final ConfigValue<String> SOUNDS_ISLAND_QUEST_COMPLETED = ConfigValue.create("sounds.island_quest_completed",
        "UI_TOAST_CHALLENGE_COMPLETE",
        "Sound played when island quest level is completed."
    );

    public static final ConfigValue<Set<String>> INTERGRATIONS_DISABLED = ConfigValue.create("Integrations.Disabled",
        Lists.newSet("PluginName", "AnotherPlugin"),
        "List here plugin names that that you want to disable integrations for."
    );

    public static final ConfigValue<Boolean> MILESTONES_RESET_PROGRESS = ConfigValue.create("Milestones.ResetProgress",
        false,
        "Controls whether milestone's progress will be reset for every next level."
    );

    public static final ConfigValue<Boolean> QUESTS_BATTLE_PASS_MODE = ConfigValue.create("Quests.BattlePassMode",
        false,
        "When set on 'true', daily quests are only available during the Battle Pass season."
    );

    public static final ConfigValue<Boolean> QUESTS_ACCEPTION_REQUIRED = ConfigValue.create("Quests.AcceptionRequired",
        true,
    "Controls whether players must accept a quest to start progress in it."
    );

    public static final ConfigValue<Boolean> QUESTS_AUTO_COMPLETION_TIME = ConfigValue.create("Quests.AutoCompletionTime",
        false,
        "Controls whether quests's completion time will be linked with the quests refresh time."
    );

    public static final ConfigValue<RankTable> QUESTS_AMOUT_PER_RANK = ConfigValue.create("Quests.AmountPerRank",
        RankTable::read,
        RankTable.builder(RankTable.Mode.RANK, 3).addRankValue("vip", 4).addRankValue("premium", 5).build(),
        "Amount of randomly generated daily quests for players based on their rank/permissions."
    );

    public static final ConfigValue<Boolean> ANTI_ABUSE_COUNT_PLAYER_BLOCKS = ConfigValue.create("AntiAbuse.CountPlayerBlocks",
        false,
        "Whether to count blocks placed by players for block related quests and milestones."
    );

    public static final ConfigValue<Boolean> ANTI_ABUSE_COUNT_ARTIFICAL_MOBS = ConfigValue.create("AntiAbuse.CountArtificallySpawnedMobs",
        false,
        "Whether to count mobs spawned artifically for mob related quests and milestones."
    );

    public static final ConfigValue<Set<String>> ANTI_ABUSE_ARTIFICAL_MOB_SPAWNS = ConfigValue.create("AntiAbuse.ArtificalMobSpawns",
        Lists.newSet(
            SpawnReason.EGG.name(),
            SpawnReason.SPAWNER.name(),
            SpawnReason.SPAWNER_EGG.name(),
            SpawnReason.DISPENSE_EGG.name(),
            SpawnReason.TRIAL_SPAWNER.name(),
            SpawnReason.BUILD_SNOWMAN.name(),
            SpawnReason.BUILD_IRONGOLEM.name()
        ),
        "List of spawn reasons considered artifical.",
        "https://jd.papermc.io/paper/1.21.8/org/bukkit/event/entity/CreatureSpawnEvent.SpawnReason.html"
    );

    public static final ConfigValue<Boolean> ANTI_ABUSE_COUNT_IN_VEHICLES = ConfigValue.create("AntiAbuse.CountInVehicles",
        true,
        "Whether to count quests/milestones progress for players in vehicles (minecarts, boats, etc.)."
    );

    public static final ConfigValue<Boolean> ANTI_ABUSE_COUNT_AUTO_COOKING = ConfigValue.create("AntiAbuse.CountAutoCooking",
        true,
        "Whether to count quests/milestones progress for automated brewing, cooking and smelting."
    );

    public static final ConfigValue<Integer> PROGRESS_BAR_LENGTH = ConfigValue.create("progress-bar.length",
        10,
        "Total number of symbols in the progress bar."
    );

    public static final ConfigValue<String> PROGRESS_BAR_SYMBOL = ConfigValue.create("progress-bar.symbol",
        "■",
        "Symbol used for progress bar."
    );

    public static final ConfigValue<String> PROGRESS_BAR_COLOR_FILLED = ConfigValue.create("progress-bar.color-filled",
        "<gradient:#2ecc71:#a3cb38>",
        "Color/Gradient for filled portion (MiniMessage format)."
    );

    public static final ConfigValue<String> PROGRESS_BAR_COLOR_EMPTY = ConfigValue.create("progress-bar.color-empty",
        "<gray>",
        "Color for empty portion (MiniMessage format)."
    );

    public static final ConfigValue<Boolean> PROGRESS_BAR_SHOW_PERCENTAGE_INSIDE = ConfigValue.create("progress-bar.show-percentage-inside",
        false,
        "Displays percentage inside the progress bar."
    );

    public static final ConfigValue<String> ISLAND_WEIGHT_FORMAT = ConfigValue.create("island.weight-format",
        "<gray>- </gray><white>%material%</white><gray>: </gray><lyellow>%weight% pts</lyellow>",
        "Format for each resource entry in %weight_lore%."
    );

    public static final ConfigValue<Integer> UI_PROGRESS_BAR_LENGTH = PROGRESS_BAR_LENGTH;
    public static final ConfigValue<String> UI_PROGRESS_BAR_CHAR = PROGRESS_BAR_SYMBOL;
    public static final ConfigValue<String> UI_PROGRESS_BAR_COLOR_FILL = PROGRESS_BAR_COLOR_FILLED;
    public static final ConfigValue<String> UI_PROGRESS_BAR_COLOR_EMPTY = PROGRESS_BAR_COLOR_EMPTY;

    public static boolean isMilestonesResetProgress() {
        return MILESTONES_RESET_PROGRESS.get();
    }

    public static boolean isBattlePassEnabled() {
        return FEATURES_BATTLE_PASS_ENABLED.get();
    }

    public static boolean isQuestsEnabled() {
        return FEATURES_QUESTS_ENABLED.get();
    }

    public static boolean isQuestsForBattlePass() {
        return QUESTS_BATTLE_PASS_MODE.get();
    }

    public static final ConfigValue<Boolean> TRACKER_ENABLED = ConfigValue.create("tracker.enabled",
        true,
        "Enables the quest tracking system."
    );

    public static final ConfigValue<String> TRACKER_DEFAULT_MODE = ConfigValue.create("tracker.default-mode",
        "BOSS_BAR",
        "Sets the default tracker mode for new players. Options: BOSS_BAR, ACTION_BAR, CHAT, NONE"
    );

    public static final ConfigValue<String> TRACKER_BOSSBAR_COLOR = ConfigValue.create("tracker.bossbar.color",
        "GREEN",
        "Sets BossBar color. Options: BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW"
    );

    public static final ConfigValue<String> TRACKER_BOSSBAR_STYLE = ConfigValue.create("tracker.bossbar.style",
        "PROGRESS",
        "Sets BossBar style. Options: PROGRESS, NOTCHED_6, NOTCHED_10, NOTCHED_12, NOTCHED_20"
    );

    public static final ConfigValue<Integer> TRACKER_BOSSBAR_DISPLAY_DURATION = ConfigValue.create("tracker.bossbar.display-duration-seconds",
        5,
        "Time in seconds the BossBar is displayed."
    );

    public static final ConfigValue<Integer> TRACKER_BOSSBAR_SWITCHING_INTERVAL = ConfigValue.create("tracker.bossbar.switching-interval-seconds",
        3,
        "Interval in seconds to switch between active quest progress bars."
    );

    public static final ConfigValue<Integer> TRACKER_ACTIONBAR_DISPLAY_DURATION = ConfigValue.create("tracker.actionbar.display-duration-seconds",
        5,
        "Time in seconds the ActionBar is displayed."
    );

    // formats bossbar
    public static final ConfigValue<String> TRACKER_FORMATS_BOSSBAR_LORE = ConfigValue.create("tracker.formats.bossbar.lore",
        "<blue>[Histoire] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    public static final ConfigValue<String> TRACKER_FORMATS_BOSSBAR_ISLAND = ConfigValue.create("tracker.formats.bossbar.island",
        "<purple>[Île] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    public static final ConfigValue<String> TRACKER_FORMATS_BOSSBAR_PERSONAL = ConfigValue.create("tracker.formats.bossbar.personal",
        "<green>[Quête] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    // formats actionbar
    public static final ConfigValue<String> TRACKER_FORMATS_ACTIONBAR_LORE = ConfigValue.create("tracker.formats.actionbar.lore",
        "<blue>[Histoire] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    public static final ConfigValue<String> TRACKER_FORMATS_ACTIONBAR_ISLAND = ConfigValue.create("tracker.formats.actionbar.island",
        "<purple>[Île] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    public static final ConfigValue<String> TRACKER_FORMATS_ACTIONBAR_PERSONAL = ConfigValue.create("tracker.formats.actionbar.personal",
        "<green>[Quête] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    // formats chat
    public static final ConfigValue<String> TRACKER_FORMATS_CHAT_LORE = ConfigValue.create("tracker.formats.chat.lore",
        "<blue>[Histoire] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    public static final ConfigValue<String> TRACKER_FORMATS_CHAT_ISLAND = ConfigValue.create("tracker.formats.chat.island",
        "<purple>[Île] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );

    public static final ConfigValue<String> TRACKER_FORMATS_CHAT_PERSONAL = ConfigValue.create("tracker.formats.chat.personal",
        "<green>[Quête] <yellow>%quest% <gray>- <white>%progress%/%required%"
    );
}
