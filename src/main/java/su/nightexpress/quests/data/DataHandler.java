package su.nightexpress.quests.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.nightcore.db.AbstractUserDataManager;
import su.nightexpress.nightcore.db.sql.column.Column;
import su.nightexpress.nightcore.db.sql.column.ColumnType;
import su.nightexpress.nightcore.db.sql.query.impl.DeleteQuery;
import su.nightexpress.nightcore.db.sql.query.impl.SelectQuery;
import su.nightexpress.nightcore.db.sql.query.type.ValuedQuery;
import su.nightexpress.nightcore.db.sql.util.WhereOperator;
import su.nightexpress.nightcore.util.Lists;
import su.nightexpress.quests.QuestsPlugin;

import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.google.common.reflect.TypeToken;

import su.nightexpress.quests.battlepass.definition.BattlePassSeason;
import su.nightexpress.quests.data.serialize.MilestoneDataSerializer;
import su.nightexpress.quests.data.serialize.QuestCounterSerializer;
import su.nightexpress.quests.data.serialize.QuestDataSerializer;
import su.nightexpress.quests.island.data.IslandQuestProgress;
import su.nightexpress.quests.data.serialize.BattlePassDataSerializer;
import su.nightexpress.quests.quest.data.QuestCounter;
import su.nightexpress.quests.milestone.data.MilestoneData;
import su.nightexpress.quests.quest.data.QuestData;
import su.nightexpress.quests.battlepass.data.BattlePassData;
import su.nightexpress.quests.user.QuestUser;

public class DataHandler extends AbstractUserDataManager<QuestsPlugin, QuestUser> {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(BattlePassData.class, new BattlePassDataSerializer())
        .registerTypeAdapter(QuestCounter.class, new QuestCounterSerializer())
        .registerTypeAdapter(QuestData.class, new QuestDataSerializer())
        .registerTypeAdapter(MilestoneData.class, new MilestoneDataSerializer())
        .create();

    static final Column COLUMN_BATTLE_PASS_DATA = Column.of("battlePassData", ColumnType.STRING);
    static final Column COLUMN_QUEST_DATA       = Column.of("questData", ColumnType.STRING);
    static final Column COLUMN_MILESTONE_DATA   = Column.of("milestoneData", ColumnType.STRING);
    static final Column COLUMN_NEW_QUESTS_DATE  = Column.of("newQuestsDate", ColumnType.LONG);

    static final Column COLUMN_BP_ID         = Column.of("seasonId", ColumnType.STRING);
    static final Column COLUMN_BP_NAME       = Column.of("name", ColumnType.STRING);
    static final Column COLUMN_BP_START_DATE = Column.of("startDate", ColumnType.LONG);
    static final Column COLUMN_BP_END_DATE   = Column.of("endDate", ColumnType.LONG);
    static final Column COLUMN_BP_EXPIRE_DATE   = Column.of("expireDate", ColumnType.LONG);
    static final Column COLUMN_BP_ACTIVE     = Column.of("active", ColumnType.BOOLEAN);

    static final String BP_TABLE = "bp_season";

    public static final String ISLANDS_TABLE = "excellentquests_islands";

    public static final Column COLUMN_LORE_COMPLETED = Column.of("lore_completed", ColumnType.STRING);
    public static final Column COLUMN_RPG_XP = Column.of("rpg_xp", ColumnType.STRING);
    public static final Column COLUMN_RPG_LEVELS = Column.of("rpg_levels", ColumnType.STRING);
    public static final Column COLUMN_TRACKER_MODE = Column.of("tracker_mode", ColumnType.STRING);
    public static final Column COLUMN_DISABLED_TRACKER_CATEGORIES = Column.of("disabled_tracker_categories", ColumnType.STRING);
    public static final Column COLUMN_LORE_QUESTS_PROGRESS = Column.of("lore_quests_progress", ColumnType.STRING);
    public static final Column COLUMN_PERSONAL_QUEST_DATA = Column.of("personal_quest_data", ColumnType.STRING);

