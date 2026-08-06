package su.nightexpress.quests.personal.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Calendar;

public class PersonalQuestData {

    private String categoryId;
    private String objectiveId;
    private int progress;
    private int requiredAmount;
    private double scaledMoney;
    private long dateAccepted;
    private int questsAcceptedToday;
    private long lastAcceptedDayTimestamp;

    public PersonalQuestData() {
    }

    public PersonalQuestData(@NotNull String categoryId, @Nullable String objectiveId, int progress, int requiredAmount, double scaledMoney, long dateAccepted, int questsAcceptedToday, long lastAcceptedDayTimestamp) {
        this.categoryId = categoryId;
        this.objectiveId = objectiveId;
        this.progress = progress;
        this.requiredAmount = requiredAmount;
        this.scaledMoney = scaledMoney;
        this.dateAccepted = dateAccepted;
        this.questsAcceptedToday = questsAcceptedToday;
        this.lastAcceptedDayTimestamp = lastAcceptedDayTimestamp;
    }

    @NotNull
    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@NotNull String categoryId) {
        this.categoryId = categoryId;
    }

    @Nullable
    public String getObjectiveId() {
        return objectiveId;
    }

    public void setObjectiveId(@Nullable String objectiveId) {
        this.objectiveId = objectiveId;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getRequiredAmount() {
        return requiredAmount;
    }

    public void setRequiredAmount(int requiredAmount) {
        this.requiredAmount = requiredAmount;
    }

    public double getScaledMoney() {
        return scaledMoney;
    }

    public void setScaledMoney(double scaledMoney) {
        this.scaledMoney = scaledMoney;
    }

    public long getDateAccepted() {
        return dateAccepted;
    }

    public void setDateAccepted(long dateAccepted) {
        this.dateAccepted = dateAccepted;
    }

    public int getQuestsAcceptedToday() {
        return questsAcceptedToday;
    }

    public void setQuestsAcceptedToday(int questsAcceptedToday) {
        this.questsAcceptedToday = questsAcceptedToday;
    }

    public long getLastAcceptedDayTimestamp() {
        return lastAcceptedDayTimestamp;
    }

    public void setLastAcceptedDayTimestamp(long lastAcceptedDayTimestamp) {
        this.lastAcceptedDayTimestamp = lastAcceptedDayTimestamp;
    }

    public boolean checkAndResetDailyLimit() {
        if (lastAcceptedDayTimestamp <= 0) {
            return false;
        }
        Calendar today = Calendar.getInstance();
        Calendar lastAccepted = Calendar.getInstance();
        lastAccepted.setTimeInMillis(lastAcceptedDayTimestamp);

        if (today.get(Calendar.YEAR) != lastAccepted.get(Calendar.YEAR) ||
            today.get(Calendar.DAY_OF_YEAR) != lastAccepted.get(Calendar.DAY_OF_YEAR)) {
            this.questsAcceptedToday = 0;
            return true;
        }
        return false;
    }

    public boolean hasActiveQuest() {
        return this.objectiveId != null && !this.objectiveId.isEmpty();
    }
}
