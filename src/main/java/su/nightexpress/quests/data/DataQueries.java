package su.nightexpress.quests.data;

import com.google.common.reflect.TypeToken;
import su.nightexpress.nightcore.db.sql.query.impl.InsertQuery;
import su.nightexpress.nightcore.db.sql.query.impl.UpdateQuery;
import su.nightexpress.quests.battlepass.definition.BattlePassSeason;
import su.nightexpress.quests.milestone.data.MilestoneData;
import su.nightexpress.quests.quest.data.QuestData;
import su.nightexpress.quests.battlepass.data.BattlePassData;
import su.nightexpress.quests.lore.data.LoreQuestData;
import su.nightexpress.quests.personal.data.PersonalQuestData;
import su.nightexpress.quests.user.QuestUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class DataQueries {

    public static final Function<ResultSet, QuestUser> USER_LOADER = resultSet -> {
        try {
            UUID uuid = UUID.fromString(resultSet.getString(DataHandler.COLUMN_USER_ID.getName()));
            String name = resultSet.getString(DataHandler.COLUMN_USER_NAME.getName());
            long dateCreated = resultSet.getLong(DataHandler.COLUMN_USER_DATE_CREATED.getName());
            long lastOnline = resultSet.getLong(DataHandler.COLUMN_USER_LAST_ONLINE.getName());
            long newQuestsDate = resultSet.getLong(DataHandler.COLUMN_NEW_QUESTS_DATE.getName());

            Map<UUID, BattlePassData> battlePassData = DataHandler.GSON.fromJson(resultSet.getString(DataHandler.COLUMN_BATTLE_PASS_DATA.getName()), new TypeToken<Map<UUID, BattlePassData>>(){}.getType());
            Map<UUID, QuestData> questData = DataHandler.GSON.fromJson(resultSet.getString(DataHandler.COLUMN_QUEST_DATA.getName()), new TypeToken<Map<UUID, QuestData>>(){}.getType());
            Map<String, MilestoneData> milestoneData = DataHandler.GSON.fromJson(resultSet.getString(DataHandler.COLUMN_MILESTONE_DATA.getName()), new TypeToken<Map<String, MilestoneData>>(){}.getType());

            // Remove battle pass datas for expired battle pass seasons.
            battlePassData.values().removeIf(BattlePassData::isExpired);

            String loreCompletedStr = resultSet.getString(DataHandler.COLUMN_LORE_COMPLETED.getName());
            String rpgXpStr = resultSet.getString(DataHandler.COLUMN_RPG_XP.getName());
            String rpgLevelsStr = resultSet.getString(DataHandler.COLUMN_RPG_LEVELS.getName());
            String trackerMode = resultSet.getString(DataHandler.COLUMN_TRACKER_MODE.getName());
            if (trackerMode == null) {
                trackerMode = "BOSS_BAR";
            }
            String disabledTrackerCategoriesStr = resultSet.getString(DataHandler.COLUMN_DISABLED_TRACKER_CATEGORIES.getName());
            String loreQuestsProgressStr = resultSet.getString(DataHandler.COLUMN_LORE_QUESTS_PROGRESS.getName());
            String personalQuestDataStr = resultSet.getString(DataHandler.COLUMN_PERSONAL_QUEST_DATA.getName());

            Set<String> loreCompleted = DataHandler.GSON.fromJson(loreCompletedStr, new TypeToken<Set<String>>(){}.getType());
            Map<String, Double> rpgXp = DataHandler.GSON.fromJson(rpgXpStr, new TypeToken<Map<String, Double>>(){}.getType());
            Map<String, Integer> rpgLevels = DataHandler.GSON.fromJson(rpgLevelsStr, new TypeToken<Map<String, Integer>>(){}.getType());
            Set<String> disabledTrackerCategories = DataHandler.GSON.fromJson(disabledTrackerCategoriesStr, new TypeToken<Set<String>>(){}.getType());
            Map<String, LoreQuestData> loreQuestsProgress = DataHandler.GSON.fromJson(loreQuestsProgressStr, new TypeToken<Map<String, LoreQuestData>>(){}.getType());
            Map<String, PersonalQuestData> personalQuestData = DataHandler.GSON.fromJson(personalQuestDataStr, new TypeToken<Map<String, PersonalQuestData>>(){}.getType());

            if (loreCompleted == null) loreCompleted = new HashSet<>();
            if (rpgXp == null) rpgXp = new HashMap<>();
            if (rpgLevels == null) rpgLevels = new HashMap<>();
            if (disabledTrackerCategories == null) disabledTrackerCategories = new HashSet<>();
            if (loreQuestsProgress == null) loreQuestsProgress = new HashMap<>();
            if (personalQuestData == null) personalQuestData = new HashMap<>();

            return new QuestUser(
                uuid, 
                name, 
                dateCreated, 
                lastOnline, 
                newQuestsDate, 
                battlePassData, 
                questData, 
                milestoneData,
                loreCompleted,
                rpgXp,
                rpgLevels,
                disabledTrackerCategories,
                loreQuestsProgress,
                trackerMode,
                personalQuestData
            );
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final Function<ResultSet, BattlePassSeason> SEASON_LOADER = resultSet -> {
        try {
            UUID uuid = UUID.fromString(resultSet.getString(DataHandler.COLUMN_BP_ID.getName()));
            String name = resultSet.getString(DataHandler.COLUMN_BP_NAME.getName());
            long startDate = resultSet.getLong(DataHandler.COLUMN_BP_START_DATE.getName());
            long endDate = resultSet.getLong(DataHandler.COLUMN_BP_END_DATE.getName());
            long expireDate = resultSet.getLong(DataHandler.COLUMN_BP_EXPIRE_DATE.getName());
            boolean active = resultSet.getBoolean(DataHandler.COLUMN_BP_ACTIVE.getName());

            return new BattlePassSeason(uuid, name, startDate, endDate, expireDate, active);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return null;
        }
    };

    public static final InsertQuery<BattlePassSeason> SEASON_INSERT = new InsertQuery<BattlePassSeason>()
        .setValue(DataHandler.COLUMN_BP_ID, season -> season.getId().toString())
        .setValue(DataHandler.COLUMN_BP_NAME, BattlePassSeason::getName)
        .setValue(DataHandler.COLUMN_BP_START_DATE, season -> String.valueOf(season.getStartDate()))
        .setValue(DataHandler.COLUMN_BP_END_DATE, season -> String.valueOf(season.getEndDate()))
        .setValue(DataHandler.COLUMN_BP_EXPIRE_DATE, season -> String.valueOf(season.getExpireDate()))
        .setValue(DataHandler.COLUMN_BP_ACTIVE, season -> String.valueOf(season.isLaunched() ? 1 : 0));

    public static final UpdateQuery<BattlePassSeason> SEASON_UPDATE = new UpdateQuery<BattlePassSeason>()
        .setValue(DataHandler.COLUMN_BP_ACTIVE, season -> String.valueOf(season.isLaunched() ? 1 : 0));
}