    static final Column COLUMN_ISLANDS_KEY = Column.of("island_uuid_quest_id", ColumnType.STRING);
    static final Column COLUMN_ISLANDS_OBJECTIVES = Column.of("objectives", ColumnType.STRING);
    static final Column COLUMN_ISLANDS_COMPLETED = Column.of("completed", ColumnType.BOOLEAN);

    public DataHandler(@NotNull QuestsPlugin plugin) {
        super(plugin);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        this.createTable(BP_TABLE, Lists.newList(
            COLUMN_BP_ID,
            COLUMN_BP_NAME,
            COLUMN_BP_START_DATE,
            COLUMN_BP_END_DATE,
            COLUMN_BP_EXPIRE_DATE,
            COLUMN_BP_ACTIVE
        ));


        this.createTable(ISLANDS_TABLE, Lists.newList(
            COLUMN_ISLANDS_KEY,
            COLUMN_ISLANDS_OBJECTIVES,
            COLUMN_ISLANDS_COMPLETED
        ));
    }

    @Override
    public void saveUser(@NotNull QuestUser user) {
        super.saveUser(user);
    }

    @Override
    @NotNull
    protected Function<ResultSet, QuestUser> createUserFunction() {
        return DataQueries.USER_LOADER;
    }

    @Override
    protected void addUpsertQueryData(@NotNull ValuedQuery<?, QuestUser> query) {
        query.setValue(COLUMN_NEW_QUESTS_DATE, user -> String.valueOf(user.getNewQuestsDate()));
        query.setValue(COLUMN_BATTLE_PASS_DATA, user -> GSON.toJson(user.getBattlePassData()));
        query.setValue(COLUMN_QUEST_DATA, user -> GSON.toJson(user.getQuestData()));
        query.setValue(COLUMN_MILESTONE_DATA, user -> GSON.toJson(user.getMilestoneDataMap()));
        query.setValue(COLUMN_LORE_COMPLETED, user -> GSON.toJson(user.getCompletedLoreQuests()));
        query.setValue(COLUMN_RPG_XP, user -> GSON.toJson(user.getRpgCategoryXP()));
        query.setValue(COLUMN_RPG_LEVELS, user -> GSON.toJson(user.getRpgCategoryLevels()));
        query.setValue(COLUMN_TRACKER_MODE, user -> user.getTrackerMode());
        query.setValue(COLUMN_DISABLED_TRACKER_CATEGORIES, user -> GSON.toJson(user.getDisabledTrackerCategories()));
        query.setValue(COLUMN_LORE_QUESTS_PROGRESS, user -> GSON.toJson(user.getLoreQuestsProgress()));
        query.setValue(COLUMN_PERSONAL_QUEST_DATA, user -> GSON.toJson(user.getPersonalQuestData()));
    }

    @Override
    protected void addSelectQueryData(@NotNull SelectQuery<QuestUser> query) {
        query.column(COLUMN_NEW_QUESTS_DATE);
        query.column(COLUMN_BATTLE_PASS_DATA);
        query.column(COLUMN_QUEST_DATA);
        query.column(COLUMN_MILESTONE_DATA);
        query.column(COLUMN_LORE_COMPLETED);
        query.column(COLUMN_RPG_XP);
        query.column(COLUMN_RPG_LEVELS);
        query.column(COLUMN_TRACKER_MODE);
        query.column(COLUMN_DISABLED_TRACKER_CATEGORIES);
        query.column(COLUMN_LORE_QUESTS_PROGRESS);
        query.column(COLUMN_PERSONAL_QUEST_DATA);
    }

