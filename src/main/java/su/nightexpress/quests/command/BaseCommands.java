package su.nightexpress.quests.command;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.commands.Arguments;
import su.nightexpress.nightcore.commands.Commands;
import su.nightexpress.nightcore.commands.command.NightCommand;
import su.nightexpress.nightcore.core.config.CoreLang;
import su.nightexpress.quests.QuestsPlugin;
import su.nightexpress.quests.battlepass.command.BattlePassCommands;
import su.nightexpress.quests.community.command.CommunityCommands;
import su.nightexpress.quests.config.Config;
import su.nightexpress.quests.config.Lang;
import su.nightexpress.quests.config.Perms;
import su.nightexpress.quests.personal.command.PersonalQuestCommands;
import su.nightexpress.quests.quest.command.QuestsCommands;

public class BaseCommands {

    private static NightCommand adminCommand;

    public static void load(@NotNull QuestsPlugin plugin) {
        String[] aliases = parseAliases(Config.GENERAL_ADMIN_COMMAND_ALIASES.get());

        adminCommand = NightCommand.hub(plugin, aliases, builder -> {
            builder
                .permission(Perms.COMMAND_RELOAD)
                .description("DanaQuests Admin Commands")
                .branch(Commands.literal("reload")
                    .description(CoreLang.COMMAND_RELOAD_DESC)
                    .permission(Perms.COMMAND_RELOAD)
                    .executes((context, arguments) -> {
                        plugin.doReload(context.getSender());
                        return true;
                    })
                )
                .branch(Commands.literal("refresh")
                    .permission(Perms.COMMAND_QUESTS_REFRESH)
                    .description(Lang.COMMAND_QUESTS_REFRESH_DESC)
                    .withArguments(Arguments.playerName("player"))
                    .executes(QuestsCommands::refreshQuests)
                );

            if (Config.FEATURES_BATTLE_PASS_ENABLED.get()) {
                builder.branch(Commands.hub("battlepass")
                    .permission(Perms.COMMAND_BATTLE_PASS)
                    .branch(Commands.literal("start")
                        .permission(Perms.COMMAND_BATTLE_PASS_START)
                        .withArguments(Arguments.string("name"), Arguments.integer("duration", 1, 365))
                        .executes(BattlePassCommands::scheduleSeason)
                    )
                    .branch(Commands.literal("cancel")
                        .permission(Perms.COMMAND_BATTLE_PASS_CANCEL)
                        .executes(BattlePassCommands::cancelSeason)
                    )
                    .branch(Commands.literal("addlevel")
                        .permission(Perms.COMMAND_BATTLE_PASS_ADD_LEVEL)
                        .withArguments(Arguments.integer("amount", 1), Arguments.playerName("player").optional())
                        .executes(BattlePassCommands::addLevel)
                    )
                    .branch(Commands.literal("setlevel")
                        .permission(Perms.COMMAND_BATTLE_PASS_SET_LEVEL)
                        .withArguments(Arguments.integer("amount", 0), Arguments.playerName("player").optional())
                        .executes(BattlePassCommands::setLevel)
                    )
                );
            }

            if (Config.FEATURES_PERSONAL_QUESTS_ENABLED.get()) {
                builder.branch(Commands.literal("personal")
                    .permission(Perms.COMMAND_PERSONAL)
                    .withArguments(
                        Arguments.string("action").optional(),
                        Arguments.playerName("player").optional(),
                        Arguments.string("category").optional(),
                        Arguments.integer("level").optional()
                    )
                    .executes(PersonalQuestCommands::executePersonalCommand)
                );
            }

            if (Config.FEATURES_COMMUNITY_QUESTS_ENABLED.get()) {
                builder.branch(Commands.hub("community")
                    .permission(Perms.COMMAND_COMMUNITY)
                    .branch(Commands.literal("start")
                        .permission(Perms.ADMIN_COMMUNITY)
                        .withArguments(Arguments.string("quest_id"), Arguments.integer("duration").optional(), Arguments.decimal("target").optional())
                        .executes(CommunityCommands::startEvent)
                    )
                    .branch(Commands.literal("stop")
                        .permission(Perms.ADMIN_COMMUNITY)
                        .executes(CommunityCommands::stopEvent)
                    )
                );
            }
        });
        adminCommand.register();
    }

    public static void shutdown() {
        if (adminCommand != null) {
            adminCommand.unregister();
            adminCommand = null;
        }
    }

    private static String[] parseAliases(String raw) {
        if (raw == null || raw.trim().isEmpty()) return new String[]{"danaquests", "dquests", "dq"};
        String[] split = raw.split(",");
        for (int i = 0; i < split.length; i++) {
            split[i] = split[i].trim();
        }
        return split;
    }
}