    @Override
    protected void addTableColumns(@NotNull List<Column> columns) {
        columns.add(COLUMN_NEW_QUESTS_DATE);
        columns.add(COLUMN_BATTLE_PASS_DATA);
        columns.add(COLUMN_QUEST_DATA);
        columns.add(COLUMN_MILESTONE_DATA);
        columns.add(COLUMN_LORE_COMPLETED);
        columns.add(COLUMN_RPG_XP);
        columns.add(COLUMN_RPG_LEVELS);
        columns.add(COLUMN_TRACKER_MODE);
        columns.add(COLUMN_DISABLED_TRACKER_CATEGORIES);
        columns.add(COLUMN_LORE_QUESTS_PROGRESS);
        columns.add(COLUMN_PERSONAL_QUEST_DATA);
    }

    @NotNull
    public List<BattlePassSeason> loadBattlePassSeasons() {
        return this.select(BP_TABLE, DataQueries.SEASON_LOADER, SelectQuery::all);
    }

    public void insertBattlePassSeason(@NotNull BattlePassSeason season) {
        this.insert(BP_TABLE, DataQueries.SEASON_INSERT, season);
    }

    public void saveBattlePassSeason(@NotNull BattlePassSeason season) {
        this.update(BP_TABLE, DataQueries.SEASON_UPDATE, season);
    }

    public void removeBattlePassSeason(@NotNull BattlePassSeason season) {
        this.delete(BP_TABLE, new DeleteQuery<BattlePassSeason>().where(COLUMN_BP_ID, WhereOperator.EQUAL, passSeason -> passSeason.getId().toString()), season);
    }

    @NotNull
    public IslandQuestProgress loadIslandProgress(@NotNull UUID islandUuid, @NotNull String questId) {
        String key = islandUuid.toString() + ":" + questId;
        String sql = "SELECT * FROM " + ISLANDS_TABLE + " WHERE " + COLUMN_ISLANDS_KEY.getName() + " = ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String objectivesJson = rs.getString(COLUMN_ISLANDS_OBJECTIVES.getName());
                    boolean completed = rs.getBoolean(COLUMN_ISLANDS_COMPLETED.getName());
                    Map<String, Integer> progressMap = null;
                    if (objectivesJson != null && !objectivesJson.isEmpty()) {
                        Type type = new TypeToken<Map<String, Integer>>(){}.getType();
                        progressMap = GSON.fromJson(objectivesJson, type);
                    }
                    if (progressMap == null) {
                        progressMap = new HashMap<>();
                    }
                    return new IslandQuestProgress(islandUuid, questId, progressMap, completed);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new IslandQuestProgress(islandUuid, questId);
    }

    public void saveIslandProgress(@NotNull UUID islandUuid, @NotNull String questId, @NotNull Map<String, Integer> progress, boolean completed) {
        String key = islandUuid.toString() + ":" + questId;
        String objectivesJson = GSON.toJson(progress);
        String deleteSql = "DELETE FROM " + ISLANDS_TABLE + " WHERE " + COLUMN_ISLANDS_KEY.getName() + " = ?";
        String insertSql = "INSERT INTO " + ISLANDS_TABLE + " (" + 
                COLUMN_ISLANDS_KEY.getName() + ", " + 
                COLUMN_ISLANDS_OBJECTIVES.getName() + ", " + 
                COLUMN_ISLANDS_COMPLETED.getName() + ") VALUES (?, ?, ?)";
        try (Connection conn = this.getConnection()) {
            try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
                delStmt.setString(1, key);
                delStmt.executeUpdate();
            }
            try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
                insStmt.setString(1, key);
                insStmt.setString(2, objectivesJson);
                insStmt.setBoolean(3, completed);
                insStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteIslandProgress(@NotNull UUID islandUuid) {
        String prefix = islandUuid.toString() + ":%";
        String sql = "DELETE FROM " + ISLANDS_TABLE + " WHERE " + COLUMN_ISLANDS_KEY.getName() + " LIKE ?";
        try (Connection conn = this.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, prefix);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    @NotNull
    protected GsonBuilder registerAdapters(@NotNull GsonBuilder builder) {
        return builder;
    }

    @Override
    public void onSynchronize() {
        // TODO
    }
}
